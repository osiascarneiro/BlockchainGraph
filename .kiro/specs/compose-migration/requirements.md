# Requirements Document

## Introduction

This feature migrates the entire view layer of the BlockchainGraph Android app from XML layouts with View Binding to Jetpack Compose. The migration covers all three XML layout files (`activity_main.xml`, `fragment_actual_currency.xml`, `dialog_coin_picker.xml`), their corresponding Kotlin view classes (`MainActivity`, `CurrencyFragment`, `BaseFragment`, `CoinPickerDialog`), and the build configuration. The ViewModel layer, Repository layer, and all model/data classes remain unchanged. The app must continue to display BTC exchange rates, historical price charts, and support currency and time-period selection after the migration.

## Glossary

- **App**: The BlockchainGraph Android application.
- **Compose_UI**: The Jetpack Compose declarative UI toolkit replacing XML layouts.
- **CurrencyScreen**: The single main screen composable that replaces `CurrencyFragment` and its XML layout.
- **CoinPickerDialog**: The currency-selection dialog, replaced by a `ModalBottomSheet` composable from Material3.
- **PeriodSelector**: The row of period toggle buttons at the bottom of the screen, replacing the XML `RadioGroup`.
- **PriceChart**: The line chart composable using Vico's `CartesianChartHost` with a `LineCartesianLayer`, replacing the XML `LineChart` view. No `AndroidView` wrapper is required.
- **CurrencyViewModel**: The existing ViewModel — unchanged; state is migrated from `LiveData` to `StateFlow`/`collectAsState` for idiomatic Compose observation.
- **View_Binding**: The XML-based view binding system being removed.
- **NavHostFragment**: The Navigation Component host fragment inside `MainActivity`.
- **Compose_Navigation**: The Jetpack Compose Navigation library (`androidx.navigation:navigation-compose`) that replaces the XML nav graph and `NavHostFragment`.

---

## Requirements

### Requirement 1: Gradle Build Configuration

**User Story:** As a developer, I want the project build files updated for Compose, so that the app compiles with Compose enabled and all necessary Compose dependencies are available.

#### Acceptance Criteria

1. THE App SHALL enable `compose = true` under `buildFeatures` in `app/build.gradle`.
2. THE App SHALL apply the `org.jetbrains.kotlin.plugin.compose` Gradle plugin, which is bundled with Kotlin 2.0+ and eliminates the need for a separate `kotlinCompilerExtensionVersion` in a `composeOptions` block.
3. THE App SHALL import the Compose BOM at version `2025.12.01` as the sole version management strategy for all Compose artifacts, which pins `androidx.compose.ui:ui` and `androidx.compose.material3:material3` to the versions defined by that BOM (Compose core 1.10.6, Material3 1.4.x); THE App SHALL additionally declare `androidx.activity:activity-compose` at version `1.10.0`, `androidx.lifecycle:lifecycle-runtime-compose` at version `2.9.0`, `androidx.navigation:navigation-compose` at version `2.9.0`, and `com.patrykandpatrick.vico:compose-m3` at version `2.4.3`; THE App SHALL remove the `com.github.mikephil.charting:MPAndroidChart` dependency; THE App SHALL apply the `io.github.takahirom.roborazzi` Gradle plugin to enable the `recordRoborazziDebug` and `verifyRoborazziDebug` tasks.
4. THE App SHALL NOT declare individual version strings on any `androidx.compose.*` artifact — all Compose artifact versions SHALL be managed exclusively through the BOM import.
5. WHEN `viewBinding true` is removed from `buildFeatures`, THE App SHALL still compile without errors.
6. THE App SHALL declare a `debugImplementation` dependency on `androidx.compose.ui:ui-tooling` for layout inspection support.

---

### Requirement 2: MainActivity Migration

**User Story:** As a developer, I want `MainActivity` to host the Compose UI instead of an XML layout, so that the activity is the single entry point for the Compose navigation graph.

#### Acceptance Criteria

