package com.osias.blockchain.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.osias.blockchain.model.enumeration.ChartPeriod
import com.osias.blockchain.model.entity.ChartPoint
import com.osias.blockchain.model.repository.ChartRepository
import com.osias.blockchain.model.repository.CurrencyRepository
import com.osias.blockchain.ui.screen.CurrencyScreen
import com.osias.blockchain.ui.theme.BlockchainGraphTheme
import com.osias.blockchain.viewmodel.CurrencyUiState
import com.osias.blockchain.viewmodel.CurrencyViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import java.util.Date

@GraphicsMode(GraphicsMode.Mode.NATIVE)
@RunWith(RobolectricTestRunner::class)
class CurrencyScreenScreenshotTest : KoinTest {

    private val mockCurrencyRepo: CurrencyRepository = mock()
    private val mockChartRepo: ChartRepository = mock()
    private val viewModel = CurrencyViewModel(mockCurrencyRepo, mockChartRepo)

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(module { single { viewModel } })
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `CurrencyScreen loading state screenshot`() {
        composeTestRule.setContent {
            BlockchainGraphTheme {
                CurrencyScreen(viewModel = viewModel)
            }
        }
        composeTestRule.onRoot()
            .captureRoboImage("src/test/snapshots/CurrencyScreen_loading.png")
    }

    @Test
    fun `CurrencyScreen loaded state screenshot`() {
        val loadedState = CurrencyUiState(
            isLoading = false,
            formattedPrice = "USD 42,000.00",
            chartPoints = listOf(
                ChartPoint(1_699_920_000f, 42000f, Date(), ChartPeriod.ONE_MONTH),
                ChartPoint(1_700_006_400f, 43500f, Date(), ChartPeriod.ONE_MONTH)
            )
        )
        val uiStateField = CurrencyViewModel::class.java.getDeclaredField("_uiState")
        uiStateField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        (uiStateField.get(viewModel) as MutableStateFlow<CurrencyUiState>).value = loadedState

        composeTestRule.setContent {
            BlockchainGraphTheme {
                CurrencyScreen(viewModel = viewModel)
            }
        }
        composeTestRule.onRoot()
            .captureRoboImage("src/test/snapshots/CurrencyScreen_loaded.png")
    }
}
