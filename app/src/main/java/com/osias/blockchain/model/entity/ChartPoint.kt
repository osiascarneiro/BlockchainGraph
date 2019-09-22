package com.osias.blockchain.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import com.google.gson.annotations.SerializedName
import com.osias.blockchain.model.enumeration.ChartPeriod
import java.util.*

@Entity(tableName = "chart_point",
    primaryKeys = ["x", "y","chart_id","chart_period"],
    foreignKeys = [ForeignKey(entity = Chart::class,
                              parentColumns = ["id", "period"],
                              childColumns = ["chart_id", "chart_period"],
                              onDelete = CASCADE,
                              onUpdate = CASCADE)])
data class ChartPoint(
    @SerializedName("x")
    @ColumnInfo(name = "x")
    val pointX: Float,
    @SerializedName("y")
    @ColumnInfo(name = "y")
    val pointY: Float,
    @ColumnInfo(name = "chart_id")
    var chartTime: Date,
    @ColumnInfo(name = "chart_period")
    var chartPeriod: ChartPeriod
    )