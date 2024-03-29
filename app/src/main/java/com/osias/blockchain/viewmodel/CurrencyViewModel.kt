package com.osias.blockchain.viewmodel

import androidx.lifecycle.MutableLiveData
import com.osias.blockchain.model.entity.Chart
import com.osias.blockchain.model.entity.ChartPoint
import com.osias.blockchain.model.entity.CurrencyValue
import com.osias.blockchain.model.enumeration.ChartPeriod
import com.osias.blockchain.model.enumeration.CurrencyEnum
import com.osias.blockchain.model.repository.ChartRepository
import com.osias.blockchain.model.repository.CurrencyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.CancellationException
import javax.inject.Inject

class CurrencyViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository,
    private val chartsRepository: ChartRepository
): BaseViewModel() {

    val period = MutableLiveData(ChartPeriod.ONE_MONTH)
    val coin = MutableLiveData(CurrencyEnum.US_DOLLAR)
    val mainScope = MainScope()
    val ioScope = CoroutineScope(Dispatchers.IO)

    override fun refreshItens() {}

    suspend fun getCurrencyByLocale(coin: CurrencyEnum): CurrencyValue? {
        return currencyRepository.getValueByCurrency(coin)
    }

    suspend fun forceUpdateCurrency(): CurrencyValue? {
        if(coin.value == null) return null
        return currencyRepository.forceUpdate(coin.value!!)
    }

    fun formatCurrency(value: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        format.currency = Currency.getInstance(coin.value?.symbol)
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

    override fun onCleared() {
        super.onCleared()
        mainScope.cancel(CancellationException("View model terminated"))
        ioScope.cancel(CancellationException("View model terminated"))
    }

}