package com.osias.blockchain.model.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.osias.blockchain.model.entity.Chart
import com.osias.blockchain.model.enumeration.ChartPeriod
import java.util.*

@Dao
interface ChartDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg chart: Chart)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg chart: Chart)

    @Delete
    fun delete(vararg chart: Chart)

    @Query("SELECT * FROM chart")
    fun getAll(): LiveData<List<Chart>>

    @Query("SELECT * FROM chart WHERE id = :time AND period = :period LIMIT 1")
    suspend fun hasChartByTimeAndPeriod(time: Date, period: ChartPeriod): Chart?

    @Query("SELECT * FROM chart WHERE id = :time AND period = :period")
    fun getChartsByTimeAndPeriod(time: Date, period: ChartPeriod): LiveData<List<Chart>>

    @Query("SELECT * FROM chart WHERE period = :period")
    fun getChartsByPeriod(period: ChartPeriod): LiveData<List<Chart>>

}