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
| Composable test tags | snake_case string | `"currency_screen_select_coin_button"` |
| Packages | lowercase, no underscores | `com.osias.blockchain.model.entity` |

## File Organization

- One class per file. File name matches the class name exactly.
- Base classes are prefixed with `Base`: `BaseViewModel`, `BaseRepository`.
- Enumerations live in `model/enumeration/`.
- Room entities live in `model/entity/`.
- DAOs live in `model/local/dao/`.
- Converters (Room TypeConverters) live in `common/converter/`.
- Utilities live in `common/utils/`.
- Composable screens live in `ui/screen/`.
- Reusable composable components live in `ui/component/`.
- Each composable file co-locates its `TestTags` object in the same file.

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
- ViewModels use `viewModelScope.launch` for all coroutines — never `GlobalScope`.
- Composables use `LaunchedEffect` for side effects keyed on state values — never create coroutine scopes manually inside a composable.

```kotlin
// Repository — suspend
suspend fun getCharts(period: ChartPeriod): Chart { ... }

// ViewModel — viewModelScope
init {
    viewModelScope.launch {
        combine(_coin, _period) { c, p -> c to p }
            .collectLatest { (c, p) -> loadData(c, p) }
    }
}

// Composable — LaunchedEffect for side effects
LaunchedEffect(coin, period) {
    // triggered when coin or period changes
}
```

## Dependency Injection (Koin)

- ViewModels are declared with `viewModel { }` in `AppModule` and injected in composables via `koinViewModel()`.
- Repositories and singletons are declared with `single { }`.
- Never use `@Inject` annotations — Koin resolves dependencies through the module DSL.
- New ViewModels must be added to `appModule` with `viewModel { MyViewModel(get(), get()) }`.
- New repositories must be added to `appModule` with `single { MyRepository(get(), get()) }`.

```kotlin
val appModule = module {
    single<Retrofit> { /* build retrofit */ }
    single<Service> { get<Retrofit>().create(Service::class.java) }
    single { CurrencyRepository(get(), get<BancoLocal>().currencyDao()) }
    viewModel { CurrencyViewModel(get(), get()) }
}

// Composable — ViewModel injected by Koin
@Composable
fun CurrencyScreen(viewModel: CurrencyViewModel = koinViewModel()) { ... }
```

## StateFlow

- ViewModel state is exposed as `StateFlow` with private `MutableStateFlow` backing properties.
- Composables collect `StateFlow` via `collectAsStateWithLifecycle()` (not `collectAsState()`).
- UI state is consolidated into a single `UiState` data class per screen.

```kotlin
// ViewModel
private val _coin = MutableStateFlow(CurrencyEnum.US_DOLLAR)
val coin: StateFlow<CurrencyEnum> = _coin.asStateFlow()

private val _uiState = MutableStateFlow(CurrencyUiState())
val uiState: StateFlow<CurrencyUiState> = _uiState.asStateFlow()

// Composable
val coin by viewModel.coin.collectAsStateWithLifecycle()
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

## Compose

- Use `remember { mutableStateOf(...) }` (not `rememberSaveable`) for transient UI state like bottom sheet visibility — it should reset on rotation.
- Apply `Modifier.testTag("tag")` to every node that tests need to find.
- Enable `semantics { testTagsAsResourceId = true }` at the semantic root of each composable.
- Define test tag constants in an `object XyzTags` co-located with the composable file.
- Never use `!!` inside composables — use `?.let { }` for nullable unwrapping.

```kotlin
// TestTags co-located with the composable
object CurrencyScreenTags {
    const val SELECT_COIN_BUTTON = "currency_screen_select_coin_button"
    const val LOADING_INDICATOR  = "currency_screen_loading_indicator"
}

// Transient UI state — resets on rotation (correct behavior)
var showPicker by remember { mutableStateOf(false) }

// Testable node
Button(
    onClick = { showPicker = true },
    modifier = Modifier.testTag(CurrencyScreenTags.SELECT_COIN_BUTTON)
) { ... }
```

## Error Handling

- Repositories do not throw exceptions for network errors. They call `delegate?.onError(Error(...))` instead.
- ViewModels wrap repository calls in `try/catch` and emit error state via `UiState.errorMessage`.
- Always check `result.isSuccessful` before accessing `result.body()`.
- Use `?.let { }` to safely unwrap nullable bodies.

```kotlin
// ViewModel — catch and emit error state
private suspend fun loadData(coin: CurrencyEnum, period: ChartPeriod) {
    _uiState.value = CurrencyUiState(isLoading = true)
    try {
        val currency = currencyRepository.getValueByCurrency(coin)
        // ...
        _uiState.value = CurrencyUiState(isLoading = false, formattedPrice = ...)
    } catch (e: Exception) {
        _uiState.value = CurrencyUiState(isLoading = false, errorMessage = e.message)
    }
}
```

## Null Safety

- Prefer `?.let { }` over `if (x != null)` for nullable unwrapping.
- Use early return with `?.let { return }` to skip logic when a cached value exists.
- **Never use `!!`** anywhere in the codebase.
- When multiple properties of a nullable object are needed, unwrap once with `?.let { }`.

```kotlin
// Good — early return cache hit
val dbChart = chartDao.hasChartByTimeAndPeriod(dateProvider.getDate(), period)
dbChart?.let { return }

// Bad — never do this
binding!!.lastCurrency.text = "..."
```

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
