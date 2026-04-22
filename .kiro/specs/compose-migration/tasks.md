# Implementation Plan: Compose Migration

## Overview

Migrate the entire view layer of BlockchainGraph from XML layouts + View Binding to Jetpack Compose. The migration proceeds bottom-up: build config first, then shared infrastructure (theme, ViewModel), then individual composables, then navigation wiring, then deletion of legacy code, then tests, and finally documentation.

All new Compose UI files live under `com.osias.blockchain.ui/`. The Repository layer, Room database, Retrofit service, Koin module, and all model/entity/enumeration classes are not modified.

## Tasks

- [x] 1. Update Gradle build configuration for Compose
  - In `app/build.gradle`, apply the `org.jetbrains.kotlin.plugin.compose` and `io.github.takahirom.roborazzi` plugins
  - Replace `viewBinding true` with `compose true` under `buildFeatures`; remove any `composeOptions` block
  - Add the Compose BOM (`androidx.compose:compose-bom:2025.12.01`) as a `platform` import; add `ui`, `material3`, `ui-tooling-preview` without version strings; add `debugImplementation "androidx.compose.ui:ui-tooling"`
  - Add versioned dependencies outside the BOM: `activity-compose:1.10.0`, `lifecycle-runtime-compose:2.9.0`, `navigation-compose:2.9.0`, `vico compose-m3:2.4.3`
  - Remove `com.github.PhilJay:MPAndroidChart`, `navigation-fragment-ktx`, `navigation-ui-ktx`, `appcompat`, `constraintlayout`, `lifecycle-livedata-ktx`
  - Add test dependencies: `ui-test-junit4` (BOM-managed), `robolectric:4.13`, `kotest-property:5.9.1`, `roborazzi-compose:1.52.0`
  - Create `app/src/test/resources/robolectric.properties` with `nativeGraphicsMode=native` and `sdk=34`
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 6.6, 8.3, 11.1, 12.1_

- [x] 2. Implement BlockchainGraphTheme
  - Create `app/src/main/java/com/osias/blockchain/ui/theme/BlockchainGraphTheme.kt`
  - Implement `BlockchainGraphTheme` composable wrapping `MaterialTheme` with light/dark color schemes derived from the app's existing color palette
  - Support `isSystemInDarkTheme()` for dark mode; apply `dynamicLightColorScheme` / `dynamicDarkColorScheme` on Android 12+ (API ≥ 31)
  - _Requirements: 10.1, 10.2, 10.3, 10.4_

- [x] 3. Migrate BaseViewModel and CurrencyViewModel to StateFlow
  - [x] 3.1 Migrate `BaseViewModel` — replace `error: MutableLiveData<String>` with `private val _error = MutableStateFlow<String?>(null)` and `val error: StateFlow<String?> = _error.asStateFlow()`
    - File: `app/src/main/java/com/osias/blockchain/viewmodel/BaseViewModel.kt`
    - _Requirements: 7.1_

  - [x] 3.2 Create `CurrencyUiState` data class
    - Create `app/src/main/java/com/osias/blockchain/viewmodel/CurrencyUiState.kt`
    - Fields: `isLoading: Boolean = true`, `formattedPrice: String = ""`, `chartPoints: List<ChartPoint> = emptyList()`, `errorMessage: String? = null`
    - _Requirements: 7.2_

  - [x] 3.3 Migrate `CurrencyViewModel` to StateFlow
    - Replace `MutableLiveData` fields with `MutableStateFlow` backing properties + public `asStateFlow()` for `coin`, `period`, and `uiState`
    - Add `selectCoin(coin: CurrencyEnum)` and `selectPeriod(period: ChartPeriod)` mutator functions
    - Add `init` block using `viewModelScope.launch { combine(_coin, _period) { ... }.collectLatest { loadData(...) } }`
    - Implement `private suspend fun loadData(coin, period)` with `try/catch` emitting `CurrencyUiState` for loading, loaded, and error states
    - Replace all `GlobalScope.launch` with `viewModelScope.launch`; preserve `formatCurrency` and `formatUnixDate` logic unchanged
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6_

