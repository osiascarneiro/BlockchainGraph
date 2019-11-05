package com.osias.blockchain.model.repository

import com.osias.blockchain.common.utils.DateUtil
import com.osias.blockchain.model.entity.Chart
import com.osias.blockchain.model.entity.ChartPoint
import com.osias.blockchain.model.enumeration.ChartPeriod
import com.osias.blockchain.model.local.dao.ChartDao
import com.osias.blockchain.model.local.dao.ChartPointDao
import com.osias.blockchain.model.remote.Service
import java.util.*

class ChartRepository(
    private val service: Service,
    private val chartDao: ChartDao,
    private val chartPointDao: ChartPointDao,
    private val dateProvider: DateProvider = object : DateProvider {
        override fun getDate(): Date {
            return Date()
        }

    }
): BaseRepository() {

    suspend fun getCharts(period: ChartPeriod): Chart {
        refreshDb(period)
        return chartDao.getChartByTimeAndPeriod(DateUtil.stripMinutes(dateProvider.getDate()), period)
    }

    //O refresh db nao eh chamado pois ja eh atualizado quando chamamos o
    //getCharts, que pela logica é sempre chamado antes desse
    //pois para chamar esse metodo eh necessario o id do Chart
    suspend fun getChartPoints(chartId: Date, chartPeriod: ChartPeriod): List<ChartPoint> {
        return chartPointDao.getAllFromChart(DateUtil.stripMinutes(chartId), chartPeriod)
    }

    private suspend fun refreshDb(period: ChartPeriod) {
        val dbChart = chartDao.hasChartByTimeAndPeriod(DateUtil.stripMinutes(dateProvider.getDate()), period)
        dbChart?.let { return }

        val result = service.getCurrencyChart(period).execute()
        if(!result.isSuccessful) {
            delegate?.onError(Error(result.errorBody().toString()))
        } else {
            result.body()?.let { chart ->
                chart.period = period
                chart.time = dateProvider.getDate()
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