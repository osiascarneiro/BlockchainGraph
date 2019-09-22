package com.osias.blockchain.model.local.dao

import androidx.room.*
import com.osias.blockchain.model.entity.ChartPoint
import com.osias.blockchain.model.enumeration.ChartPeriod
import java.util.*

@Dao
interface ChartPointDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg point: ChartPoint)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg point: ChartPoint)

    @Delete
    fun delete(vararg point: ChartPoint)

    @Query("SELECT * FROM chart_point WHERE chart_id = :chartId AND chart_period = :period")
    suspend fun getAllFromChart(chartId: Date, period: ChartPeriod): List<ChartPoint>

}