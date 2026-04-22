---
inclusion: always
---

# Architecture: BlockchainGraph

## Pattern

MVVM (Model-View-ViewModel) with a Repository layer. Single-activity Compose navigation.

```
View Layer (Compose)
  CurrencyScreen / CoinPickerBottomSheet / PeriodSelector / PriceChart
        │  collectAsState() on StateFlow
        │  calls selectCoin / selectPeriod via ViewModel
        ▼
ViewModel (extends BaseViewModel)
        │  viewModelScope.launch + combine(coin, period)
        │  emits CurrencyUiState via StateFlow
        ▼
Repository (extends BaseRepository)
        │                    │
        ▼                    ▼
Remote (Retrofit)      Local (Room)
   Service                BancoLocal
```

---

## Layer Responsibilities

### View Layer — `ui/`

- **MainActivity** is a thin host. It calls `enableEdgeToEdge()` then `setContent { BlockchainGraphTheme { AppNavGraph() } }`. No business logic.
- **Screens** are `@Composable` functions in `ui/screen/`. They collect `StateFlow` values via `collectAsStateWithLifecycle()` and call ViewModel mutators (`selectCoin`, `selectPeriod`) in response to user actions.
- **Components** are reusable `@Composable` functions in `ui/component/`: `CoinPickerBottomSheet`, `PeriodSelector`, `PriceChart`.
- **Navigation** is handled by `AppNavGraph` using Compose Navigation (`NavHost` + `composable()`). No XML nav graph, no `FragmentManager`.
- Side effects (data loading) use `LaunchedEffect` keyed on state values — no `GlobalScope`, no manual coroutine scope creation inside composables.
- Every testable node has a `Modifier.testTag(...)` and `semantics { testTagsAsResourceId = true }` at the semantic root.
- `TestTags` constants objects are co-located with each composable file.

```kotlin
// Screen — collects StateFlow, calls ViewModel mutators
@Composable
fun CurrencyScreen(viewModel: CurrencyViewModel = koinViewModel()) {
    val coin by viewModel.coin.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // ...
}
```

### ViewModel Layer — `viewmodel/`

- Extends `BaseViewModel` which provides `error: StateFlow<String?>` and the abstract `refreshItens()`.
- Holds UI state as `MutableStateFlow` with private backing properties and public `asStateFlow()` exposure.
- `CurrencyUiState` is the single state object for `CurrencyScreen`: `isLoading`, `formattedPrice`, `chartPoints`, `errorMessage`.
- Reactive reload via `combine(_coin, _period).collectLatest { loadData(...) }` in `init` — no manual observer wiring.
- `selectCoin(coin)` and `selectPeriod(period)` are the only public mutators.
- All coroutines use `viewModelScope.launch` — never `GlobalScope`.
- Declared in `appModule` with `viewModel { }`. Never instantiated manually.

```kotlin
class CurrencyViewModel(...) : BaseViewModel() {
    private val _coin = MutableStateFlow(CurrencyEnum.US_DOLLAR)
    val coin: StateFlow<CurrencyEnum> = _coin.asStateFlow()

    private val _uiState = MutableStateFlow(CurrencyUiState())
    val uiState: StateFlow<CurrencyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(_coin, _period) { c, p -> c to p }
                .collectLatest { (c, p) -> loadData(c, p) }
        }
    }
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
- Room annotation processor uses **KSP**. Never use `kapt` for Room.
- `hasByX` naming convention for nullable existence checks (returns `null` = not found).

#### Database — `model/local/BancoLocal`
- Single `RoomDatabase` subclass named `BancoLocal`.
- Registered type converters: `DateConverter`, `ChartPeriodConverter`.

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

Single-activity pattern. `MainActivity` hosts a `NavHost` composable defined in `AppNavGraph`. Routes are string constants in `object Routes`. No XML nav graph, no `NavHostFragment`, no `FragmentManager` transactions.

```kotlin
object Routes { const val CURRENCY = "currency" }

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController, startDestination = Routes.CURRENCY) {
        composable(Routes.CURRENCY) { CurrencyScreen() }
    }
}
```

---

## Edge-to-Edge and Rotation

- `enableEdgeToEdge()` is called in `MainActivity.onCreate()` before `setContent`.
- `CurrencyScreen` applies `Modifier.windowInsetsPadding(WindowInsets.safeDrawing)` at the root `Scaffold`.
- Screen orientation is not locked — both portrait and landscape are supported.
- `CurrencyViewModel` survives rotation automatically as a `ViewModel`.

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

---

## Testing Architecture

Unit tests mock the boundary interfaces (`Service`, `CurrencyDao`, `ChartDao`, `ChartPointDao`, `DateProvider`) using Mockito-Kotlin. Composable tests use Robolectric + `createComposeRule()`. Screenshot tests use Roborazzi.

- `TestApplication` (in `src/test/`) prevents Koin from auto-starting during Robolectric tests.
- `KoinTestRule` manages Koin lifecycle in composable tests that need a ViewModel.
- `DateProvider` is always injected as a mock with a fixed `Date` to make cache logic deterministic.
- Property-based tests use Kotest `checkAll` / `Arb` generators with state-driven composables (single `setContent` call, state updated via `runOnUiThread`).

```
app/src/test/java/com/osias/blockchain/
├── repository/         # cache hit/miss, error delegation
├── viewmodel/          # formatCurrency, formatUnixDate, delegation
├── ui/                 # Robolectric composable tests + Roborazzi screenshot tests
└── property/           # property-based tests for composables and ViewModel
```

---

## Adding a New Screen — Checklist

1. Create `XyzViewModel` extending `BaseViewModel` (no `@Inject`).
2. Add `viewModel { XyzViewModel(get(), get()) }` to `appModule`.
3. Create `XyzScreen` composable in `ui/screen/`.
4. Define `object XyzScreenTags` with test tag constants in the same file.
5. Apply `Modifier.testTag(...)` to every testable node; enable `testTagsAsResourceId = true` at the semantic root.
6. Add `composable(Routes.XYZ) { XyzScreen() }` to `AppNavGraph`.
7. Write Robolectric unit tests using `onNodeWithTag()` in `ui/XyzScreenTest.kt`.
8. Write Roborazzi screenshot tests in `ui/XyzScreenshotTest.kt`; run `./gradlew recordRoborazziDebug` to generate baselines.
9. Write unit tests for the ViewModel and any new repository.

## Adding a New Repository — Checklist

1. Create the repository class extending `BaseRepository`.
2. Accept `DateProvider` as a constructor parameter with a default implementation.
3. Implement cache-or-fetch logic following the existing pattern.
4. Add `single { XyzRepository(get(), get()) }` to `appModule`.
5. Write unit tests covering cache hit, cache miss, and error delegation.