1. THE MainActivity SHALL call `setContent { }` with a Compose theme wrapper instead of `setContentView(R.layout.activity_main)`.
2. THE MainActivity SHALL host the top-level `NavHost` composable inside `setContent`, replacing the XML `NavHostFragment`.
3. WHEN `activity_main.xml` is deleted, THE App SHALL compile and launch without referencing `R.layout.activity_main`.
4. THE MainActivity SHALL extend `ComponentActivity` instead of `AppCompatActivity` after the migration.

---

### Requirement 3: CurrencyScreen Composable

**User Story:** As a developer, I want a `CurrencyScreen` composable that replaces `CurrencyFragment` and its XML layout, so that the main screen is fully implemented in Compose.

#### Acceptance Criteria

1. THE CurrencyScreen SHALL accept a `CurrencyViewModel` parameter injected via Koin's `koinViewModel()`.
2. THE CurrencyScreen SHALL observe `CurrencyViewModel` state and recompose when `coin` or `period` changes.
3. THE CurrencyScreen SHALL display a button showing the currently selected currency symbol that, when tapped, opens the `CoinPickerDialog`.
4. THE CurrencyScreen SHALL display the label "Last quote" and the formatted BTC price for the selected currency.
5. WHEN the currency or period changes, THE CurrencyScreen SHALL trigger a data reload and update the displayed price and chart.
6. THE CurrencyScreen SHALL display the `PriceChart` composable occupying the available vertical space between the price card and the `PeriodSelector`.
7. THE CurrencyScreen SHALL display the `PeriodSelector` anchored to the bottom of the screen.
8. WHEN `fragment_actual_currency.xml` is deleted, THE App SHALL compile without referencing `R.layout.fragment_actual_currency` or `FragmentActualCurrencyBinding`.
9. THE CurrencyScreen SHALL observe CurrencyViewModel state exclusively via `collectAsState()` — no `GlobalScope.launch` or manually created coroutine scope SHALL exist inside the composable.
10. WHEN the selected coin or period changes, THE CurrencyScreen SHALL trigger any side effects using `LaunchedEffect` keyed on the relevant state values.

---

### Requirement 4: CoinPickerDialog Migration

**User Story:** As a developer, I want the currency picker implemented as a `ModalBottomSheet` composable, so that the `DialogFragment` and its XML layout are fully removed.

#### Acceptance Criteria

1. THE CoinPickerDialog SHALL be implemented as a `ModalBottomSheet` composable from Material3, not as a `DialogFragment` subclass.
2. THE CoinPickerDialog SHALL display all 22 `CurrencyEnum` entries as selectable options within the bottom sheet content.
3. THE CoinPickerDialog SHALL highlight the currently selected `CurrencyEnum` entry.
4. WHEN the user selects a currency, THE CoinPickerDialog SHALL invoke a callback lambda with the selected `CurrencyEnum` value.
5. WHEN the user confirms the selection, THE CoinPickerDialog SHALL dismiss the `ModalBottomSheet`.
6. WHEN `dialog_coin_picker.xml` is deleted, THE App SHALL compile without referencing `R.layout.dialog_coin_picker` or `DialogCoinPickerBinding`.

---

### Requirement 5: PeriodSelector Composable

**User Story:** As a developer, I want a `PeriodSelector` composable that replaces the XML `RadioGroup`, so that the time-period toggle is implemented in Compose.

#### Acceptance Criteria

1. THE PeriodSelector SHALL display one toggle button for each of the six `ChartPeriod` values: `ONE_MONTH`, `TWO_MONTHS`, `SIX_MONTHS`, `ONE_YEAR`, `TWO_YEARS`, `ALL_TIME`.
2. THE PeriodSelector SHALL visually distinguish the currently selected `ChartPeriod` from the unselected ones.
3. WHEN the user taps a period button, THE PeriodSelector SHALL invoke a callback lambda with the tapped `ChartPeriod` value.
4. THE PeriodSelector SHALL display all six buttons in a single horizontal row that fills the screen width.
5. IF the screen width is insufficient to display all six labels without truncation, THE PeriodSelector SHALL abbreviate or scroll the labels to remain usable on Min SDK 23 devices.

