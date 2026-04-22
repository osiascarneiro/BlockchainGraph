package com.osias.blockchain.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.osias.blockchain.ui.component.CoinPickerBottomSheet
import com.osias.blockchain.ui.component.PeriodSelector
import com.osias.blockchain.ui.component.PriceChart
import com.osias.blockchain.viewmodel.CurrencyViewModel
import org.koin.androidx.compose.koinViewModel

object CurrencyScreenTags {
    const val SELECT_COIN_BUTTON = "currency_screen_select_coin_button"
    const val LAST_QUOTE_LABEL = "currency_screen_last_quote_label"
    const val PRICE_VALUE = "currency_screen_price_value"
    const val LOADING_INDICATOR = "currency_screen_loading_indicator"
}

@Composable
fun CurrencyScreen(
    viewModel: CurrencyViewModel = koinViewModel()
) {
    val coin by viewModel.coin.collectAsStateWithLifecycle()
    val period by viewModel.period.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showPicker by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .semantics { testTagsAsResourceId = true },
        bottomBar = {
            PeriodSelector(
                selectedPeriod = period,
                onPeriodSelected = viewModel::selectPeriod
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { showPicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(CurrencyScreenTags.SELECT_COIN_BUTTON)
            ) {
                Text(text = coin.symbol)
            }

            Text(
                text = "Last quote",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.testTag(CurrencyScreenTags.LAST_QUOTE_LABEL)
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.testTag(CurrencyScreenTags.LOADING_INDICATOR)
                        )
                    }
                }
                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {
                    Text(
                        text = uiState.formattedPrice,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.testTag(CurrencyScreenTags.PRICE_VALUE)
                    )
                }
            }

            PriceChart(
                points = uiState.chartPoints,
                formatXLabel = viewModel::formatUnixDate,
                formatYLabel = viewModel::formatCurrency,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }

    if (showPicker) {
        CoinPickerBottomSheet(
            selectedCoin = coin,
            onCoinSelected = viewModel::selectCoin,
            onDismiss = { showPicker = false }
        )
    }
}