- [x] 4. Implement PeriodSelector composable
  - Create `app/src/main/java/com/osias/blockchain/ui/component/PeriodSelector.kt`
  - Signature: `fun PeriodSelector(selectedPeriod: ChartPeriod, onPeriodSelected: (ChartPeriod) -> Unit, modifier: Modifier = Modifier)`
  - Render all six `ChartPeriod` values as `FilterChip` or `TextButton` in a `Row` that fills screen width; visually distinguish the selected period
  - Apply `Modifier.testTag(PeriodSelectorTags.periodButton(period))` to each button; apply `semantics { testTagsAsResourceId = true }` at the `Row` root tagged with `PeriodSelectorTags.ROOT`
  - Define `object PeriodSelectorTags` in the same file with `ROOT` and `fun periodButton(period: ChartPeriod)` constants
  - Handle narrow screens (min SDK 23) by abbreviating labels or using `horizontalScroll`
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 12.9, 12.10_

- [x] 5. Implement PriceChart composable
  - Create `app/src/main/java/com/osias/blockchain/ui/component/PriceChart.kt`
  - Signature: `fun PriceChart(points: List<ChartPoint>, formatXLabel: (Float) -> String, formatYLabel: (Double) -> String, modifier: Modifier = Modifier)`
  - Use Vico's `CartesianChartHost` with a `LineCartesianLayer`; populate a `CartesianChartModelProducer` using `pointX` as x-value and `pointY` as y-value for each `ChartPoint`
  - Wire `formatXLabel` to the bottom axis `AxisValueFormatter` and `formatYLabel` to the start axis `AxisValueFormatter`
  - Render an empty chart without crashing when `points` is empty
  - Apply `Modifier.testTag(PriceChartTags.ROOT)` at the root; define `object PriceChartTags` with `ROOT` constant in the same file; enable `testTagsAsResourceId = true`
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 12.9, 12.10_

- [x] 6. Implement CoinPickerBottomSheet composable
  - Create `app/src/main/java/com/osias/blockchain/ui/component/CoinPickerBottomSheet.kt`
  - Signature: `fun CoinPickerBottomSheet(selectedCoin: CurrencyEnum, onCoinSelected: (CurrencyEnum) -> Unit, onDismiss: () -> Unit)`
  - Use `ModalBottomSheet` from Material3; display all 22 `CurrencyEnum` entries as selectable rows; visually highlight the currently selected entry
  - Each row invokes `onCoinSelected(coin)` then `onDismiss()` when tapped
  - Apply `Modifier.testTag(CoinPickerTags.coinItem(coin))` to each row; apply `Modifier.testTag(CoinPickerTags.ROOT)` + `testTagsAsResourceId = true` at the sheet root
  - Define `object CoinPickerTags` with `ROOT` and `fun coinItem(coin: CurrencyEnum)` in the same file
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 12.9, 12.10_

- [x] 7. Implement CurrencyScreen composable
  - Create `app/src/main/java/com/osias/blockchain/ui/screen/CurrencyScreen.kt`
  - Signature: `fun CurrencyScreen(viewModel: CurrencyViewModel = koinViewModel())`
  - Collect `viewModel.coin`, `viewModel.period`, and `viewModel.uiState` via `collectAsState()`
  - Use a `Scaffold` with `modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)` at the root; place `PeriodSelector` in `bottomBar`
  - In the content area: show a currency-symbol button (tagged `CurrencyScreenTags.SELECT_COIN_BUTTON`) that sets `var showPicker by remember { mutableStateOf(false) }`; show "Last quote" label (tagged `CurrencyScreenTags.LAST_QUOTE_LABEL`); show formatted price (tagged `CurrencyScreenTags.PRICE_VALUE`) or `CircularProgressIndicator` (tagged `CurrencyScreenTags.LOADING_INDICATOR`) based on `uiState`; show `PriceChart` filling remaining vertical space
  - Show `CoinPickerBottomSheet` when `showPicker == true`; pass `viewModel::selectCoin` as `onCoinSelected` and `{ showPicker = false }` as `onDismiss`
  - Use `LaunchedEffect` keyed on coin and period for any side effects; no `GlobalScope` or manually created coroutine scopes inside the composable
  - Define `object CurrencyScreenTags` with `SELECT_COIN_BUTTON`, `LAST_QUOTE_LABEL`, `PRICE_VALUE`, `LOADING_INDICATOR` in the same file; enable `testTagsAsResourceId = true` at the semantic root
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.9, 3.10, 12.9, 12.10, 13.2, 13.3, 13.5, 13.6, 13.7_