---

### Requirement 6: PriceChart Composable

**User Story:** As a developer, I want a `PriceChart` composable that uses Vico's native Compose chart API, so that the chart is rendered without an `AndroidView` wrapper and without a dependency on MPAndroidChart.

#### Acceptance Criteria

1. THE PriceChart SHALL use Vico's `CartesianChartHost` composable with a `LineCartesianLayer` — no `AndroidView` wrapper is required.
2. WHEN a non-empty list of `ChartPoint` values is provided, THE PriceChart SHALL populate a `CartesianChartModelProducer` with one entry per `ChartPoint` using `pointX` as the x-value and `pointY` as the y-value.
3. THE PriceChart SHALL format x-axis labels using the same `formatUnixDate` logic currently in `CurrencyFragment`, applied via a Vico `AxisValueFormatter` on the bottom axis.
4. THE PriceChart SHALL format y-axis labels using the same `formatCurrency` logic currently in `CurrencyFragment`, applied via a Vico `AxisValueFormatter` on the start axis.
5. WHEN an empty list of `ChartPoint` values is provided, THE PriceChart SHALL render an empty chart without crashing.
6. THE App SHALL replace the `com.github.mikephil.charting:MPAndroidChart` dependency with `com.patrykandpatrick.vico:compose-m3` at version `2.4.3` in `app/build.gradle`.

---

### Requirement 7: ViewModel State Adaptation

**User Story:** As a developer, I want `CurrencyViewModel` state exposed as `StateFlow` in addition to or instead of `LiveData`, so that Compose screens can observe state idiomatically using `collectAsState`.

#### Acceptance Criteria

1. THE CurrencyViewModel SHALL expose `coin` and `period` as `StateFlow<CurrencyEnum>` and `StateFlow<ChartPeriod>` respectively, preserving their default values (`US_DOLLAR` and `ONE_MONTH`).
2. THE CurrencyViewModel SHALL expose a `uiState: StateFlow<CurrencyUiState>` that holds the current price string, chart points list, and loading/error state.
3. WHEN `coin` or `period` changes, THE CurrencyViewModel SHALL automatically trigger a data reload and emit a new `CurrencyUiState`.
4. THE CurrencyViewModel SHALL replace `GlobalScope.launch` usages with `viewModelScope.launch` for all coroutine dispatching.
5. IF a network or database error occurs, THE CurrencyViewModel SHALL emit a `CurrencyUiState` with a non-null `errorMessage` field.
6. THE CurrencyViewModel SHALL remain registered in `appModule` with `viewModel { CurrencyViewModel(get(), get()) }` — no changes to the Koin module declaration are required beyond this.

---

### Requirement 8: Fragment and BaseFragment Removal

**User Story:** As a developer, I want `BaseFragment` and `CurrencyFragment` removed from the codebase, so that no Fragment-based view classes remain after the migration.

#### Acceptance Criteria

1. WHEN `CurrencyFragment` is deleted, THE App SHALL compile and navigate to the currency screen via Compose Navigation without referencing `CurrencyFragment`.
2. WHEN `BaseFragment` is deleted, THE App SHALL compile without any remaining subclass references.
3. THE App SHALL remove the `androidx.navigation:navigation-fragment-ktx` and `androidx.navigation:navigation-ui-ktx` dependencies from `app/build.gradle` after the migration, replacing them with `androidx.navigation:navigation-compose`.
4. THE App SHALL remove the `res/navigation/nav_graph.xml` file after the migration.

---

### Requirement 9: XML Layout and Drawable Cleanup

**User Story:** As a developer, I want all XML layout files and Compose-superseded drawable resources removed, so that no dead XML view resources remain in the project.

#### Acceptance Criteria

