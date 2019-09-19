package com.osias.blockchain.model.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.osias.blockchain.model.entity.Chart
import com.osias.blockchain.model.entity.CurrencyList

/**
 * Created by osiascarneiro on 05/12/17.
 */

@Database(entities = [Chart::class,CurrencyList::class], version = 1, exportSchema = false)
abstract class BancoLocal : RoomDatabase() {
    abstract fun currencyDao(): CurrencyDao
}