- [x] 8. Implement AppNavGraph and migrate MainActivity
  - [x] 8.1 Create `app/src/main/java/com/osias/blockchain/ui/navigation/AppNavGraph.kt`
    - Define `object Routes { const val CURRENCY = "currency" }`
    - Implement `fun AppNavGraph(navController: NavHostController = rememberNavController())` with a single `composable(Routes.CURRENCY) { CurrencyScreen() }` destination
    - _Requirements: 2.2_

  - [x] 8.2 Migrate `MainActivity`
    - Change superclass from `AppCompatActivity` to `ComponentActivity`
    - Call `enableEdgeToEdge()` before `setContent { }`
    - Replace `setContentView(R.layout.activity_main)` with `setContent { BlockchainGraphTheme { AppNavGraph() } }`
    - Remove `AndroidManifest.xml` `android:screenOrientation` attribute if present
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 10.2, 13.1, 13.4_

- [x] 9. Checkpoint — verify the app compiles and launches
  - Ensure `./gradlew assembleDebug` succeeds with the new Compose UI wired up alongside the still-present legacy files
  - Ensure all tests pass, ask the user if questions arise

- [x] 10. Delete legacy view files and XML resources
  - Delete Kotlin source files: `BaseFragment.kt`, `CurrencyFragment.kt`, `CoinPickerDialog.kt`
  - Delete XML layout files: `activity_main.xml`, `fragment_actual_currency.xml`, `dialog_coin_picker.xml`
  - Delete XML drawable files: `radio_checked_state.xml`, `radio_normal_state.xml`, `radio_selector.xml`
  - Delete navigation resource: `res/navigation/nav_graph.xml`
  - Remove `viewBinding true` from `buildFeatures` in `app/build.gradle`
  - Verify zero remaining references to `R.layout.*`, `R.drawable.radio_*`, `FragmentActualCurrencyBinding`, `DialogCoinPickerBinding`, `GlobalScope`, `AppCompatActivity`, `NavHostFragment`, `DialogFragment`
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 9.1, 9.2, 9.3, 9.4_

- [x] 11. Write Robolectric composable unit tests
  - [x] 11.1 Write `CurrencyScreenTest`
    - Create `app/src/test/java/com/osias/blockchain/ui/CurrencyScreenTest.kt`
    - Annotate with `@RunWith(RobolectricTestRunner::class)`; use `createComposeRule()`
    - Test: currency symbol button is displayed; "Last quote" label is displayed; price value is displayed when `uiState` has a loaded price; loading indicator is displayed when `uiState.isLoading = true`
    - Use fake `StateFlow` values — no real repositories or network calls
    - Use `onNodeWithTag()` exclusively for all node lookups
    - _Requirements: 12.2, 12.3, 12.7, 12.8, 12.9_

  - [x] 11.2 Write `CoinPickerBottomSheetTest`
    - Create `app/src/test/java/com/osias/blockchain/ui/CoinPickerBottomSheetTest.kt`
    - Test: all 22 `CurrencyEnum` entries are displayed; selected entry is visually marked; tapping an entry invokes the callback with the correct `CurrencyEnum` value
    - _Requirements: 12.2, 12.4, 12.7, 12.8, 12.9_

  - [x] 11.3 Write `PeriodSelectorTest`
    - Create `app/src/test/java/com/osias/blockchain/ui/PeriodSelectorTest.kt`
    - Test: all six `ChartPeriod` buttons are displayed; selected button is visually distinguished; tapping a button invokes the callback with the correct `ChartPeriod` value
    - _Requirements: 12.2, 12.5, 12.7, 12.8, 12.9_

  - [x] 11.4 Write `PriceChartTest`
    - Create `app/src/test/java/com/osias/blockchain/ui/PriceChartTest.kt`
    - Test: renders without crashing on an empty `ChartPoint` list; renders without crashing on a non-empty `ChartPoint` list
    - _Requirements: 12.2, 12.6, 12.7, 12.8, 12.9_

