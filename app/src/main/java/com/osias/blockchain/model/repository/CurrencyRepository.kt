package com.osias.blockchain.model.repository

import com.osias.blockchain.common.utils.DateUtil
import com.osias.blockchain.model.entity.CurrencyValue
import com.osias.blockchain.model.enumeration.CurrencyEnum
import com.osias.blockchain.model.local.dao.CurrencyDao
import com.osias.blockchain.model.remote.Service
import java.util.*

class CurrencyRepository(
    private val service: Service,
    private val dao: CurrencyDao,
    private val dateProvider: DateProvider = object: DateProvider {
        override fun getDate(): Date {
            return Date()
        }
    }
): BaseRepository() {

    suspend fun getValueByCurrency(currency: CurrencyEnum): CurrencyValue? {
        val currencyDb = dao.getCurrencyDateAndSymbol(dateProvider.getDate(), currency.symbol)
        if(currencyDb == null) refreshDb()
        return dao.getCurrencyDateAndSymbol(dateProvider.getDate(), currency.symbol)
    }

    suspend fun forceUpdate(currency: CurrencyEnum): CurrencyValue? {
        refreshDb(true)
        return dao.getCurrencyDateAndSymbol(dateProvider.getDate(), currency.symbol)
    }

    private suspend fun refreshDb(force: Boolean = false) {
        //Limitando a atualizacao dos dados a cada hora
        //TODO: talvez possa ser reduzido
        val dbCurrency = dao.hasCurrencyDate(DateUtil.stripMinutes(dateProvider.getDate()))
        if(!force && dbCurrency != null) return

        val currency = service.actualCurrency().execute()
        if(!currency.isSuccessful) {
            delegate?.onError(Error(currency.errorBody().toString()))
        } else {
            currency.body()?.let {map ->
                map.forEach {
                    val currencyDb = it.value
                    currencyDb.currencyKey = it.key
                    currencyDb.time = dateProvider.getDate()
                    dao.insertCurrency(currencyDb)
                }
            }
        }
    }

}