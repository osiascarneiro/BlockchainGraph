package com.osias.blockchain.model.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.osias.blockchain.model.entity.Chart

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

}