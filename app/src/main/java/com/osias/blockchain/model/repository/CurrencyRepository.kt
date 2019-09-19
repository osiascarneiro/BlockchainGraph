package com.osias.blockchain.model.repository

import androidx.lifecycle.LiveData
import com.osias.blockchain.model.entity.CurrencyList
import com.osias.blockchain.model.enumeration.ChartPeriod
import com.osias.blockchain.model.local.CurrencyDao
import com.osias.blockchain.model.remote.Service
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface CurrencyRepositoryErrorDelegate {
    fun onError(error: Error)
}

class CurrencyRepository(
    private val service: Service,
    private val dao: CurrencyDao
) {

    var delegate: CurrencyRepositoryErrorDelegate? = null

    fun getCurrency(): LiveData<List<CurrencyList>> {
        refreshDb()
        return dao.getCurrencies()
    }

    private fun refreshDb() {
        GlobalScope.launch {
            val currency = service.actualCurrency().execute() ?: return@launch
            if(!currency.isSuccessful) { delegate?.onError(Error(currency.errorBody().toString())) }
            else { currency.body()?.let { dao.insertCurrency(it) } }
        }
    }

}