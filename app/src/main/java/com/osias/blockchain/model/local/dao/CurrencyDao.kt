package com.osias.blockchain.model.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.osias.blockchain.model.entity.CurrencyList
import java.util.*

@Dao
interface CurrencyDao {

    @Query("SELECT * FROM currency_list")
    fun getCurrencies(): LiveData<List<CurrencyList>>

    @Query("SELECT * FROM currency_list WHERE time = :date LIMIT 1")
    suspend fun getCurrencyDate(date: Date): CurrencyList?

    @Update
    fun updateCurrencies(vararg currency: CurrencyList)

    @Delete
    fun deleteCurrencies(vararg currency: CurrencyList)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCurrency(currency: CurrencyList)
}