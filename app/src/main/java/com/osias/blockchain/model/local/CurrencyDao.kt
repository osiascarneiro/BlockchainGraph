package com.osias.blockchain.model.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.osias.blockchain.model.entity.CurrencyList

@Dao
interface CurrencyDao {

    @Query("SELECT * FROM currency_list")
    fun getCurrencies(): LiveData<List<CurrencyList>>

    @Update
    fun updateCurrencies(vararg currency: CurrencyList)

    @Delete
    fun deleteCurrencies(vararg currency: CurrencyList)

    @Insert
    fun insertCurrency(currency: CurrencyList)
}