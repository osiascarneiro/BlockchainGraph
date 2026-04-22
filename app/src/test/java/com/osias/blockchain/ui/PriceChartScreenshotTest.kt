package com.osias.blockchain.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.osias.blockchain.model.entity.ChartPoint
import com.osias.blockchain.model.enumeration.ChartPeriod
import com.osias.blockchain.ui.component.PriceChart
import com.osias.blockchain.ui.theme.BlockchainGraphTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@GraphicsMode(GraphicsMode.Mode.NATIVE)
@RunWith(RobolectricTestRunner::class)
class PriceChartScreenshotTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `PriceChart empty state screenshot`() {
        composeTestRule.setContent {
            BlockchainGraphTheme {
                PriceChart(
                    points = emptyList(),
                    formatXLabel = { "date" },
                    formatYLabel = { "price" },
                    modifier = Modifier.fillMaxWidth().height(300.dp)
                )
            }
        }
        composeTestRule.onRoot()
            .captureRoboImage("src/test/snapshots/PriceChart_empty.png")
    }

    @Test
    fun `PriceChart non-empty state screenshot`() {
        val date = Date()
        val dateFormatter = SimpleDateFormat("dd/MM", Locale.getDefault())
        val points = listOf(
            ChartPoint(1_699_920_000f, 40000f, date, ChartPeriod.ONE_MONTH),
            ChartPoint(1_700_006_400f, 41500f, date, ChartPeriod.ONE_MONTH),
            ChartPoint(1_700_092_800f, 42000f, date, ChartPeriod.ONE_MONTH),
            ChartPoint(1_700_179_200f, 43500f, date, ChartPeriod.ONE_MONTH),
            ChartPoint(1_700_265_600f, 42800f, date, ChartPeriod.ONE_MONTH)
        )
        composeTestRule.setContent {
            BlockchainGraphTheme {
                PriceChart(
                    points = points,
                    formatXLabel = { value ->
                        dateFormatter.format(Date(value.toLong() * 1000))
                    },
                    formatYLabel = { value -> "$${"%.0f".format(value)}" },
                    modifier = Modifier.fillMaxWidth().height(300.dp)
                )
            }
        }
        composeTestRule.onRoot()
            .captureRoboImage("src/test/snapshots/PriceChart_with_data.png")
    }
}
