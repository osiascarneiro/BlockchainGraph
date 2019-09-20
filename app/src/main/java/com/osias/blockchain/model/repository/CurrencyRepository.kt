package com.osias.blockchain.model.repository

import androidx.lifecycle.LiveData
import com.osias.blockchain.common.utils.DateUtil
import com.osias.blockchain.model.entity.CurrencyValue
import com.osias.blockchain.model.enumeration.CurrencyEnum
import com.osias.blockchain.model.local.dao.CurrencyDao
import com.osias.blockchain.model.remote.Service
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class CurrencyRepository(
    private val service: Service,
    private val dao: CurrencyDao
): BaseRepository() {

    fun getCurrency(): LiveData<List<CurrencyValue>> {
        refreshDbNoRoutine()
        return dao.getCurrencies()
    }

    suspend fun getValueByCurrency(currency: CurrencyEnum): CurrencyValue? {
        val currencyDb = dao.getCurrencyDateAndSymbol(Date(), currency.symbol)
        if(currencyDb == null) refreshDb()
        return dao.getCurrencyDateAndSymbol(Date(), currency.symbol)
    }

    private suspend fun refreshDb() {
        //Limitando a atualizacao dos dados a cada hora
        //TODO: talvez possa ser reduzido
        val dbCurrency = dao.hasCurrencyDate(DateUtil.stripMinutes(Date()))
        dbCurrency?.let { return }

        val currency = service.actualCurrency().execute()
        if(!currency.isSuccessful) {
            delegate?.onError(Error(currency.errorBody().toString()))
        } else {
            currency.body()?.let {map ->
                map.forEach {
                    val currencyDb = it.value
                    currencyDb.currencyKey = it.key
                    currencyDb.time = Date()
                    dao.insertCurrency(currencyDb)
                }
            }
        }
    }

    private fun refreshDbNoRoutine() {
        GlobalScope.launch {
            refreshDb()
        }
    }

}