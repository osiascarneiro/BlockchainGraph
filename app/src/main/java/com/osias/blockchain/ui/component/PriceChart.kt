package com.osias.blockchain.ui.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.osias.blockchain.model.entity.ChartPoint
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.VicoZoomState
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import java.math.BigDecimal
import java.math.RoundingMode

object PriceChartTags {
    const val ROOT = "price_chart_root"
}

private fun Float.truncateDecimalPlaces() = BigDecimal(this.toDouble())
    .setScale(2, RoundingMode.HALF_UP)
    .toFloat()

@Composable
fun PriceChart(
    points: List<ChartPoint>,
    formatXLabel: (Float) -> String,
    formatYLabel: (Double) -> String,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(points) {
        if (points.isNotEmpty()) {
            modelProducer.runTransaction {
                lineSeries {
                    series(
                        // Round x-values to integers — Vico requires at most 4 decimal places
                        // Real blockchain data uses Unix timestamps (integers)
                        x = points.map { it.pointX },
                        y = points.map { it.pointY }
                    )
                }
            }
        }
        // When points is empty, leave the model producer untouched — Vico renders a blank chart
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(
                label = rememberAxisLabelComponent(),
                valueFormatter = { _, value, _ ->
                    formatYLabel(value)
                }
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                label = rememberAxisLabelComponent(),
                valueFormatter = { _, value, _ ->
                    formatXLabel(value.toFloat())
                }
            )
        ),
        zoomState = rememberVicoZoomState(
            initialZoom = Zoom.Content,
            zoomEnabled = false
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxSize()
            .semantics { testTagsAsResourceId = true }
            .testTag(PriceChartTags.ROOT)
    )
}
