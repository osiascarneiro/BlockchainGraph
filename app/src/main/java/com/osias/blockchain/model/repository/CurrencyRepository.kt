package com.osias.blockchain.model.repository

import androidx.lifecycle.LiveData
import com.osias.blockchain.common.utils.DateUtil
import com.osias.blockchain.model.entity.CurrencyList
import com.osias.blockchain.model.local.dao.CurrencyDao
import com.osias.blockchain.model.remote.Service
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

/**
 * Interface para expor o erro do servidor
 */
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
            //Limitando a atualizacao dos dados a cada hora
            //TODO: talvez possa ser reduzido
            val dbCurrency = dao.getCurrencyDate(DateUtil.stripMinutes(Date()))
            dbCurrency?.let { return@launch }

            val currency = service.actualCurrency().execute() ?: return@launch
            if(!currency.isSuccessful) {
                delegate?.onError(Error(currency.errorBody().toString()))
            } else {
                currency.body()?.let {
                    it.time = Date()
                    dao.insertCurrency(it)
                }
            }
        }
    }

}