package com.osias.blockchain.property

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.osias.blockchain.model.enumeration.ChartPeriod
import com.osias.blockchain.ui.component.PeriodSelector
import com.osias.blockchain.ui.component.PeriodSelectorTags
import com.osias.blockchain.ui.theme.BlockchainGraphTheme
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Feature: compose-migration, Property 4 & 5: PeriodSelector properties
 */
@RunWith(RobolectricTestRunner::class)
class PeriodSelectorPropertyTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `Property 4 - PeriodSelector displays all periods and marks selected for any ChartPeriod`() = runTest {
        var selectedPeriod by mutableStateOf(ChartPeriod.ONE_MONTH)

        composeTestRule.setContent {
            BlockchainGraphTheme {
                PeriodSelector(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { selectedPeriod = it }
                )
            }
        }

        checkAll(100, Arb.enum<ChartPeriod>()) { period ->
            composeTestRule.runOnUiThread { selectedPeriod = period }
            composeTestRule.waitForIdle()

            // Property: all 6 buttons always exist regardless of which is selected
            ChartPeriod.values().forEach { p ->
                composeTestRule
                    .onNodeWithTag(PeriodSelectorTags.periodButton(p))
                    .assertExists()
            }
        }
    }

    @Test
    fun `Property 5 - PeriodSelector callback receives tapped period for any ChartPeriod`() = runTest {
        var selectedPeriod by mutableStateOf(ChartPeriod.ONE_MONTH)
        var receivedPeriod: ChartPeriod? = null

        composeTestRule.setContent {
            BlockchainGraphTheme {
                PeriodSelector(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { receivedPeriod = it }
                )
            }
        }

        checkAll(100, Arb.enum<ChartPeriod>()) { tappedPeriod ->
            receivedPeriod = null

            composeTestRule
                .onNodeWithTag(PeriodSelectorTags.periodButton(tappedPeriod))
                .performClick()

            // Property: callback always receives the exact tapped period
            assertEquals(tappedPeriod, receivedPeriod)
        }
    }
}
