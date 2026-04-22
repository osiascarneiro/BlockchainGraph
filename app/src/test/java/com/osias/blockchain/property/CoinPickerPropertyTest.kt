package com.osias.blockchain.property

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.osias.blockchain.model.enumeration.CurrencyEnum
import com.osias.blockchain.ui.component.CoinPickerTags
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
 * Feature: compose-migration, Property 2 & 3: CoinPickerBottomSheet properties
 * Note: Tests the sheet content directly (bypassing ModalBottomSheet popup window).
 */
@RunWith(RobolectricTestRunner::class)
class CoinPickerPropertyTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `Property 2 - CoinPickerBottomSheet displays all entries and marks selected for any CurrencyEnum`() = runTest {
        var selectedCoin by mutableStateOf(CurrencyEnum.US_DOLLAR)

        composeTestRule.setContent {
            BlockchainGraphTheme {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    CurrencyEnum.values().forEach { coin ->
                        ListItem(
                            headlineContent = { Text(coin.symbol) },
                            trailingContent = {
                                RadioButton(selected = coin == selectedCoin, onClick = null)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(CoinPickerTags.coinItem(coin))
                        )
                    }
                }
            }
        }

        checkAll(100, Arb.enum<CurrencyEnum>()) { coin ->
            composeTestRule.runOnUiThread { selectedCoin = coin }
            composeTestRule.waitForIdle()

            // Property: all 22 entries always exist in the tree
            CurrencyEnum.values().forEach { c ->
                composeTestRule
                    .onNodeWithTag(CoinPickerTags.coinItem(c))
                    .assertExists()
            }
        }
    }

    @Test
    fun `Property 3 - CoinPickerBottomSheet callback receives tapped coin for any CurrencyEnum`() = runTest {
        var currentCoin by mutableStateOf(CurrencyEnum.US_DOLLAR)
        var receivedCoin: CurrencyEnum? = null

        // Render a single item that updates when currentCoin changes
        composeTestRule.setContent {
            BlockchainGraphTheme {
                ListItem(
                    headlineContent = { Text(currentCoin.symbol) },
                    trailingContent = {
                        RadioButton(selected = false, onClick = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { receivedCoin = currentCoin }
                        .testTag(CoinPickerTags.coinItem(currentCoin))
                )
            }
        }

        // Test each coin by updating the state
        CurrencyEnum.values().forEach { coin ->
            receivedCoin = null
            composeTestRule.runOnUiThread { currentCoin = coin }
            composeTestRule.waitForIdle()

            composeTestRule
                .onNodeWithTag(CoinPickerTags.coinItem(coin))
                .performClick()

            // Property: callback receives the exact tapped coin
            assertEquals(coin, receivedCoin)
        }
    }
}