1. THE App SHALL delete `res/layout/activity_main.xml`, `res/layout/fragment_actual_currency.xml`, and `res/layout/dialog_coin_picker.xml`.
2. THE App SHALL delete `res/drawable/radio_checked_state.xml`, `res/drawable/radio_normal_state.xml`, and `res/drawable/radio_selector.xml`, as these are replaced by Compose state-based styling.
3. WHEN the above files are deleted, THE App SHALL compile without any `R.layout.*` or `R.drawable.radio_*` references remaining in Kotlin source files.
4. THE App SHALL disable `viewBinding` in `buildFeatures` after all binding classes have been removed from Kotlin source files.

---

### Requirement 10: Compose Theme

**User Story:** As a developer, I want a Compose `MaterialTheme` wrapper applied at the root of the UI, so that all composables share a consistent color scheme and typography.

#### Acceptance Criteria

1. THE App SHALL define a `BlockchainGraphTheme` composable that wraps `MaterialTheme` with a color scheme derived from the app's existing color palette.
2. THE MainActivity SHALL apply `BlockchainGraphTheme` as the outermost wrapper inside `setContent { }`.
3. THE BlockchainGraphTheme SHALL support both light and dark color schemes using `isSystemInDarkTheme()`.
4. WHERE the device runs Android 12 or higher, THE BlockchainGraphTheme SHALL apply dynamic color via `dynamicLightColorScheme` / `dynamicDarkColorScheme`.

---

### Requirement 11: Screenshot Testing

**User Story:** As a developer, I want screenshot tests for each composable, so that visual regressions are automatically detected when the UI changes.

#### Acceptance Criteria

1. THE App SHALL add `io.github.takahirom.roborazzi:roborazzi-compose:1.52.0` and the `io.github.takahirom.roborazzi` Gradle plugin as test dependencies in `app/build.gradle`.
2. THE App SHALL configure Robolectric with Native Graphics (RNG) enabled via `robolectric.properties` or test annotations so that composables render correctly on the JVM.
3. THE App SHALL provide a screenshot test for each of the following composables: `CurrencyScreen`, `CoinPickerBottomSheet`, `PeriodSelector`, and `PriceChart`.
4. EACH screenshot test SHALL render the composable wrapped in `BlockchainGraphTheme` and capture a reference image using `captureRoboImage()`.
5. THE screenshot tests SHALL cover at minimum two states per composable where applicable: a default/loaded state and an empty/loading state.
6. Reference screenshot images SHALL be stored under `app/src/test/snapshots/` and committed to version control as the baseline for regression detection.
7. WHEN running `./gradlew verifyRoborazziDebug`, THE App SHALL fail the build if any composable's rendered output differs from its reference screenshot.
8. WHEN running `./gradlew recordRoborazziDebug`, THE App SHALL regenerate all reference screenshots.

---

### Requirement 12: Composable Unit Tests

**User Story:** As a developer, I want Robolectric-based unit tests for each composable, so that UI behaviour and state rendering can be verified on the JVM without a device or emulator.

#### Acceptance Criteria

1. THE App SHALL add `androidx.compose.ui:ui-test-junit4` (version managed by the Compose BOM) and `org.robolectric:robolectric` as `testImplementation` dependencies in `app/build.gradle`.
2. EACH composable unit test SHALL be annotated with `@RunWith(RobolectricTestRunner::class)` and use `createComposeRule()` to host the composable under test.
3. THE App SHALL provide unit tests for `CurrencyScreen` that verify: the currency symbol button is displayed, the "Last quote" label is displayed, the price value is displayed when `uiState` contains a loaded price, and a loading indicator is displayed when `uiState` is in a loading state.
4. THE App SHALL provide unit tests for `CoinPickerBottomSheet` that verify: all 22 `CurrencyEnum` entries are displayed, the currently selected entry is visually marked as selected, and tapping an entry invokes the selection callback with the correct `CurrencyEnum` value.
5. THE App SHALL provide unit tests for `PeriodSelector` that verify: all six `ChartPeriod` buttons are displayed, the currently selected period button is visually distinguished, and tapping a period button invokes the callback with the correct `ChartPeriod` value.
6. THE App SHALL provide unit tests for `PriceChart` that verify: the composable renders without crashing when given an empty list of `ChartPoint` values, and the composable renders without crashing when given a non-empty list of `ChartPoint` values.
7. ALL composable unit tests SHALL use fake or stub ViewModel state (plain data objects or fake StateFlow) — no real repositories, DAOs, or network calls SHALL be made in any composable unit test.
8. THE composable unit tests SHALL be located under `app/src/test/java/com/osias/blockchain/ui/` and run as part of `./gradlew test`.
9. EVERY composable node that is the subject of a test assertion SHALL be identified by a `Modifier.testTag()` — tests SHALL use `onNodeWithTag()` exclusively for structural assertions and SHALL NOT rely on `onNodeWithText()` or `onNodeWithContentDescription()` for node lookup.
10. EACH composable SHALL define its test tags as constants in a companion `TestTags` object co-located in the same file, and `testTagsAsResourceId = true` SHALL be enabled at the semantic root of each composable so tags are accessible to all test frameworks.

