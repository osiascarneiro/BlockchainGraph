# Implementation Plan: Navigation 3 Migration

## Overview

Swap the Nav2 dependency for Nav3 in `app/build.gradle`, then rewrite `AppNavGraph.kt` to use `NavDisplay`, `rememberNavBackStack`, and a type-safe `CurrencyKey`. No other files are touched.

## Tasks

- [x] 1. Update `app/build.gradle` dependencies and plugins
  - Remove `implementation "androidx.navigation:navigation-compose:2.9.0"`
  - Add `implementation "androidx.navigation3:navigation3-runtime:1.1.1"`
  - Add `implementation "androidx.navigation3:navigation3-ui:1.1.1"`
  - Add `id 'org.jetbrains.kotlin.plugin.serialization'` to the `plugins` block (required for `@Serializable` on `CurrencyKey`)
  - _Requirements: 1.1, 1.2, 1.3, 1.4_

- [x] 2. Rewrite `AppNavGraph.kt` with Nav3 API
  - [x] 2.1 Define `CurrencyKey` as a `@Serializable data object` implementing `NavKey`
    - Replace the `object Routes` string-constants block entirely
    - Add `import kotlinx.serialization.Serializable` and `import androidx.navigation3.runtime.NavKey`
    - _Requirements: 2.1, 2.2, 7.1, 7.2_

  - [x] 2.2 Replace `NavHost` with `NavDisplay` and wire the back stack
    - Remove the `navController: NavHostController` parameter from `AppNavGraph` (no-arg signature preserved for `MainActivity`)
    - Create back-stack state with `rememberNavBackStack(CurrencyKey)`
    - Replace `NavHost { composable(...) }` with `NavDisplay(backStack, onBack, entryProvider { entry<CurrencyKey> { CurrencyScreen() } })`
    - Remove all `androidx.navigation.compose` and `androidx.navigation.NavHostController` imports
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 4.1, 4.2, 4.3, 5.1, 5.2, 6.2, 6.3, 7.1, 7.2, 7.3_

- [x] 3. Checkpoint — verify the build and existing tests pass
  - Run `./gradlew assembleDebug` and confirm zero compilation errors
  - Run `./gradlew test` and confirm all existing tests pass without modification
  - _Requirements: 1.4, 5.3, 6.1, 7.3_
