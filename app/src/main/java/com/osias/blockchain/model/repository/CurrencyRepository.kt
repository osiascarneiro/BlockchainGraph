package com.osias.blockchain.model.repository

import androidx.lifecycle.LiveData
import com.osias.blockchain.model.entity.CurrencyList
import com.osias.blockchain.model.enumeration.ChartPeriod
import com.osias.blockchain.model.local.CurrencyDao
import com.osias.blockchain.model.remote.Service
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CurrencyRepository(
    val service: Service,
    val dao: CurrencyDao
) {

    fun getCurrency(period: ChartPeriod): LiveData<List<CurrencyList>> {
        refreshDb(period)
        return dao.getCurrencies()
    }

    private fun refreshDb(period: ChartPeriod) {
        GlobalScope.launch {
            //TODO: passar o periodo
            val currency = service.getCurrencyChart(period).execute() ?: return@launch
            currency.body()?.let { dao.insertCurrency(it) }
        }
    }

}