---

### Requirement 14: Post-Migration Documentation Update

**User Story:** As a developer, I want the project steering documents updated to reflect the Compose migration, so that the documented architecture, code style, and project context match the actual codebase after the migration is complete.

#### Acceptance Criteria

1. AFTER the migration is complete, `.kiro/steering/project-context.md` SHALL be updated to replace all references to XML layouts, View Binding, Fragments, and MPAndroidChart with their Compose equivalents, and the tech stack table SHALL reflect the new dependencies (Compose BOM 2025.12.01, Vico 2.4.3, Roborazzi 1.52.0, navigation-compose 2.9.0).
2. AFTER the migration is complete, `.kiro/steering/architecture.md` SHALL be updated to describe the Compose-based view layer — replacing Fragment/Dialog/ViewBinding patterns with composable functions, `StateFlow`/`collectAsState`, `LaunchedEffect`, and `viewModelScope` — and the "Adding a New Screen" checklist SHALL reflect the Compose workflow instead of the Fragment workflow.
3. AFTER the migration is complete, `.kiro/steering/code-style.md` SHALL be updated to remove View Binding conventions and replace them with Compose conventions: `testTag` usage, `TestTags` objects, no `!!` in composables, `collectAsState()` for state observation, and `LaunchedEffect` for side effects.
4. THE documentation updates SHALL be the final task in the implementation plan, performed only after all composables are implemented, all tests pass, and all XML/Fragment files have been deleted.

---

### Requirement 13: Edge-to-Edge and Rotation Support

**User Story:** As a user, I want the app to display edge-to-edge and support screen rotation, so that the UI uses the full screen area and remains usable in both portrait and landscape orientations.

#### Acceptance Criteria

1. THE MainActivity SHALL call `enableEdgeToEdge()` before `setContent { }` so that the app draws behind system bars on Android 15+ and on supported older versions.
2. THE CurrencyScreen SHALL apply `WindowInsets.safeDrawing` (or equivalent) as padding via `Modifier.windowInsetsPadding(WindowInsets.safeDrawing)` so that content is never obscured by the status bar, navigation bar, or display cutouts.
3. THE PeriodSelector, anchored to the bottom of the screen, SHALL account for the navigation bar inset so that its buttons are not hidden behind the system navigation bar.
4. THE App SHALL NOT lock screen orientation in `AndroidManifest.xml` — both portrait and landscape orientations SHALL be supported.
5. WHEN the device is rotated, THE CurrencyScreen SHALL retain the currently selected `CurrencyEnum` and `ChartPeriod` without resetting to defaults, as these are held in `CurrencyViewModel` which survives configuration changes.
6. WHEN the device is rotated, THE CoinPickerBottomSheet SHALL dismiss and not attempt to re-show itself unless the user explicitly taps the currency button again.
7. THE PriceChart SHALL recompose and resize correctly when the screen dimensions change due to rotation, without crashing or displaying stale layout measurements.
