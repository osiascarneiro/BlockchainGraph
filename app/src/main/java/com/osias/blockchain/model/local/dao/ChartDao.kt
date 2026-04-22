package com.osias.blockchain.model.local.dao

import androidx.room.*
import com.osias.blockchain.model.entity.Chart
import com.osias.blockchain.model.enumeration.ChartPeriod
import java.util.*

@Dao
interface ChartDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(charts: List<Chart>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(charts: List<Chart>)

    @Delete
    fun delete(charts: List<Chart>)

    @Query("SELECT * FROM chart WHERE id = :time AND period = :period LIMIT 1")
    suspend fun hasChartByTimeAndPeriod(time: Date, period: ChartPeriod): Chart?

    @Query("SELECT * FROM chart WHERE id = :time AND period = :period LIMIT 1")
    suspend fun getChartByTimeAndPeriod(time: Date, period: ChartPeriod): Chart
}
