package com.osias.blockchain.ui.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import com.osias.blockchain.model.enumeration.ChartPeriod

object PeriodSelectorTags {
    const val ROOT = "period_selector_root"
    fun periodButton(period: ChartPeriod) = "period_selector_button_${period.name}"
}

@Composable
fun PeriodSelector(
    selectedPeriod: ChartPeriod,
    onPeriodSelected: (ChartPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .semantics { testTagsAsResourceId = true }
            .testTag(PeriodSelectorTags.ROOT),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChartPeriod.values().forEach { period ->
            FilterChip(
                selected = period == selectedPeriod,
                onClick = { onPeriodSelected(period) },
                label = {
                    Text(
                        text = when (period) {
                            ChartPeriod.ONE_MONTH -> "30d"
                            ChartPeriod.TWO_MONTHS -> "60d"
                            ChartPeriod.SIX_MONTHS -> "180d"
                            ChartPeriod.ONE_YEAR -> "1y"
                            ChartPeriod.TWO_YEARS -> "2y"
                            ChartPeriod.ALL_TIME -> "All"
                        }
                    )
                },
                modifier = Modifier.testTag(PeriodSelectorTags.periodButton(period))
            )
        }
    }
}
