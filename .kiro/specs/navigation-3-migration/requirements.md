# Requirements Document

## Introduction

This feature migrates BlockchainGraph's navigation layer from Jetpack Navigation 2 (`androidx.navigation:navigation-compose:2.9.0`) to Navigation 3 (`androidx.navigation3`). Navigation 3 replaces string-based routes with type-safe Kotlin objects as back-stack keys, replaces `NavHost` with `NavDisplay`, and manages the back stack as an explicit `MutableList` held in a `rememberNavController()` (Nav3) state holder. The migration must preserve all existing screen behaviour, Koin ViewModel injection, edge-to-edge support, and the existing test suite.

## Glossary

- **Nav2**: Jetpack Navigation Compose 2.x (`androidx.navigation:navigation-compose`), the library being replaced.
- **Nav3**: Jetpack Navigation 3 (`androidx.navigation3:navigation3-ui` + `androidx.navigation3:navigation3-runtime`), the target library.
- **Back_Stack**: The `MutableList<Any>` managed by `rememberNavController()` (Nav3) that represents the ordered list of navigation keys currently on the stack.
- **NavDisplay**: The Nav3 composable that renders the current back-stack entry, replacing `NavHost`.
- **EntryProvider**: The Nav3 DSL block (`entryProvider { }`) that maps back-stack keys to `NavEntry` composable content, replacing `composable()` route registrations.
- **NavKey**: A Kotlin `object` or `data class` used as a type-safe back-stack key, replacing string-based route constants.
- **NavEntry**: A Nav3 unit of navigation content associated with a `NavKey`.
- **AppNavGraph**: The existing composable in `ui/navigation/AppNavGraph.kt` that hosts the navigation container.
- **Routes**: The existing `object Routes` holding string route constants — to be replaced by `NavKey` objects.
- **CurrencyScreen**: The single screen composable currently registered under `Routes.CURRENCY`.
- **MainActivity**: The single `ComponentActivity` that hosts `AppNavGraph` inside `setContent`.
- **Koin**: The dependency-injection framework (v4.2.0) used to provide ViewModels to composables via `koinViewModel()`.

## Requirements

### Requirement 1: Replace Nav2 Dependencies with Nav3 Dependencies

**User Story:** As a developer, I want the project to depend on Navigation 3 libraries instead of Navigation 2, so that the app uses the supported Nav3 API surface.

#### Acceptance Criteria

1. THE `build.gradle` SHALL declare `androidx.navigation3:navigation3-runtime` as an `implementation` dependency.
2. THE `build.gradle` SHALL declare `androidx.navigation3:navigation3-ui` as an `implementation` dependency.
3. THE `build.gradle` SHALL NOT declare `androidx.navigation:navigation-compose` as a dependency after the migration.
4. WHEN the project is assembled, THE Gradle_Build_System SHALL resolve all Nav3 dependencies without version conflicts.

---

### Requirement 2: Replace String-Based Routes with Type-Safe NavKeys

**User Story:** As a developer, I want navigation destinations identified by type-safe Kotlin objects instead of strings, so that route mismatches are caught at compile time.

#### Acceptance Criteria

1. THE `AppNavGraph` SHALL define a `NavKey` for the Currency destination as a Kotlin `object` (e.g., `object CurrencyKey`).
2. THE `AppNavGraph` SHALL NOT reference the `Routes` string constants object after the migration.
3. WHEN a `NavKey` is pushed onto the `Back_Stack`, THE `NavDisplay` SHALL render the `NavEntry` associated with that key.
4. THE `Back_Stack` SHALL be initialised with `CurrencyKey` as its sole entry, making `CurrencyScreen` the start destination.

---

### Requirement 3: Replace NavHost with NavDisplay

**User Story:** As a developer, I want the navigation container to use `NavDisplay` instead of `NavHost`, so that the app uses the Nav3 rendering API.

#### Acceptance Criteria

1. THE `AppNavGraph` SHALL use `NavDisplay` as the navigation container composable.
2. THE `AppNavGraph` SHALL NOT use `NavHost` after the migration.
3. THE `NavDisplay` SHALL receive the `Back_Stack` as its `backStack` parameter.
4. THE `NavDisplay` SHALL receive an `EntryProvider` that maps `CurrencyKey` to a `NavEntry` containing `CurrencyScreen`.
5. WHEN the `Back_Stack` contains only `CurrencyKey`, THE `NavDisplay` SHALL display `CurrencyScreen`.

---

### Requirement 4: Replace rememberNavController (Nav2) with Nav3 Back-Stack State

**User Story:** As a developer, I want the back-stack state managed by Nav3's state holder instead of Nav2's `NavHostController`, so that navigation state follows the Nav3 ownership model.

#### Acceptance Criteria

1. THE `AppNavGraph` SHALL manage the back stack as a `MutableList<Any>` created via `rememberNavController()` (Nav3 overload) or `remember { mutableStateListOf(CurrencyKey) }`.
2. THE `AppNavGraph` SHALL NOT accept or create a `NavHostController` (Nav2) parameter after the migration.
3. WHEN the device undergoes a configuration change, THE `Back_Stack` SHALL retain its state across the recreation.

---

### Requirement 5: Preserve CurrencyScreen Behaviour and Koin Integration

**User Story:** As a developer, I want `CurrencyScreen` to continue receiving its ViewModel via Koin after the migration, so that no business logic or DI wiring changes are required.

#### Acceptance Criteria

1. WHEN `CurrencyScreen` is rendered inside a `NavEntry`, THE `CurrencyScreen` SHALL obtain `CurrencyViewModel` via `koinViewModel()` without any changes to `CurrencyScreen.kt` or `AppModule.kt`.
2. THE `CurrencyScreen` composable signature SHALL remain unchanged after the migration.
3. WHEN the app is launched, THE `CurrencyScreen` SHALL display the Bitcoin price and chart as before the migration.

---

### Requirement 6: Preserve Edge-to-Edge and MainActivity Integration

**User Story:** As a developer, I want `MainActivity` to continue hosting the navigation graph with edge-to-edge support after the migration, so that the visual behaviour is unchanged.

#### Acceptance Criteria

1. THE `MainActivity` SHALL continue to call `enableEdgeToEdge()` before `setContent`.
2. THE `MainActivity` SHALL continue to call `AppNavGraph()` inside `BlockchainGraphTheme { }` within `setContent`.
3. THE `MainActivity` SHALL NOT require any changes to its own source file as a result of the Nav3 migration.

---

### Requirement 7: Remove All Nav2 API References

**User Story:** As a developer, I want no Nav2 API symbols remaining in the navigation layer, so that the codebase is fully on Nav3 and there is no ambiguity between the two APIs.

#### Acceptance Criteria

1. THE `AppNavGraph.kt` SHALL NOT import any symbol from `androidx.navigation.compose` after the migration.
2. THE `AppNavGraph.kt` SHALL NOT import `androidx.navigation.NavHostController` after the migration.
3. WHEN the project is compiled, THE Kotlin_Compiler SHALL produce zero unresolved-reference errors related to Nav2 symbols.
