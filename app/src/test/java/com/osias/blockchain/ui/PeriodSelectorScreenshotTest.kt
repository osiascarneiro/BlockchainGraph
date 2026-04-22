package com.osias.blockchain.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.osias.blockchain.model.enumeration.ChartPeriod
import com.osias.blockchain.ui.component.PeriodSelector
import com.osias.blockchain.ui.theme.BlockchainGraphTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@GraphicsMode(GraphicsMode.Mode.NATIVE)
@RunWith(RobolectricTestRunner::class)
class PeriodSelectorScreenshotTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `PeriodSelector default state screenshot`() {
        composeTestRule.setContent {
            BlockchainGraphTheme {
                PeriodSelector(
                    selectedPeriod = ChartPeriod.ONE_MONTH,
                    onPeriodSelected = {}
                )
            }
        }
        composeTestRule.onRoot()
            .captureRoboImage("src/test/snapshots/PeriodSelector_one_month.png")
    }

    @Test
    fun `PeriodSelector non-default period screenshot`() {
        composeTestRule.setContent {
            BlockchainGraphTheme {
                PeriodSelector(
                    selectedPeriod = ChartPeriod.ONE_YEAR,
                    onPeriodSelected = {}
                )
            }
        }
        composeTestRule.onRoot()
            .captureRoboImage("src/test/snapshots/PeriodSelector_one_year.png")
    }
}
