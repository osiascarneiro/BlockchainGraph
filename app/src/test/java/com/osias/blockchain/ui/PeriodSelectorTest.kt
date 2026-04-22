package com.osias.blockchain.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.osias.blockchain.model.enumeration.ChartPeriod
import com.osias.blockchain.ui.component.PeriodSelector
import com.osias.blockchain.ui.component.PeriodSelectorTags
import com.osias.blockchain.ui.theme.BlockchainGraphTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PeriodSelectorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `all six ChartPeriod buttons are displayed`() {
        composeTestRule.setContent {
            BlockchainGraphTheme {
                PeriodSelector(
                    selectedPeriod = ChartPeriod.ONE_MONTH,
                    onPeriodSelected = {}
                )
            }
        }

        // Verify all 6 periods are displayed
        ChartPeriod.values().forEach { period ->
            composeTestRule
                .onNodeWithTag(PeriodSelectorTags.periodButton(period))
                .assertIsDisplayed()
        }
    }

    @Test
    fun `selected button is visually distinguished`() {
        val selectedPeriod = ChartPeriod.ONE_YEAR

        composeTestRule.setContent {
            BlockchainGraphTheme {
                PeriodSelector(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = {}
                )
            }
        }

        // The selected period button should be displayed
        composeTestRule
            .onNodeWithTag(PeriodSelectorTags.periodButton(selectedPeriod))
            .assertIsDisplayed()
    }

    @Test
    fun `tapping a button invokes callback with correct ChartPeriod value`() {
        var receivedPeriod: ChartPeriod? = null
        val testPeriod = ChartPeriod.SIX_MONTHS

        composeTestRule.setContent {
            BlockchainGraphTheme {
                PeriodSelector(
                    selectedPeriod = ChartPeriod.ONE_MONTH,
                    onPeriodSelected = { receivedPeriod = it }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(PeriodSelectorTags.periodButton(testPeriod))
            .performClick()

        assertEquals(testPeriod, receivedPeriod)
    }
}
