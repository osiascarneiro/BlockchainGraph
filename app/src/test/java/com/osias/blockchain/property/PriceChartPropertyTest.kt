package com.osias.blockchain.property

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.osias.blockchain.model.entity.ChartPoint
import com.osias.blockchain.model.enumeration.ChartPeriod
import com.osias.blockchain.ui.component.PriceChart
import com.osias.blockchain.ui.component.PriceChartTags
import com.osias.blockchain.ui.theme.BlockchainGraphTheme
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

/**
 * Feature: compose-migration, Property 6: PriceChart renders without crashing
 */
@RunWith(RobolectricTestRunner::class)
class PriceChartPropertyTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `Property 6 - PriceChart renders without crashing for any list of ChartPoints`() = runTest {
        val chartPointArb = Arb.bind(
            Arb.float(min = 0f, max = 1000000f),
            Arb.float(min = 0f, max = 100000f)
        ) { x, y ->
            // Use integer x-values (Unix timestamps) to avoid Vico's 4-decimal-place limit
            ChartPoint(x.toLong().toFloat(), y, Date(), ChartPeriod.ONE_MONTH)
        }

        // Use a state holder so we can drive the composable with different inputs
        // without calling setContent multiple times
        var currentPoints by mutableStateOf<List<ChartPoint>>(emptyList())

        composeTestRule.setContent {
            BlockchainGraphTheme {
                PriceChart(
                    points = currentPoints,
                    formatXLabel = { it.toString() },
                    formatYLabel = { it.toString() },
                    modifier = Modifier.fillMaxWidth().height(300.dp)
                )
            }
        }

        // Verify the chart root exists initially (empty list)
        composeTestRule.onNodeWithTag(PriceChartTags.ROOT).assertExists()

        // Now drive the composable with 100 different point lists
        checkAll(100, Arb.list(chartPointArb, range = 0..50)) { points ->
            composeTestRule.runOnUiThread {
                currentPoints = points
            }
            composeTestRule.waitForIdle()
            // Property: chart root always exists regardless of input
            composeTestRule.onNodeWithTag(PriceChartTags.ROOT).assertExists()
        }
    }
}
