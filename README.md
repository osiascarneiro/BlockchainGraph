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
| Language | Kotlin 2.2.0 |
| Architecture | MVVM + Repository pattern |
| DI | Koin 4.2.0 |
| Networking | Retrofit 2.11.0 + OkHttp 4.12.0 |
| Local DB | Room 2.7.2 (KSP 2.3.6) |
| Charts | MPAndroidChart 3.1.0 |
| Async | Kotlin Coroutines 1.9.0 |
| Navigation | Android Navigation Component 2.7.7 |

## Requirements

- Android Studio Panda 3 (2025.3.3) or newer
- Android SDK 36 (compileSdk), targetSdk 34
- Min SDK: 21 (Android 5.0 Lollipop)
- Kotlin 2.2.0+
- Gradle 9.4.1+
- JDK 17

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/<your-username>/BlockchainGraph.git
   ```

2. Open the project in Android Studio.

3. Sync Gradle dependencies.

4. Run on an emulator or physical device (API 21+).

> The app uses `https://blockchain.info/` as the default API base URL. No API key is required.

## Build Variants

| Variant | Base URL |
|---|---|
| `debug` | `https://blockchain.info/` |
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
├── viewmodel/           # CurrencyViewModel, BaseViewModel
├── view/
│   ├── activity/        # MainActivity
│   ├── fragment/        # CurrencyFragment, BaseFragment
│   └── dialog/          # CoinPickerDialog
├── module/              # Koin AppModule
└── common/              # Converters and utilities
```

## Architecture

The app follows **MVVM** with a clean repository layer:

```
View (Fragment/Activity)
    ↕ LiveData / Coroutines
ViewModel
    ↕ suspend functions
Repository
    ↕                ↕
Remote (Retrofit)   Local (Room)
```

Data is fetched from the network and cached locally. Currency data refreshes hourly; chart data is cached per period per day.

## API

Uses the public [Blockchain.info API](https://www.blockchain.com/explorer/api/exchange_rates_api):

- `GET /ticker` — current exchange rates for all currencies
- `GET /charts/market-price?timespan={period}&format=json` — historical market price chart

## Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

Test dependencies include JUnit 4, Mockito-Kotlin, AndroidX Test, and Espresso.

## License

This project is open source. Add your license here.
