package com.osias.blockchain.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.osias.blockchain.model.entity.Chart
import com.osias.blockchain.model.entity.ChartPoint
import com.osias.blockchain.model.entity.CurrencyValue
import com.osias.blockchain.model.enumeration.ChartPeriod
import com.osias.blockchain.model.enumeration.CurrencyEnum
import com.osias.blockchain.model.repository.ChartRepository
import com.osias.blockchain.model.repository.CurrencyRepository
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

class CurrencyViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository,
    private val chartsRepository: ChartRepository
): BaseViewModel() {

    val period = MutableLiveData(ChartPeriod.ONE_MONTH)
    val coin = MutableLiveData(CurrencyEnum.US_DOLLAR)

    override fun refreshItens() {}

    suspend fun getCurrencyByLocale(coin: CurrencyEnum): CurrencyValue? {
        return currencyRepository.getValueByCurrency(coin)
    }

    fun formatCurrency(value: Double): String {
        //TODO: Pegar o curency dinamicamente talvez usar o symbol do objeto
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        return format.format(value)
    }

    fun formatUnixDate(value: Float): String {
        val formatter = java.text.SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        return formatter.format(Date(value.toLong()*1000))
    }

    fun getChart(period: ChartPeriod): LiveData<List<Chart>> {
        return chartsRepository.getCharts(period)
    }

    fun getPoints(chartId: Date, chartPeriod: ChartPeriod): LiveData<List<ChartPoint>> {
        return chartsRepository.getChartPoints(chartId, chartPeriod)
    }

}