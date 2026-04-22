# Design: BlockchainGraph

## Architecture Overview

BlockchainGraph follows the **MVVM (Model-View-ViewModel)** pattern with a Repository layer, using Koin for dependency injection and Kotlin Coroutines for asynchronous operations.

```
┌─────────────────────────────────────────┐
│              View Layer                 │
│  MainActivity → CurrencyFragment        │
│  CoinPickerDialog                       │
└────────────────┬────────────────────────┘
                 │ LiveData / Coroutines
┌────────────────▼────────────────────────┐
│           ViewModel Layer               │
│  CurrencyViewModel                      │
│  (coin: LiveData, period: LiveData)     │
└────────────────┬────────────────────────┘
                 │ suspend functions
┌────────────────▼────────────────────────┐
│          Repository Layer               │
│  CurrencyRepository  ChartRepository   │
└──────┬──────────────────────┬───────────┘
       │                      │
┌──────▼──────┐      ┌────────▼────────┐
│  Remote     │      │  Local (Room)   │
│  (Retrofit) │      │  BancoLocal DB  │
└─────────────┘      └─────────────────┘
```

---

## Component Design

### View Layer

#### MainActivity
- Single activity, hosts the Navigation Component's `NavHostFragment`.
- Layout: `activity_main.xml` with a full-screen `NavHostFragment`.

#### CurrencyFragment
- Main screen fragment.
- Observes `CurrencyViewModel.coin` and `CurrencyViewModel.period` via LiveData.
- On coin change: fetches current rate and updates the label.
- On period change: fetches chart data and rebuilds the graph.
- Uses `GlobalScope.launch` for async calls (coroutines).
- Renders chart via MPAndroidChart `LineChart`.

#### CoinPickerDialog
- `DialogFragment` with a `NumberPicker`.
- Accepts a `NumberPicker.OnValueChangeListener` callback.
- Initialized with the currently selected `CurrencyEnum`.

---

### ViewModel Layer

#### BaseViewModel
- Extends `ViewModel`.
- Declares abstract `refreshItens()`.

#### CurrencyViewModel
- Injected via Dagger (`@Inject constructor`).
- State:
  - `coin: MutableLiveData<CurrencyEnum>` — default `USD`
  - `period: MutableLiveData<ChartPeriod>` — default `ONE_MONTH`
- Methods:
  - `getCurrencyByLocale(coin)` — delegates to `CurrencyRepository`
  - `getChart(period)` — delegates to `ChartRepository`
  - `getPoints(chartId, chartPeriod)` — delegates to `ChartRepository`
  - `formatCurrency(value)` — formats using `NumberFormat` with selected currency
  - `formatUnixDate(value)` — formats Unix timestamp as `dd/MM/yy`

---

### Repository Layer

#### BaseRepository
- Holds an optional `delegate` for error callbacks.

#### CurrencyRepository
- Dependencies: `Service`, `CurrencyDao`, `DateProvider`
- `getValueByCurrency(currency)`:
  1. Query DB for today's record matching currency symbol.
  2. If absent, call `refreshDb()`.
  3. Return DB record.
- `refreshDb()`:
  1. Check if any record exists for the current hour (`DateUtil.stripMinutes`).
  2. If present, skip fetch.
  3. Otherwise, call `service.actualCurrency()`, persist all entries.

#### ChartRepository
- Dependencies: `Service`, `ChartDao`, `ChartPointDao`, `DateProvider`
- `getCharts(period)`:
  1. Call `refreshDb(period)`.
  2. Return chart from DB by today's date + period.
- `getChartPoints(chartId, chartPeriod)`:
  1. Return all points from DB for the given chart reference.
- `refreshDb(period)`:
  1. Check if chart exists for today + period.
  2. If present, skip fetch.
  3. Otherwise, call `service.getCurrencyChart(period)`, persist chart and all points.

---

### Data Layer

#### Entities

**Chart**
```
TABLE chart
  id       DATE    (PK, composite with period)
  period   TEXT    (PK, composite with id)
  name     TEXT
  description TEXT
```

**ChartPoint**
```
TABLE chart_point
  x            REAL  (PK, composite)
  y            REAL  (PK, composite)
  chart_id     DATE  (PK, FK → chart.id)
  chart_period TEXT  (PK, FK → chart.period)
  ON DELETE CASCADE
```

**CurrencyValue**
```
TABLE currency
  currency_symbol  TEXT    (PK, composite with time)
  time             DATE    (PK, composite with currency_symbol)
  fifteen_minutes  REAL
  buy_value        REAL
  sell_value       REAL
  last_value       REAL
  symbol           TEXT
```

#### Type Converters
- `DateConverter`: `Date ↔ Long` (Unix timestamp)
- `ChartPeriodConverter`: `ChartPeriod ↔ String` (enum name)

---

### Network Layer

#### Service (Retrofit Interface)
```kotlin
GET /ticker
  → Call<Map<String, CurrencyValue>>

GET /charts/market-price?format=json&timespan={ChartPeriod}
  → Call<Chart>
```

- Base URL: `https://blockchain.info/` (configurable per build variant)
- Converters: `GsonConverterFactory`, `EnumRetrofitConverterFactory`
- Interceptors: `HttpLoggingInterceptor` (debug), Accept header injection

#### EnumRetrofitConverterFactory
- Custom Retrofit converter that serializes enums using their `@SerializedName` annotation value.

---

### Dependency Injection (Koin)

#### AppModule
- Declares all dependencies in a single `module { }` block.
- Provides: `Retrofit`, `Service`, `BancoLocal`, `CurrencyRepository`, `ChartRepository`, `CurrencyViewModel`
- Singletons use `single { }`. ViewModels use `viewModel { }`.
- Started in `BlockchainGraphApplication.onCreate()` via `startKoin { }`.

```kotlin
val appModule = module {
    single<Retrofit> { /* build */ }
    single<Service> { get<Retrofit>().create(Service::class.java) }
    single { CurrencyRepository(get(), get<BancoLocal>().currencyDao()) }
    viewModel { CurrencyViewModel(get(), get()) }
}
```

---

### Enumerations

#### ChartPeriod
| Enum | Serialized Value |
|---|---|
| ONE_MONTH | `30days` |
| TWO_MONTHS | `60days` |
| SIX_MONTHS | `180days` |
| ONE_YEAR | `1year` |
| TWO_YEARS | `2years` |
| ALL_TIME | `all` |

#### CurrencyEnum
22 currencies with their ISO 4217 symbols (USD, AUD, BRL, CAD, CHF, CLP, CNY, DKK, EUR, GBP, HKD, INR, ISK, JPY, KRW, NZD, PLN, RUB, SEK, SGD, THB, TWD).

---

## Build Configuration

| Property | Value |
|---|---|
| compileSdk | 36 |
| minSdkVersion | 21 |
| targetSdkVersion | 34 |
| applicationId | com.osias.blockchain |
| versionCode | 1 |
| versionName | 1.0 |
| AGP | 9.1.0 |
| Gradle | 9.4.1 |
| JDK | 17 |

Build variants: `debug`, `staging`, `release` — each with its own `SERVER_URL`.
