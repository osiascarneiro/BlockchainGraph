# BlockchainGraph

An Android app for tracking real-time Bitcoin exchange rates and historical price charts across 22+ currencies, powered by the [blockchain.info](https://blockchain.info) public API.

## Features

- Real-time BTC exchange rates (buy, sell, last price)
- Interactive line charts with historical price data
- 22+ supported currencies (USD, EUR, GBP, JPY, BRL, and more)
- Time period selection: 30 days, 60 days, 180 days, 1 year, 2 years, all-time
- Offline-first with local caching (Room database)

## Screenshots

> _Add screenshots here_

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.2.10 |
| Architecture | MVVM + Repository pattern |
| UI | Jetpack Compose + Material3 (Compose BOM 2025.12.01) |
| DI | Koin 4.2.0 |
| Networking | Retrofit 2.11.0 + OkHttp 4.12.0 |
| Local DB | Room 2.7.2 (KSP 2.3.6) |
| Charts | Vico 2.4.3 (compose-m3) |
| Async | Kotlin Coroutines 1.9.0 |
| Navigation | Navigation 3 (navigation3-runtime + navigation3-ui 1.1.1) |

## Requirements

- Android Studio Panda 3 (2025.3.3) or newer
- Android SDK 36 (compileSdk), targetSdk 34
- Min SDK: 23 (Android 6.0 Marshmallow)
- Kotlin 2.2.10+
- Gradle 9.4.1+
- JDK 17

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/<your-username>/BlockchainGraph.git
   ```

2. Open the project in Android Studio.

3. Sync Gradle dependencies.

4. Run on an emulator or physical device (API 23+).

> The app uses `https://api.blockchain.info/` as the default API base URL. No API key is required.

## Build Variants

| Variant | Base URL |
|---|---|
| `debug` | `https://api.blockchain.info/` |
| `staging` | Configurable in `build.gradle` |
| `release` | Configurable in `build.gradle` |

## Project Structure

```
app/src/main/java/com/osias/blockchain/
├── model/
│   ├── entity/          # Room entities (Chart, ChartPoint, CurrencyValue)
│   ├── enumeration/     # ChartPeriod, CurrencyEnum
│   ├── local/           # Room database and DAOs
│   ├── remote/          # Retrofit service interface
│   └── repository/      # ChartRepository, CurrencyRepository
├── viewmodel/           # CurrencyViewModel, BaseViewModel, CurrencyUiState
├── ui/
│   ├── screen/          # CurrencyScreen (Compose)
│   ├── component/       # CoinPickerBottomSheet, PeriodSelector, PriceChart
│   ├── navigation/      # AppNavGraph (Nav3 NavDisplay + CurrencyKey)
│   └── theme/           # BlockchainGraphTheme
├── view/
│   └── activity/        # MainActivity
├── module/              # Koin AppModule
└── common/              # Converters and utilities
```

## Architecture

The app follows **MVVM** with a clean repository layer:

```
View (Compose screens)
    ↕ StateFlow / collectAsStateWithLifecycle
ViewModel
    ↕ suspend functions
Repository
    ↕                ↕
Remote (Retrofit)   Local (Room)
```

Navigation uses Jetpack Navigation 3 with type-safe `NavKey` objects and `NavDisplay`. The back stack is managed as an explicit `SnapshotStateList` via `rememberNavBackStack`. Data is fetched from the network and cached locally — currency data refreshes hourly, chart data is cached per period per day.

## API

Uses the public [Blockchain.info API](https://www.blockchain.com/explorer/api/exchange_rates_api):

- `GET /ticker` — current exchange rates for all currencies
- `GET /charts/market-price?timespan={period}&format=json` — historical market price chart

## Testing

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

Test dependencies include JUnit 4, Mockito-Kotlin, Robolectric 4.13, Kotest Property 5.9.1, and Roborazzi 1.59.0.

## License

This project is open source. Add your license here.
