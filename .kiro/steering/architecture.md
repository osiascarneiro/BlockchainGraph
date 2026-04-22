---
inclusion: always
---

# Architecture: BlockchainGraph

## Pattern

MVVM (Model-View-ViewModel) with a Repository layer. Single-activity navigation via Android Navigation Component.

```
View (Fragment / Dialog)
        │  observes LiveData
        │  calls suspend funs via GlobalScope.launch
        ▼
ViewModel (extends BaseViewModel)
        │  calls suspend funs
        ▼
Repository (extends BaseRepository)
        │                    │
        ▼                    ▼
Remote (Retrofit)      Local (Room)
   Service                BancoLocal
```

---

## Layer Responsibilities

### View Layer — `view/`

- **Activities** are thin hosts. `MainActivity` only sets the content view and hosts the `NavHostFragment`. No business logic lives here.
- **Fragments** extend `BaseFragment` (no generic). Each fragment declares `override val viewModel: XyzViewModel by viewModel()` — Koin resolves the type from the reified call site. No type parameter on the base class is needed or possible due to Kotlin's reified generic constraints.
- **Dialogs** extend `DialogFragment`. They receive callbacks via constructor parameters (not via fragment result API).
- Fragments observe ViewModel `LiveData` in a dedicated private `bindItems()` function called from `onCreate`.
- UI setup (click listeners, etc.) goes in `onViewCreated` — not the deprecated `onActivityCreated`.
- All fragments and dialogs use **View Binding**. Binding is inflated in `onCreateView`, accessed via `binding?.let {}` in all other lifecycle methods, and nulled in `onDestroyView` to prevent memory leaks.
- Async work is dispatched with `GlobalScope.launch`. UI updates switch back to `Dispatchers.Main`.
- Fragments never access repositories or DAOs directly.

```kotlin
// Fragment base — no generic, viewModel contract via abstract property
abstract class BaseFragment : Fragment() {
    abstract val viewModel: BaseViewModel
}

// Subclass — Koin resolves the type from the reified call site
class CurrencyFragment : BaseFragment() {
    override val viewModel: CurrencyViewModel by viewModel()
}
```

> Koin 4.x removed the non-reified `viewModel(clazz = ...)` overload because reified generics cannot be passed through inheritance chains in Kotlin. The `by viewModel()` delegate in the subclass is the officially supported pattern — the base class holds the `abstract val viewModel: BaseViewModel` contract, each subclass provides the typed delegate. No generic type parameter on `BaseFragment` is needed.

### ViewModel Layer — `viewmodel/`

- Extends `BaseViewModel` which provides a shared `error: MutableLiveData<String>` and the abstract `refreshItens()`.
- Holds UI state as `MutableLiveData` with default values.
- Exposes `suspend fun` methods that delegate directly to repositories — no business logic beyond formatting.
- Formatting helpers (`formatCurrency`, `formatUnixDate`) live here because they depend on the current UI state (`coin.value`).
- Declared in `appModule` with `viewModel { }`. Never instantiated manually.
- No `@Inject` annotation needed — Koin resolves constructor parameters via `get()`.

```kotlin
class CurrencyViewModel(
    private val currencyRepository: CurrencyRepository,
    private val chartsRepository: ChartRepository
): BaseViewModel() {
    val period = MutableLiveData(ChartPeriod.ONE_MONTH)
    val coin   = MutableLiveData(CurrencyEnum.US_DOLLAR)
}
```

### Repository Layer — `model/repository/`

- Extends `BaseRepository` which holds an optional `RepositoryErrorDelegate`.
- Owns the cache-or-fetch decision: check DB first, call network only if no valid cached record exists.
- All I/O methods are `suspend fun`.
- Network calls use `.execute()` (blocking, called from a coroutine) — not enqueue/callback style.
- Errors are reported via `delegate?.onError(...)`, never thrown.
- `DateProvider` interface is injected to allow deterministic date control in tests.

```kotlin
// Cache-or-fetch pattern used in every repository
private suspend fun refreshDb(period: ChartPeriod) {
    val cached = chartDao.hasChartByTimeAndPeriod(dateProvider.getDate(), period)
    cached?.let { return }          // cache hit — skip network

    val result = service.getCurrencyChart(period).execute()
    if (!result.isSuccessful) {
        delegate?.onError(Error(result.errorBody().toString()))
    } else {
        result.body()?.let { /* persist */ }
    }
}
```

### Model Layer — `model/`

#### Entities — `model/entity/`
- Plain `data class` annotated with Room and Gson annotations.
- Composite primary keys are declared in `@Entity(primaryKeys = [...])`.
- Foreign keys with `CASCADE` delete/update are declared in `@Entity(foreignKeys = [...])`.
- Fields that are JSON-only (not persisted) use `@Ignore`.

