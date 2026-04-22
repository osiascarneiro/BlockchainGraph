package com.osias.blockchain.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.osias.blockchain.model.repository.ChartRepository
import com.osias.blockchain.model.repository.CurrencyRepository
import com.osias.blockchain.ui.screen.CurrencyScreen
import com.osias.blockchain.ui.screen.CurrencyScreenTags
import com.osias.blockchain.ui.theme.BlockchainGraphTheme
import com.osias.blockchain.viewmodel.CurrencyViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CurrencyScreenTest : KoinTest {

    private val mockCurrencyRepo: CurrencyRepository = mock()
    private val mockChartRepo: ChartRepository = mock()
    private val viewModel = CurrencyViewModel(mockCurrencyRepo, mockChartRepo)

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(module {
            single { viewModel }
        })
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `currency symbol button is displayed`() {
        composeTestRule.setContent {
            BlockchainGraphTheme {
                CurrencyScreen(viewModel = viewModel)
            }
        }

        composeTestRule
            .onNodeWithTag(CurrencyScreenTags.SELECT_COIN_BUTTON)
            .assertIsDisplayed()
    }

    @Test
    fun `last quote label is displayed`() {
        composeTestRule.setContent {
            BlockchainGraphTheme {
                CurrencyScreen(viewModel = viewModel)
            }
        }

        composeTestRule
            .onNodeWithTag(CurrencyScreenTags.LAST_QUOTE_LABEL)
            .assertIsDisplayed()
    }

    @Test
    fun `loading indicator is displayed when uiState is loading`() {
        // Force the uiState to loading before the composable renders
        val uiStateField = CurrencyViewModel::class.java.getDeclaredField("_uiState")
        uiStateField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        (uiStateField.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<com.osias.blockchain.viewmodel.CurrencyUiState>)
            .value = com.osias.blockchain.viewmodel.CurrencyUiState(isLoading = true)

        composeTestRule.setContent {
            BlockchainGraphTheme {
                CurrencyScreen(viewModel = viewModel)
            }
        }

        composeTestRule
            .onNodeWithTag(CurrencyScreenTags.LOADING_INDICATOR)
            .assertExists()
    }
}
