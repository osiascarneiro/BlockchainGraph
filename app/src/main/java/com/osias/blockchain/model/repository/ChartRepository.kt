package com.osias.blockchain.model.repository

import androidx.lifecycle.LiveData
import com.osias.blockchain.model.entity.Chart
import com.osias.blockchain.model.entity.ChartPoint
import com.osias.blockchain.model.enumeration.ChartPeriod
import com.osias.blockchain.model.local.dao.ChartDao
import com.osias.blockchain.model.local.dao.ChartPointDao
import com.osias.blockchain.model.remote.Service
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class ChartRepository(
    private val service: Service,
    private val chartDao: ChartDao,
    private val chartPointDao: ChartPointDao
): BaseRepository() {

    fun getCharts(period: ChartPeriod): LiveData<List<Chart>> {
        refreshDb(period)
        return chartDao.getChartsByPeriod(period)
    }

    //O refresh db nao eh chamado pois ja eh atualizado quando chamamos o
    //getCharts, que pela logica é sempre chamado antes desse
    //pois para chamar esse metodo eh necessario o id do Chart
    fun getChartPoints(chartId: Date, chartPeriod: ChartPeriod): LiveData<List<ChartPoint>> {
        return chartPointDao.getAllFromChart(chartId, chartPeriod)
    }

    private fun refreshDb(period: ChartPeriod) {
        GlobalScope.launch {
            val dbChart = chartDao.hasChartByTimeAndPeriod(Date(), period)
            dbChart?.let { return@launch }

            val result = service.getCurrencyChart(period).execute()
            if(!result.isSuccessful) {
                delegate?.onError(Error(result.errorBody().toString()))
            } else {
                result.body()?.let { chart ->
                    chart.period = period
                    chart.time = Date()
                    chartDao.insert(chart)
                    chart.values?.forEach {
                        it.chartTime = chart.time
                        it.chartPeriod = chart.period
                        chartPointDao.insert(it)
                    }
                }
            }
        }
    }

}