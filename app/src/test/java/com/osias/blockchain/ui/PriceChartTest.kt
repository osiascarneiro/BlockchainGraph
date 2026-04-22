package com.osias.blockchain.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.osias.blockchain.model.entity.ChartPoint
import com.osias.blockchain.model.enumeration.ChartPeriod
import com.osias.blockchain.ui.component.PriceChart
import com.osias.blockchain.ui.component.PriceChartTags
import com.osias.blockchain.ui.theme.BlockchainGraphTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class PriceChartTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `renders without crashing on empty ChartPoint list`() {
        composeTestRule.setContent {
            BlockchainGraphTheme {
                PriceChart(
                    points = emptyList(),
                    formatXLabel = { it.toString() },
                    formatYLabel = { it.toString() }
                )
            }
        }

        // Chart should be displayed even with empty data
        composeTestRule
            .onNodeWithTag(PriceChartTags.ROOT)
            .assertIsDisplayed()
    }

    @Test
    fun `renders without crashing on non-empty ChartPoint list`() {
        val testPoints = listOf(
            ChartPoint(1000f, 30000f, Date(), ChartPeriod.ONE_MONTH),
            ChartPoint(2000f, 31000f, Date(), ChartPeriod.ONE_MONTH),
            ChartPoint(3000f, 32000f, Date(), ChartPeriod.ONE_MONTH)
        )

        composeTestRule.setContent {
            BlockchainGraphTheme {
                PriceChart(
                    points = testPoints,
                    formatXLabel = { it.toString() },
                    formatYLabel = { it.toString() }
                )
            }
        }

        // Chart should be displayed with data
        composeTestRule
            .onNodeWithTag(PriceChartTags.ROOT)
            .assertIsDisplayed()
    }
}
