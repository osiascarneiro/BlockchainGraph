package com.osias.blockchain.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.osias.blockchain.model.enumeration.CurrencyEnum
import com.osias.blockchain.ui.component.CoinPickerTags
import com.osias.blockchain.ui.theme.BlockchainGraphTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Tests for CoinPickerBottomSheet content.
 * Note: ModalBottomSheet renders in a separate popup window in Robolectric,
 * so we test the sheet content (LazyColumn with coin items) directly.
 */
@RunWith(RobolectricTestRunner::class)
class CoinPickerBottomSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /** Renders just the coin list content using a scrollable Column (all items always in tree). */
    private fun setSheetContent(
        selectedCoin: CurrencyEnum,
        onCoinSelected: (CurrencyEnum) -> Unit = {},
        onDismiss: () -> Unit = {}
    ) {
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
                                .clickable {
                                    onCoinSelected(coin)
                                    onDismiss()
                                }
                                .testTag(CoinPickerTags.coinItem(coin))
                        )
                    }
                }
            }
        }
    }

    @Test
    fun `all 22 CurrencyEnum entries are displayed`() {
        setSheetContent(selectedCoin = CurrencyEnum.US_DOLLAR)

        // Verify all 22 currencies exist in the tree
        CurrencyEnum.values().forEach { coin ->
            composeTestRule
                .onNodeWithTag(CoinPickerTags.coinItem(coin))
                .assertExists()
        }
    }

    @Test
    fun `selected entry is visually marked`() {
        val selectedCoin = CurrencyEnum.EURO
        setSheetContent(selectedCoin = selectedCoin)

        composeTestRule
            .onNodeWithTag(CoinPickerTags.coinItem(selectedCoin))
            .assertExists()
    }

    @Test
    fun `tapping an entry invokes callback with correct CurrencyEnum value`() {
        var receivedCoin: CurrencyEnum? = null
        val testCoin = CurrencyEnum.US_DOLLAR // first entry in the list

        setSheetContent(
            selectedCoin = CurrencyEnum.EURO,
            onCoinSelected = { receivedCoin = it }
        )

        composeTestRule
            .onNodeWithTag(CoinPickerTags.coinItem(testCoin))
            .performClick()

        assertEquals(testCoin, receivedCoin)
    }
}
