package com.osias.blockchain.model.repository

import androidx.lifecycle.LiveData
import com.osias.blockchain.model.entity.Chart
import com.osias.blockchain.model.local.dao.ChartDao
import com.osias.blockchain.model.local.dao.ChartPointDao
import com.osias.blockchain.model.remote.Service

class ChartRepository(
    val service: Service,
    val chartDao: ChartDao,
    val chartPointDao: ChartPointDao
) {

    fun getCharts(): LiveData<List<Chart>> {
        return chartDao.getAll()
    }

}