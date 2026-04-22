package com.osias.blockchain.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.osias.blockchain.ui.screen.CurrencyScreen
import kotlinx.serialization.Serializable

@Serializable
data object CurrencyKey : NavKey

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
