package com.osias.blockchain.model.local.dao

import androidx.room.*
import com.osias.blockchain.model.entity.ChartPoint
import com.osias.blockchain.model.enumeration.ChartPeriod
import java.util.*

@Dao
interface ChartPointDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(points: List<ChartPoint>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(points: List<ChartPoint>)

    @Delete
    fun delete(points: List<ChartPoint>)

    @Query("SELECT * FROM chart_point WHERE chart_id = :chartId AND chart_period = :period")
    suspend fun getAllFromChart(chartId: Date, period: ChartPeriod): List<ChartPoint>
}
