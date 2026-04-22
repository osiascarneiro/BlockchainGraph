# Tasks: BlockchainGraph

## Status Legend
- `[ ]` Not started
- `[-]` In progress
- `[x]` Completed

---

## Phase 1: Core Data Layer

- [x] 1. Define Room entities (Chart, ChartPoint, CurrencyValue)
- [x] 2. Implement DAOs (ChartDao, ChartPointDao, CurrencyDao)
- [x] 3. Set up Room database (BancoLocal) with type converters
- [x] 4. Define Retrofit Service interface
- [x] 5. Implement EnumRetrofitConverterFactory for enum serialization

## Phase 2: Repository Layer

- [x] 6. Implement BaseRepository with error delegate
- [x] 7. Implement CurrencyRepository with hourly cache logic
- [x] 8. Implement ChartRepository with daily cache logic per period
- [x] 9. Add DateProvider interface for testable date injection

## Phase 3: Dependency Injection

- [x] 10. Configure AppModule (Retrofit, OkHttp, Room, repositories, ViewModels) with Koin
- [x] 11. Start Koin in BlockchainGraphApplication
- [x] 12. Inject ViewModels in BaseFragment via Koin viewModel delegate

## Phase 4: ViewModel Layer

- [x] 14. Implement BaseViewModel
- [x] 15. Implement CurrencyViewModel with coin and period LiveData
- [x] 16. Add formatCurrency and formatUnixDate helpers to CurrencyViewModel
- [x] 17. Implement ViewModelFactory for Dagger-injected ViewModels

## Phase 5: View Layer

- [x] 18. Implement MainActivity with Navigation Component host
- [x] 19. Implement BaseFragment with ViewModel binding
- [x] 20. Implement CurrencyFragment
  - [x] 20.1 Currency rate label display
  - [x] 20.2 Period segmented radio button group
  - [x] 20.3 MPAndroidChart LineChart rendering
  - [x] 20.4 X-axis date formatting
  - [x] 20.5 Y-axis currency formatting
- [x] 21. Implement CoinPickerDialog with NumberPicker

## Phase 6: Testing

- [x] 22. Unit test CurrencyRepository cache logic
  - [x] 22.1 Test that a second call within the same hour does not trigger a network request
  - [x] 22.2 Test that a call with no cached data triggers a network request
- [x] 23. Unit test ChartRepository cache logic
  - [x] 23.1 Test that a second call for the same period on the same day does not trigger a network request
  - [x] 23.2 Test that chart points are persisted with correct foreign key references
- [x] 24. Unit test CurrencyViewModel
  - [x] 24.1 Test formatCurrency returns correctly formatted string for selected currency
  - [x] 24.2 Test formatUnixDate returns correct date string from Unix timestamp
- [x] 25. Property-based test: CP-1 Cache Freshness — Currency
- [x] 26. Property-based test: CP-2 Cache Freshness — Chart
- [x] 27. Property-based test: CP-5 Period Mapping Completeness

## Phase 7: Polish & Configuration

- [ ] 28. Configure staging and release SERVER_URL in build.gradle
- [ ] 29. Enable ProGuard rules for release build
- [ ] 30. Add app icon and launcher assets
