package com.osias.blockchain.viewmodel

import androidx.lifecycle.viewModelScope
import com.osias.blockchain.model.entity.Chart
import com.osias.blockchain.model.entity.ChartPoint
import com.osias.blockchain.model.entity.CurrencyValue
import com.osias.blockchain.model.enumeration.ChartPeriod
import com.osias.blockchain.model.enumeration.CurrencyEnum
import com.osias.blockchain.model.repository.ChartRepository
import com.osias.blockchain.model.repository.CurrencyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class CurrencyViewModel(
    private val currencyRepository: CurrencyRepository,
    private val chartsRepository: ChartRepository
) : BaseViewModel() {

    private val _coin = MutableStateFlow(CurrencyEnum.US_DOLLAR)
    val coin: StateFlow<CurrencyEnum> = _coin.asStateFlow()

    private val _period = MutableStateFlow(ChartPeriod.ONE_MONTH)
    val period: StateFlow<ChartPeriod> = _period.asStateFlow()

    private val _uiState = MutableStateFlow(CurrencyUiState())
    val uiState: StateFlow<CurrencyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(_coin, _period) { coin, period -> coin to period }
                .collectLatest { (coin, period) -> loadData(coin, period) }
        }
    }

    fun selectCoin(coin: CurrencyEnum) {
        _coin.value = coin
    }

    fun selectPeriod(period: ChartPeriod) {
        _period.value = period
    }

    private suspend fun loadData(coin: CurrencyEnum, period: ChartPeriod) {
        _uiState.value = CurrencyUiState(isLoading = true)
        try {
            val currency = currencyRepository.getValueByCurrency(coin)
            val chart = chartsRepository.getCharts(period)
            val points = chartsRepository.getChartPoints(chart.time, chart.period)
            _uiState.value = CurrencyUiState(
                isLoading = false,
                formattedPrice = currency?.let { formatCurrency(it.lastValue) } ?: "",
                chartPoints = points
            )
        } catch (e: Exception) {
            _uiState.value = CurrencyUiState(
                isLoading = false,
                errorMessage = e.message ?: "Unknown error"
            )
        }
    }

    override fun refreshItens() {
        viewModelScope.launch {
            loadData(_coin.value, _period.value)
        }
    }

    suspend fun getCurrencyByLocale(coin: CurrencyEnum): CurrencyValue? {
        return currencyRepository.getValueByCurrency(coin)
    }

    fun formatCurrency(value: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        format.currency = Currency.getInstance(_coin.value.symbol)
        return format.format(value)
    }

    fun formatUnixDate(value: Float): String {
        val formatter = java.text.SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        return formatter.format(Date(value.toLong()*1000))
    }

    suspend fun getChart(period: ChartPeriod): Chart {
        return chartsRepository.getCharts(period)
    }

    suspend fun getPoints(chartId: Date, chartPeriod: ChartPeriod): List<ChartPoint> {
        return chartsRepository.getChartPoints(chartId, chartPeriod)
    }

}
