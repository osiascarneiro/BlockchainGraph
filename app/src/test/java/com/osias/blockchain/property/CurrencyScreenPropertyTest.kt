package com.osias.blockchain.property

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.osias.blockchain.model.enumeration.CurrencyEnum
import com.osias.blockchain.model.repository.ChartRepository
import com.osias.blockchain.model.repository.CurrencyRepository
import com.osias.blockchain.ui.screen.CurrencyScreen
import com.osias.blockchain.ui.screen.CurrencyScreenTags
import com.osias.blockchain.ui.theme.BlockchainGraphTheme
import com.osias.blockchain.viewmodel.CurrencyViewModel
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner

/**
 * Feature: compose-migration, Property 1: Currency button displays the selected coin's symbol
 */
@RunWith(RobolectricTestRunner::class)
class CurrencyScreenPropertyTest : KoinTest {

    private val mockCurrencyRepo: CurrencyRepository = mock()
    private val mockChartRepo: ChartRepository = mock()
    private val viewModel = CurrencyViewModel(mockCurrencyRepo, mockChartRepo)

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(module { single { viewModel } })
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `Property 1 - currency button displays selected coin symbol for all CurrencyEnum values`() = runTest {
        // Use state to drive the composable without calling setContent multiple times
        var currentCoin by mutableStateOf(CurrencyEnum.US_DOLLAR)

        composeTestRule.setContent {
            BlockchainGraphTheme {
                CurrencyScreen(viewModel = viewModel)
            }
        }

        checkAll(100, Arb.enum<CurrencyEnum>()) { coin ->
            composeTestRule.runOnUiThread {
                viewModel.selectCoin(coin)
                currentCoin = coin
            }
            composeTestRule.waitForIdle()

            // Property: the button always shows the currently selected coin's symbol
            composeTestRule
                .onNodeWithTag(CurrencyScreenTags.SELECT_COIN_BUTTON)
                .assertTextContains(coin.symbol, substring = true)
        }
    }
}