- [x] 12. Write property-based tests
  - [x] 12.1 Write property test for Property 1 — currency button displays selected coin's symbol
    - Create `app/src/test/java/com/osias/blockchain/property/CurrencyScreenPropertyTest.kt`
    - Use `checkAll(Arb.enum<CurrencyEnum>())` to verify the `SELECT_COIN_BUTTON` node displays the coin's `symbol` for every `CurrencyEnum` value
    - **Property 1: Currency button displays the selected coin's symbol**
    - **Validates: Requirements 3.3**

  - [x] 12.2 Write property test for Property 2 — CoinPickerBottomSheet displays all entries and marks selected
    - Add to `app/src/test/java/com/osias/blockchain/property/CoinPickerPropertyTest.kt`
    - Use `checkAll(Arb.enum<CurrencyEnum>())` to verify all 22 entries appear and the selected entry is marked for every possible `selectedCoin`
    - **Property 2: CoinPickerBottomSheet displays all entries and marks the selected one**
    - **Validates: Requirements 4.2, 4.3**

  - [x] 12.3 Write property test for Property 3 — CoinPickerBottomSheet callback receives tapped coin
    - Add to `CoinPickerPropertyTest.kt`
    - Use `checkAll(Arb.enum<CurrencyEnum>())` to verify `onCoinSelected` is invoked with the exact tapped `CurrencyEnum`
    - **Property 3: CoinPickerBottomSheet callback receives the tapped coin**
    - **Validates: Requirements 4.4**

  - [x] 12.4 Write property test for Property 4 — PeriodSelector displays all periods and marks selected
    - Create `app/src/test/java/com/osias/blockchain/property/PeriodSelectorPropertyTest.kt`
    - Use `checkAll(Arb.enum<ChartPeriod>())` to verify all six buttons appear and the selected button is distinguished for every `ChartPeriod`
    - **Property 4: PeriodSelector displays all periods and marks the selected one**
    - **Validates: Requirements 5.1, 5.2**

  - [x] 12.5 Write property test for Property 5 — PeriodSelector callback receives tapped period
    - Add to `PeriodSelectorPropertyTest.kt`
    - Use `checkAll(Arb.enum<ChartPeriod>())` to verify `onPeriodSelected` is invoked with the exact tapped `ChartPeriod`
    - **Property 5: PeriodSelector callback receives the tapped period**
    - **Validates: Requirements 5.3**

  - [x] 12.6 Write property test for Property 6 — PriceChart renders without crashing for any ChartPoint list
    - Create `app/src/test/java/com/osias/blockchain/property/PriceChartPropertyTest.kt`
    - Use `checkAll(Arb.list(Arb.bind(...) { x, y -> ChartPoint(x, y) }))` to verify no exception is thrown for any list including empty
    - **Property 6: PriceChart renders without crashing for any list of ChartPoints**
    - **Validates: Requirements 6.2, 6.5**

  - [x] 12.7 Write property test for Property 7 — ViewModel emits new uiState when coin or period changes
    - Create `app/src/test/java/com/osias/blockchain/property/CurrencyViewModelPropertyTest.kt`
    - Use `checkAll(Arb.enum<CurrencyEnum>(), Arb.enum<ChartPeriod>())` with a fake repository to verify `uiState` emits at least one new value after `selectCoin` / `selectPeriod`
    - **Property 7: ViewModel emits new uiState when coin or period changes**
    - **Validates: Requirements 7.3**

  - [x] 12.8 Write property test for Property 8 — ViewModel emits errorMessage on failure
    - Add to `CurrencyViewModelPropertyTest.kt`
    - Use a fake repository that always throws; use `checkAll` over exception message strings to verify `uiState.errorMessage` is non-null and `isLoading` is false
    - **Property 8: ViewModel emits errorMessage on failure**
    - **Validates: Requirements 7.5**

