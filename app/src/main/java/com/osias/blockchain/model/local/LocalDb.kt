package com.osias.blockchain.model.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.osias.blockchain.common.converter.ChartPeriodConverter
import com.osias.blockchain.common.converter.DateConverter
import com.osias.blockchain.model.entity.Chart
import com.osias.blockchain.model.entity.ChartPoint
import com.osias.blockchain.model.entity.CurrencyList
import com.osias.blockchain.model.local.dao.ChartDao
import com.osias.blockchain.model.local.dao.ChartPointDao
import com.osias.blockchain.model.local.dao.CurrencyDao

/**
 * Created by osiascarneiro on 05/12/17.
 */

@Database(entities = [Chart::class,ChartPoint::class,CurrencyList::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class, ChartPeriodConverter::class)
abstract class BancoLocal : RoomDatabase() {
    abstract fun currencyDao(): CurrencyDao
    abstract fun chartDao(): ChartDao
    abstract fun chartPointDao(): ChartPointDao
}