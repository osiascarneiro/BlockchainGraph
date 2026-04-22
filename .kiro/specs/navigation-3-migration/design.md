# Design Document: Navigation 3 Migration

## Overview

This migration replaces the Jetpack Navigation 2 (`androidx.navigation:navigation-compose:2.9.0`) layer in BlockchainGraph with Navigation 3 (`androidx.navigation3`). The change is confined entirely to `AppNavGraph.kt` and `app/build.gradle`. No screen composables, ViewModels, Koin modules, or `MainActivity` are touched.

Navigation 3 treats the back stack as a plain `MutableList<Any>` owned by the composable layer, replacing the opaque `NavHostController` with explicit, observable state. `NavDisplay` replaces `NavHost` as the rendering container, and type-safe Kotlin objects replace string route constants.

The app has a single destination (`CurrencyScreen`), so the migration is a minimal, atomic swap with no multi-back-stack or deep-link complexity.

**Key research findings:**
- Nav3 stable version is `1.1.1` ([get-started](https://developer.android.com/guide/navigation/navigation-3/get-started))
- `rememberNavBackStack(startKey)` (requires `@Serializable` + `NavKey`) survives configuration changes via `rememberSaveable` internally
- `remember { mutableStateListOf<Any>(CurrencyKey) }` is the non-serializable alternative — does **not** survive process death but does survive rotation when held in a `@Composable` scope
- `koinViewModel()` continues to work inside `NavEntry` content lambdas without any changes — Koin resolves from the nearest `KoinContext`, which is already provided by `BlockchainGraphApplication`
- `compileSdk 36` is already set in `app/build.gradle` — no SDK bump needed

---

## Architecture

The migration touches exactly two files:

```
app/build.gradle          ← swap Nav2 dependency for Nav3 dependencies
ui/navigation/AppNavGraph.kt  ← replace NavHost + Routes with NavDisplay + CurrencyKey
```

All other layers remain unchanged:

```
MainActivity  ──setContent──▶  BlockchainGraphTheme
                                    └─ AppNavGraph()          ← only internal impl changes
                                           └─ NavDisplay
                                                └─ NavEntry(CurrencyKey)
                                                       └─ CurrencyScreen()
                                                              └─ koinViewModel() → CurrencyViewModel
```

### Before vs After

| Concern | Nav2 (before) | Nav3 (after) |
|---|---|---|
| Dependency | `navigation-compose:2.9.0` | `navigation3-runtime:1.1.1` + `navigation3-ui:1.1.1` |
| Route identifier | `object Routes { const val CURRENCY = "currency" }` | `@Serializable data object CurrencyKey : NavKey` |
| Back-stack state | `NavHostController` (opaque) | `rememberNavBackStack(CurrencyKey)` (`SnapshotStateList`) |
| Container | `NavHost(navController, startDestination)` | `NavDisplay(backStack, onBack, entryProvider)` |
| Destination registration | `composable(Routes.CURRENCY) { CurrencyScreen() }` | `entry<CurrencyKey> { CurrencyScreen() }` (DSL) or `when` lambda |
| `AppNavGraph` parameter | `navController: NavHostController = rememberNavController()` | none (back stack is internal state) |

---

## Components and Interfaces

### `AppNavGraph.kt` (rewritten)

The file is fully replaced. It contains:

1. **`CurrencyKey`** — the sole navigation key:
   ```kotlin
   @Serializable
   data object CurrencyKey : NavKey
   ```

2. **`AppNavGraph` composable** — no parameters (matching the existing call-site in `MainActivity`):
   ```kotlin
   @Composable
   fun AppNavGraph() {
       val backStack = rememberNavBackStack(CurrencyKey)

       NavDisplay(
           backStack = backStack,
           onBack = { backStack.removeLastOrNull() },
           entryProvider = entryProvider {
               entry<CurrencyKey> { CurrencyScreen() }
           }
       )
   }
   ```

   - `rememberNavBackStack` requires `CurrencyKey` to be `@Serializable` and implement `NavKey`; it persists the back stack across configuration changes.
   - `onBack` pops the last entry; with a single-screen app this effectively does nothing (the system back gesture is handled by the OS).
   - `entryProvider { }` is the Nav3 DSL from `navigation3-runtime`; `entry<T>` maps a key type to a `NavEntry`.

### `app/build.gradle` (modified)

Remove:
```groovy
implementation "androidx.navigation:navigation-compose:2.9.0"
```

Add:
```groovy
implementation "androidx.navigation3:navigation3-runtime:1.1.1"
implementation "androidx.navigation3:navigation3-ui:1.1.1"
```

The Kotlin Serialization plugin is already present (`id 'org.jetbrains.kotlin.plugin.compose'` is in the plugins block; the serialization plugin `id 'org.jetbrains.kotlin.plugin.serialization'` must be added to support `@Serializable` on `CurrencyKey`).

### Unchanged files

| File | Reason untouched |
|---|---|
| `MainActivity.kt` | Calls `AppNavGraph()` with no arguments — signature is preserved |
| `CurrencyScreen.kt` | No navigation imports; ViewModel injected via `koinViewModel()` |
| `AppModule.kt` | No navigation wiring in Koin module |
| All other source files | No Nav2 imports outside `AppNavGraph.kt` |

---

## Data Models

Navigation 3 uses plain Kotlin objects as back-stack keys. For this app:

```kotlin
@Serializable
data object CurrencyKey : NavKey
```

- `NavKey` is a marker interface from `androidx.navigation3:navigation3-runtime`.
- `@Serializable` (from `kotlinx-serialization-core`) is required by `rememberNavBackStack` to persist the back stack across process death.
- `data object` is idiomatic for a destination with no arguments.

The back stack type is `NavBackStack<NavKey>` (returned by `rememberNavBackStack`), which is a `SnapshotStateList` — fully observable by Compose.

---

## Error Handling

| Scenario | Handling |
|---|---|
| Unknown key in `entryProvider` | The `entryProvider { }` DSL throws `IllegalArgumentException` for unregistered keys at runtime. With a single registered key this cannot occur in production. |
| `onBack` called on a single-entry stack | `removeLastOrNull()` is a no-op on an empty list — safe by design. |
| Serialization plugin missing | Compile-time error: `@Serializable` annotation unresolved. Caught during build, not at runtime. |
| Nav3 version conflict | Gradle will surface a dependency resolution error at build time. Both `navigation3-runtime` and `navigation3-ui` must use the same version (`1.1.1`). |

---

## Testing Strategy

PBT assessment: This feature is a declarative wiring migration — it replaces one set of configuration calls with another. There are no pure functions with meaningful input variation, no data transformations, and no algorithms. Property-based testing is not appropriate here. All acceptance criteria classify as SMOKE, EXAMPLE, or INTEGRATION tests.

### Smoke / Static Checks

These verify the migration is structurally complete and can be checked by reading source files or running a build:

- `build.gradle` contains `navigation3-runtime` and `navigation3-ui`, and does **not** contain `navigation-compose`
- `AppNavGraph.kt` contains no imports from `androidx.navigation.compose` or `androidx.navigation.NavHostController`
- `AppNavGraph.kt` contains no reference to `Routes`
- `MainActivity.kt` is byte-for-byte identical to the pre-migration version
- `CurrencyScreen.kt` is byte-for-byte identical to the pre-migration version
- `./gradlew assembleDebug` succeeds with zero errors

### Example / Unit Tests (Robolectric + `createComposeRule`)

These verify runtime behaviour of the migrated navigation layer:

1. **Start destination renders correctly** — set up `AppNavGraph()` with a `KoinTestRule`, verify that `CurrencyScreenTags.SELECT_COIN_BUTTON` is displayed (confirms `CurrencyScreen` is the initial destination and Koin injection works inside `NavEntry`).

2. **Back stack initial state** — verify the back stack contains exactly one entry (`CurrencyKey`) on first composition.

3. **Configuration change preserves back stack** — use Robolectric's `ActivityScenario.recreate()` and verify the back stack still contains `CurrencyKey` after recreation (validates `rememberNavBackStack` persistence).

### Integration Tests

- **Koin ViewModel injection inside NavEntry** — render `AppNavGraph()` with a real (test) Koin module providing `CurrencyViewModel`; verify `CurrencyScreen` receives a non-null ViewModel and the loading indicator or price value is shown.

### Existing Tests

All existing tests in `ui/`, `property/`, `repository/`, and `viewmodel/` packages are unaffected by this migration. They test `CurrencyScreen`, `CurrencyViewModel`, and the repository layer directly — none depend on `NavHost`, `NavHostController`, or `Routes`. The full test suite (`./gradlew test`) must pass without modification after the migration.