- [x] 13. Checkpoint — ensure all tests pass
  - Run `./gradlew test` and confirm all unit and property tests pass
  - Ensure all tests pass, ask the user if questions arise

- [ ] 14. Write Roborazzi screenshot tests
  - [x] 14.1 Write screenshot test for `CurrencyScreen`
    - Create `app/src/test/java/com/osias/blockchain/ui/CurrencyScreenScreenshotTest.kt`
    - Capture loaded state: `uiState` with `isLoading = false` and a non-empty `formattedPrice`
    - Capture loading state: `uiState` with `isLoading = true`
    - Wrap in `BlockchainGraphTheme`; call `composeTestRule.onRoot().captureRoboImage(...)` for each state
    - Store reference images under `app/src/test/snapshots/`
    - _Requirements: 11.2, 11.3, 11.4, 11.5, 11.6_

  - [x] 14.2 Write screenshot test for `CoinPickerBottomSheet`
    - Add to `app/src/test/java/com/osias/blockchain/ui/CoinPickerScreenshotTest.kt`
    - Capture default state (sheet open, first coin selected) and a state with a non-default coin selected
    - _Requirements: 11.3, 11.4, 11.5, 11.6_

  - [x] 14.3 Write screenshot test for `PeriodSelector`
    - Add to `app/src/test/java/com/osias/blockchain/ui/PeriodSelectorScreenshotTest.kt`
    - Capture default state (`ONE_MONTH` selected) and a state with a different period selected
    - _Requirements: 11.3, 11.4, 11.5, 11.6_

  - [x] 14.4 Write screenshot test for `PriceChart`
    - Add to `app/src/test/java/com/osias/blockchain/ui/PriceChartScreenshotTest.kt`
    - Capture non-empty chart state and empty chart state
    - _Requirements: 11.3, 11.4, 11.5, 11.6_

- [x] 15. Update post-migration steering documentation
  - Update `.kiro/steering/project-context.md`: replace MPAndroidChart with Vico 2.4.3 in the tech stack table; add Compose BOM 2025.12.01, navigation-compose 2.9.0, Roborazzi 1.52.0; remove references to XML layouts, View Binding, `NavHostFragment`, `DialogFragment`; update the package structure section to include the `ui/` tree; add `./gradlew recordRoborazziDebug` and `./gradlew verifyRoborazziDebug` to the Running Tests section
  - Update `.kiro/steering/architecture.md`: replace the View Layer section to describe composable functions, `StateFlow`/`collectAsState`, `LaunchedEffect`, `viewModelScope`; update the layer diagram to show `CurrencyScreen`, `CoinPickerBottomSheet`, `PeriodSelector`, `PriceChart`; replace the "Adding a New Screen" checklist with the Compose workflow (create composable in `ui/screen/`, define `TestTags` object, add route to `AppNavGraph`, write Robolectric + Roborazzi tests)
  - Update `.kiro/steering/code-style.md`: remove the View Binding section; add a Compose section covering `collectAsState()`, `LaunchedEffect`, no `!!` in composables, `Modifier.testTag()` + `testTagsAsResourceId = true`, `TestTags` objects co-located with composables, `remember { mutableStateOf(false) }` for transient UI state
  - _Requirements: 14.1, 14.2, 14.3, 14.4_

## Notes

- Tasks marked with `*` are optional and can be skipped for a faster MVP
- Task 9 is a compile checkpoint — run it before deleting legacy files in Task 10
- Property tests (Task 12) use Kotest's `checkAll` / `Arb` generators with a minimum of 100 iterations per property
- Screenshot tests (Task 14) require `./gradlew recordRoborazziDebug` to generate baselines before `verifyRoborazziDebug` can be used for regression detection
- The `CurrencyViewModelTest.kt` existing test will need updating to collect `StateFlow` instead of observing `LiveData` — this is a natural consequence of Task 3 and should be done as part of that task
