package com.osias.blockchain.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.osias.blockchain.model.enumeration.CurrencyEnum
import com.osias.blockchain.ui.component.CoinPickerBottomSheet
import com.osias.blockchain.ui.theme.BlockchainGraphTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@GraphicsMode(GraphicsMode.Mode.NATIVE)
@RunWith(RobolectricTestRunner::class)
class CoinPickerScreenshotTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `CoinPickerBottomSheet default state screenshot`() {
        composeTestRule.setContent {
            BlockchainGraphTheme {
                CoinPickerBottomSheet(
                    selectedCoin = CurrencyEnum.US_DOLLAR,
                    onCoinSelected = {},
                    onDismiss = {}
                )
            }
        }
        composeTestRule.onRoot()
            .captureRoboImage("src/test/snapshots/CoinPickerBottomSheet_default.png")
    }

    @Test
    fun `CoinPickerBottomSheet non-default selection screenshot`() {
        composeTestRule.setContent {
            BlockchainGraphTheme {
                CoinPickerBottomSheet(
                    selectedCoin = CurrencyEnum.EURO,
                    onCoinSelected = {},
                    onDismiss = {}
                )
            }
        }
        composeTestRule.onRoot()
            .captureRoboImage("src/test/snapshots/CoinPickerBottomSheet_euro_selected.png")
    }
}