#### DAOs — `model/local/dao/`
- Kotlin `interface` annotated with `@Dao`.
- Query methods that return a single result or list are `suspend fun`.
- Insert/update/delete methods are regular (non-suspend) functions.
- Room annotation processor uses **KSP** (`ksp "androidx.room:room-compiler"`). Never use `kapt` for Room.
- `hasByX` naming convention for nullable existence checks (returns `null` = not found).

```kotlin
@Query("SELECT * FROM chart WHERE id = :time AND period = :period LIMIT 1")
suspend fun hasChartByTimeAndPeriod(time: Date, period: ChartPeriod): Chart?  // null = not cached
```

#### Database — `model/local/BancoLocal`
- Single `RoomDatabase` subclass named `BancoLocal`.
- Registered type converters: `DateConverter`, `ChartPeriodConverter`.
- `DateConverter.fromDate` strips minutes before storing — this is what enforces hourly cache granularity.

#### Remote — `model/remote/`
- `Service` is a Retrofit interface. Two endpoints only.
- `EnumRetrofitConverterFactory` serializes enums using their `@SerializedName` value as the query parameter string.

---

## Dependency Injection — `module/`

Single Koin module file:

| File | Role |
|---|---|
| `AppModule.kt` | Declares all `single` and `viewModel` definitions for the entire app. |

`BlockchainGraphApplication` extends `Application` and calls `startKoin { }` in `onCreate`.

**Rules:**
- Every new repository must be added to `appModule` as `single { MyRepository(get(), ...) }`.
- Every new ViewModel must be added to `appModule` as `viewModel { MyViewModel(get(), ...) }`.
- `get()` resolves dependencies by type — ensure the required type is already declared in the module.
- No `@Inject`, `@Component`, `@Module`, or `@Provides` annotations anywhere in the codebase.

---

## Navigation

Single-activity pattern. `MainActivity` hosts a `NavHostFragment` bound to `res/navigation/nav_graph.xml`. All screen transitions go through the Navigation Component — no `FragmentManager` transactions in activities.

---

## Caching Architecture

Two independent cache strategies, both using `DateProvider` for testability:

```
Currency cache (hourly):
  getValueByCurrency(coin)
    → query DB by (date, symbol)
    → if null: refreshDb()
      → query DB by stripMinutes(date)   ← hourly key
      → if null: fetch network, persist all currencies
    → return DB record

Chart cache (daily per period):
  getCharts(period)
    → refreshDb(period)
      → query DB by (date, period)
      → if null: fetch network, persist chart + all points
    → return DB record by (date, period)
```

`DateUtil.stripMinutes()` is the single source of truth for the hourly cache key. `DateConverter.fromDate()` also calls it before persisting, ensuring DB timestamps are always hour-aligned.

---

## Testing Architecture

Unit tests mock the boundary interfaces (`Service`, `CurrencyDao`, `ChartDao`, `ChartPointDao`, `DateProvider`) using Mockito-Kotlin. No Android framework is needed for repository or ViewModel tests.

- `InstantTaskExecutorRule` is required in any test that touches `LiveData`.
- `DateProvider` is always injected as a mock with a fixed `Date` to make cache logic deterministic.
- Property-based tests verify invariants across multiple inputs (currencies, periods, dates, call counts) rather than single examples.

```
app/src/test/java/com/osias/blockchain/
├── repository/
│   ├── CurrencyRepositoryTest.kt   # cache hit/miss, error delegation
│   └── ChartRepositoryTest.kt      # cache hit/miss, FK persistence, error delegation
├── viewmodel/
│   └── CurrencyViewModelTest.kt    # formatCurrency, formatUnixDate, delegation
└── property/
    ├── CurrencyCacheFreshnessPropertyTest.kt   # CP-1: hourly cache invariant
    ├── ChartCacheFreshnessPropertyTest.kt      # CP-2: daily cache invariant
    └── PeriodMappingCompletenessPropertyTest.kt # CP-5: button↔period bijection
```

---

## Adding a New Screen — Checklist

1. Create `XyzViewModel` extending `BaseViewModel` (no `@Inject`).
2. Add `viewModel { XyzViewModel(get(), get()) }` to `appModule`.
3. Create `XyzFragment` extending `BaseFragment` (no generic).
4. Declare `override val viewModel: XyzViewModel by viewModel()` in the fragment.
5. Use View Binding: inflate in `onCreateView`, access via `binding?.let {}`, null in `onDestroyView`.
6. Put click listeners and UI setup in `onViewCreated`, not `onActivityCreated`.
7. Add the fragment destination to `res/navigation/nav_graph.xml`.
8. Write unit tests for the ViewModel and any new repository.

## Adding a New Repository — Checklist

1. Create the repository class extending `BaseRepository`.
2. Accept `DateProvider` as a constructor parameter with a default implementation.
3. Implement cache-or-fetch logic following the existing pattern.
4. Add `single { XyzRepository(get(), get()) }` to `appModule`.
5. Write unit tests covering cache hit, cache miss, and error delegation.
