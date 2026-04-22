# Requirements: BlockchainGraph

## Overview

BlockchainGraph is an Android application that allows users to track real-time Bitcoin exchange rates and visualize historical price data across multiple fiat currencies.

---

## Functional Requirements

### FR-1: Currency Rate Display
- The app shall fetch and display the current BTC exchange rate for a user-selected currency.
- Displayed values shall include the last traded price.
- The rate shall be formatted according to the selected currency's locale and symbol.

### FR-2: Currency Selection
- The user shall be able to select from at least 22 supported fiat currencies.
- Supported currencies: USD, AUD, BRL, CAD, CHF, CLP, CNY, DKK, EUR, GBP, HKD, INR, ISK, JPY, KRW, NZD, PLN, RUB, SEK, SGD, THB, TWD.
- The default currency shall be USD.
- Currency selection shall be presented via a picker dialog.

### FR-3: Historical Price Chart
- The app shall display a line chart of BTC market price over a selected time period.
- The chart shall render data points with X-axis as date and Y-axis as price.
- X-axis labels shall be formatted as `dd/MM/yy`.
- Y-axis labels shall be formatted as currency values matching the selected currency.

### FR-4: Time Period Selection
- The user shall be able to select one of the following chart periods:
  - 30 days
  - 60 days
  - 180 days
  - 1 year
  - 2 years
  - All time
- The default period shall be 30 days.
- Period selection shall be presented as a segmented radio button group.

### FR-5: Offline Caching
- Currency rate data shall be cached locally and refreshed at most once per hour.
- Chart data shall be cached locally per period per day.
- Cached data shall be served when a fresh network request is not needed.

### FR-6: Error Handling
- Network errors shall be propagated via a delegate error callback.
- The app shall not crash on failed API responses.

---

## Non-Functional Requirements

### NFR-1: Performance
- The UI shall remain responsive during network and database operations (async via coroutines).
- Chart rendering shall complete without blocking the main thread.

### NFR-2: Compatibility
- Minimum supported Android version: API 21 (Android 5.0 Lollipop).
- Compile SDK: API 36. Target SDK: API 34 (Android 14).
- Build toolchain: Android Studio Panda 3 (2025.3.3), AGP 9.1.0, Gradle 9.4.1, JDK 17.

### NFR-3: Testability
- Repositories shall accept a `DateProvider` interface to allow date injection in tests.
- ViewModels shall be injectable via Dagger for unit testing.

### NFR-4: Maintainability
- The app shall follow MVVM architecture with a clean separation between View, ViewModel, and Repository layers.
- Dependency injection shall be managed by Koin.

---

## Correctness Properties

### CP-1: Cache Freshness — Currency
> Currency data cached for a given date shall only be reused within the same hour. A new fetch shall occur if no record exists for the current hour.

**Property**: For any two calls to `getValueByCurrency` within the same hour, the second call shall not trigger a network request if the first succeeded.

### CP-2: Cache Freshness — Chart
> Chart data for a given period shall only be fetched once per day. Subsequent calls with the same period and date shall return cached data.

**Property**: For any two calls to `getCharts(period)` on the same date, the second call shall not trigger a network request if the first succeeded.

### CP-3: Chart Point Integrity
> All chart points stored in the database shall reference a valid parent Chart via foreign key. Deleting a Chart shall cascade-delete its associated ChartPoints.

### CP-4: Currency Formatting Consistency
> `formatCurrency(value)` shall always return a string formatted with the currently selected currency symbol, regardless of the device locale.

### CP-5: Period Mapping Completeness
> Every `ChartPeriod` enum value shall map to exactly one radio button in the UI, and every radio button selection shall map to exactly one `ChartPeriod` value.
