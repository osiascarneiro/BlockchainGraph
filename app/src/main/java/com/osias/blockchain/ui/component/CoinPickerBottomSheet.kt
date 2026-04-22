package com.osias.blockchain.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import com.osias.blockchain.model.enumeration.CurrencyEnum

object CoinPickerTags {
    const val ROOT = "coin_picker_root"
    fun coinItem(coin: CurrencyEnum) = "coin_picker_item_${coin.name}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinPickerBottomSheet(
    selectedCoin: CurrencyEnum,
    onCoinSelected: (CurrencyEnum) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .semantics { testTagsAsResourceId = true }
            .testTag(CoinPickerTags.ROOT)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            items(CurrencyEnum.values()) { coin ->
                ListItem(
                    headlineContent = { Text(coin.symbol) },
                    trailingContent = {
                        RadioButton(
                            selected = coin == selectedCoin,
                            onClick = null
                        )
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
