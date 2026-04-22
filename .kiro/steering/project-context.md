---
inclusion: always
---

# Project Context: BlockchainGraph

## What This App Does

BlockchainGraph is an Android application that displays real-time Bitcoin exchange rates and historical price charts. It consumes the public [blockchain.info](https://blockchain.info) API — no API key required.

Users can:
- Select a fiat currency from 22 options (USD, EUR, BRL, JPY, GBP, and more)
- View the current BTC last-traded price formatted in the selected currency
- View a historical BTC market price line chart
- Switch between 6 time periods: 30 days, 60 days, 180 days, 1 year, 2 years, all time

## Tech Stack

| Concern | Library / Version |
|---|---|
| Language | Kotlin 2.2.10 |
| Min SDK | 23 (Android 6.0) |
| Target SDK | 34 (Android 14) |
| Compile SDK | 36 |
| AGP | 9.1.1 |
| Gradle | 9.4.1 |
| JDK | 17 |
| IDE | Android Studio Panda 3 (2025.3.3) |
| Architecture | MVVM + Repository |
| DI | Koin 4.2.0 |
| Networking | Retrofit 2.11.0 + OkHttp 4.12.0 + Gson |
| Local DB | Room 2.7.2 |
| UI | Jetpack Compose + Material3 |
| Compose BOM | 2025.12.01 |
| Charts | Vico 2.4.3 (compose-m3) |
| Async | Kotlin Coroutines 1.9.0 |
| Navigation | Navigation Compose 2.9.0 |
| Annotation Processing | KSP 2.3.6 |
| Testing | JUnit 4.13.2, Mockito-Kotlin 5.2.1, Robolectric 4.13, Kotest Property 5.9.1, Roborazzi 1.59.0 |

## Package Structure

```
com.osias.blockchain
├── BlockchainGraphApplication.kt   # Application entry point, starts Koin
├── common/
│   ├── converter/                  # Room TypeConverters (Date, ChartPeriod)
│   └── utils/                      # DateUtil, EnumUtils
├── model/
│   ├── entity/                     # Room entities: Chart, ChartPoint, CurrencyValue
│   ├── enumeration/                # ChartPeriod, CurrencyEnum
│   ├── local/                      # BancoLocal (RoomDatabase) + DAOs
│   ├── remote/                     # Retrofit Service interface + EnumRetrofitConverterFactory
│   └── repository/                 # ChartRepository, CurrencyRepository, BaseRepository, DateProvider
├── module/                         # Koin: AppModule (all DI definitions)
├── ui/
│   ├── theme/                      # BlockchainGraphTheme (MaterialTheme wrapper)
│   ├── screen/                     # CurrencyScreen composable
│   ├── component/                  # CoinPickerBottomSheet, PeriodSelector, PriceChart
│   └── navigation/                 # AppNavGraph, Routes
├── view/
│   └── activity/                   # MainActivity (ComponentActivity + setContent)
└── viewmodel/                      # BaseViewModel, CurrencyViewModel, CurrencyUiState
```

## API Endpoints Used

```
GET https://blockchain.info/ticker
  → Map<String, CurrencyValue>   (current exchange rates for all currencies)

GET https://blockchain.info/charts/market-price?format=json&timespan={period}
  → Chart (with nested ChartPoint list)
```

The base URL is set via `BuildConfig.SERVER_URL` and varies per build variant:
- `debug` / `staging`: configurable
- `release`: configurable

## Caching Strategy

- **Currency rates**: cached per hour. `DateUtil.stripMinutes()` strips minutes/seconds from the timestamp before querying the DB. A new network call is only made if no record exists for the current hour.
- **Chart data**: cached per period per day. A new network call is only made if no chart record exists for today's date + the requested period.

## Database

Room database named `local_storage`, version 1.

Tables:
- `chart` — composite PK: `(id: Date, period: ChartPeriod)`
- `chart_point` — composite PK: `(x, y, chart_id, chart_period)`, FK → `chart` with CASCADE delete/update
- `currency` — composite PK: `(time: Date, currency_symbol: String)`

Type converters:
- `DateConverter`: `Date ↔ Long` — strips minutes before storing (hourly granularity)
- `ChartPeriodConverter`: `ChartPeriod ↔ String` (enum name)

## Supported Currencies

22 fiat currencies via `CurrencyEnum`:
USD, AUD, BRL, CAD, CHF, CLP, CNY, DKK, EUR, GBP, HKD, INR, ISK, JPY, KRW, NZD, PLN, RUB, SEK, SGD, THB, TWD

## Chart Periods

Defined in `ChartPeriod` enum with `@SerializedName` for Retrofit serialization:

| Enum | API value |
|---|---|
| ONE_MONTH | `30days` |
| TWO_MONTHS | `60days` |
| SIX_MONTHS | `180days` |
| ONE_YEAR | `1year` |
| TWO_YEARS | `2years` |
| ALL_TIME | `all` |

## Running Tests

```bash
# Unit tests (JVM) — includes Robolectric composable tests and property tests
./gradlew test

# Instrumented tests (device/emulator)
./gradlew connectedAndroidTest

# Record Roborazzi screenshot baselines
./gradlew recordRoborazziDebug

# Verify screenshots against recorded baselines
./gradlew verifyRoborazziDebug
```

Test files are under `app/src/test/java/com/osias/blockchain/`:
- `repository/CurrencyRepositoryTest.kt`
- `repository/ChartRepositoryTest.kt`
- `viewmodel/CurrencyViewModelTest.kt`
- `ui/CurrencyScreenTest.kt`
- `ui/CoinPickerBottomSheetTest.kt`
- `ui/PeriodSelectorTest.kt`
- `ui/PriceChartTest.kt`
- `ui/CurrencyScreenScreenshotTest.kt`
- `ui/CoinPickerScreenshotTest.kt`
- `ui/PeriodSelectorScreenshotTest.kt`
- `ui/PriceChartScreenshotTest.kt`
- `property/CurrencyCacheFreshnessPropertyTest.kt`
- `property/ChartCacheFreshnessPropertyTest.kt`
- `property/CurrencyScreenPropertyTest.kt`
- `property/CoinPickerPropertyTest.kt`
- `property/PeriodSelectorPropertyTest.kt`
- `property/PriceChartPropertyTest.kt`
- `property/CurrencyViewModelPropertyTest.kt`

Screenshot baselines are stored under `app/src/test/snapshots/`.
