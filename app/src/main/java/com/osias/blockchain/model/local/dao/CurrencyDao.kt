package com.osias.blockchain.model.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.osias.blockchain.model.entity.CurrencyValue
import java.util.Date

@Dao
interface CurrencyDao {

    @Query("SELECT * FROM currency")
    fun getCurrencies(): LiveData<List<CurrencyValue>>

    @Query("SELECT * FROM currency WHERE time >= :date LIMIT 1")
    suspend fun hasCurrencyDate(date: Date): CurrencyValue?

    @Query("SELECT * FROM currency WHERE time >= :date")
    suspend fun getCurrencyDate(date: Date): List<CurrencyValue>

    @Query("SELECT * FROM currency WHERE time >= :date AND currency_symbol = :currency ORDER BY time DESC LIMIT 1")
    suspend fun getCurrencyDateAndSymbol(date: Date, currency: String): CurrencyValue?

    @Update
    fun updateCurrencies(vararg currency: CurrencyValue)

    @Delete
    fun deleteCurrencies(vararg currency: CurrencyValue)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCurrency(currency: CurrencyValue)
}