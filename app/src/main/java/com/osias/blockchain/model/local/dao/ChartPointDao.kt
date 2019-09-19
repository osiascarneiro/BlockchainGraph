package com.osias.blockchain.model.local.dao

import androidx.room.*
import com.osias.blockchain.model.entity.ChartPoint

@Dao
interface ChartPointDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg point: ChartPoint)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg point: ChartPoint)

    @Delete
    fun delete(vararg point: ChartPoint)

    @Query("SELECT * FROM chart_point")
    suspend fun getAll(): List<ChartPoint>

    @Query("SELECT * FROM chart_point WHERE chart_id = :chartId")
    suspend fun getAllFromChart(chartId: Long): List<ChartPoint>

}