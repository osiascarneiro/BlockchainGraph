---
inclusion: always
---

# Code Style: BlockchainGraph

## Language

Kotlin. No Java source files in the `app/src/main` tree.

## Naming Conventions

| Element | Convention | Example |
|---|---|---|
| Classes / Interfaces | PascalCase | `ChartRepository`, `DateProvider` |
| Functions / variables | camelCase | `getValueByCurrency`, `dateProvider` |
| Constants / companion vals | camelCase (companion) | `stripMinutes` |
| Enum entries | UPPER_SNAKE_CASE | `ONE_MONTH`, `US_DOLLAR` |
| DB table names | snake_case string | `"chart_point"`, `"currency"` |
| DB column names | snake_case string | `"currency_symbol"`, `"last_value"` |
| Layout files | snake_case | `fragment_actual_currency.xml`, `dialog_coin_picker.xml` |
| Resource IDs | snake_case | `R.id.thirty_days`, `R.id.selectCoinButton` |
| Packages | lowercase, no underscores | `com.osias.blockchain.model.entity` |

## File Organization

- One class per file. File name matches the class name exactly.
- Base classes are prefixed with `Base`: `BaseFragment`, `BaseViewModel`, `BaseRepository`.
- Enumerations live in `model/enumeration/`.
- Room entities live in `model/entity/`.
- DAOs live in `model/local/dao/`.
- Converters (Room TypeConverters) live in `common/converter/`.
- Utilities live in `common/utils/`.

## Classes and Data Classes

- Prefer `data class` for entities and value objects (all three Room entities are `data class`).
- Use `@ColumnInfo(name = "...")` on every Room entity field — never rely on field name inference.
- Use `@SerializedName("...")` on every field that maps to a JSON key, even when the name matches.
- Fields that exist in JSON but must not be persisted in Room are annotated with `@Ignore`.

```kotlin
// Correct entity pattern
@Entity(tableName = "chart", primaryKeys = ["id", "period"])
data class Chart(
    @ColumnInfo(name = "id")
    var time: Date = Date(),

    @ColumnInfo(name = "name")
    @SerializedName("name")
    var name: String,

    @ColumnInfo(name = "period")
    var period: ChartPeriod
) {
    @Ignore
    @SerializedName("values")
    var values: List<ChartPoint>? = null
}
```

## Enums

- Enum entries are UPPER_SNAKE_CASE.
- Enums that are serialized over the network use `@SerializedName` on each entry.
- Enums that carry a display value use a constructor parameter (e.g., `CurrencyEnum(val symbol: String)`).

```kotlin
enum class ChartPeriod {
    @SerializedName("30days") ONE_MONTH,
    @SerializedName("all")    ALL_TIME
}

enum class CurrencyEnum(val symbol: String) {
    US_DOLLAR("USD"),
    EURO("EUR")
}
```

## Coroutines

- Repository functions that perform I/O are `suspend fun`.
- View layer dispatches coroutines with `GlobalScope.launch` (current pattern — note: prefer `viewLifecycleOwner.lifecycleScope` for new code).
- UI updates always switch to `Dispatchers.Main` explicitly.

```kotlin
// Repository — suspend
suspend fun getCharts(period: ChartPeriod): Chart { ... }

// Fragment — launch on background, switch to Main for UI
GlobalScope.launch {
    val chart = viewModel.getChart(period)
    GlobalScope.launch(Dispatchers.Main) {
        buildGraph(viewModel.getPoints(chart.time, chart.period))
    }
}
```

## Dependency Injection (Koin)

- ViewModels are declared with `viewModel { }` in `AppModule` and injected in fragments via `override val viewModel: XyzViewModel by viewModel()` — no type parameter on `BaseFragment` needed.
- Repositories and singletons are declared with `single { }`.
- Never use `@Inject` annotations — Koin resolves dependencies through the module DSL.
- New ViewModels must be added to `appModule` with `viewModel { MyViewModel(get(), get()) }`.
- New repositories must be added to `appModule` with `single { MyRepository(get(), get()) }`.

```kotlin
// Module declaration
val appModule = module {
    single<Retrofit> { /* build retrofit */ }
    single<Service> { get<Retrofit>().create(Service::class.java) }
    single { CurrencyRepository(get(), get<BancoLocal>().currencyDao()) }
    viewModel { CurrencyViewModel(get(), get()) }
}

// Fragment — ViewModel injected by Koin
class CurrencyFragment : BaseFragment<CurrencyViewModel>(CurrencyViewModel::class)

// BaseFragment — delegates to Koin
val viewModel: T by viewModel(cls)
```

## LiveData

- ViewModel state is exposed as `MutableLiveData` with a default value set at declaration.
- Fragments observe LiveData in `bindItems()` (or equivalent private setup function), called from `onCreate`.

```kotlin
val period = MutableLiveData(ChartPeriod.ONE_MONTH)
val coin   = MutableLiveData(CurrencyEnum.US_DOLLAR)
```

## Error Handling

- Repositories do not throw exceptions for network errors. They call `delegate?.onError(Error(...))` instead.
- Always check `result.isSuccessful` before accessing `result.body()`.
- Use `?.let { }` to safely unwrap nullable bodies.

```kotlin
if (!result.isSuccessful) {
    delegate?.onError(Error(result.errorBody().toString()))
} else {
    result.body()?.let { chart ->
        // persist
    }
}
```

## Null Safety

- Prefer `?.let { }` over `if (x != null)` for nullable unwrapping.
- Use early return with `?.let { return }` to skip logic when a cached value exists.
- **Never use `!!`** — it is a runtime crash waiting to happen. The only accepted exception is `requireNotNull(x)` in `onCreateView` immediately after assigning the binding, where non-nullability is structurally guaranteed.
- When multiple properties of a nullable object are needed, unwrap once with `?.let { }` rather than chaining multiple `?.` calls.

```kotlin
// Good — unwrap once, use safely
binding?.let { b ->
    b.lastCurrencyTitle.text = "..."
    b.lastCurrency.text = "..."
}

// Good — early return cache hit
val dbChart = chartDao.hasChartByTimeAndPeriod(dateProvider.getDate(), period)
dbChart?.let { return }

// Bad — never do this
binding!!.lastCurrency.text = "..."
```

## View Binding

- `viewBinding true` is enabled in `app/build.gradle`.
- All fragments and dialogs use the generated binding class — never `kotlinx.android.synthetic` (removed) or `findViewById`.
- Pattern for fragments and dialogs:
  ```kotlin
  private var binding: FragmentXyzBinding? = null

  override fun onCreateView(...): View {
      binding = FragmentXyzBinding.inflate(inflater, container, false)
      return requireNotNull(binding).root  // requireNotNull instead of !!
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
      binding?.let { b ->
          // access views via b.viewId
      }
  }

  override fun onDestroyView() {
      super.onDestroyView()
      binding = null  // avoid memory leaks
  }
  ```
- `requireNotNull(binding)` is the only acceptable place to assert non-null — exclusively in `onCreateView` where the binding was just assigned on the line above.
- Everywhere else, use `binding?.let { b -> }` or `binding?.viewId` — never `!!`.
- When multiple views need updating in the same block, use `binding?.let { b -> }` to unwrap once and access all views through `b`.

## Comments

- Comments explaining non-obvious business logic are welcome (Portuguese is acceptable — the codebase uses both Portuguese and English comments).
- Avoid redundant comments that just restate the code.
- TODOs are written as `//TODO: description`.

## Formatting

- 4-space indentation.
- Opening braces on the same line.
- No trailing whitespace.
- One blank line between functions.
- Imports are not wildcard — each import is explicit.
