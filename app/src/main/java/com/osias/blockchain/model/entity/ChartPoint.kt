package com.osias.blockchain.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import com.google.gson.annotations.SerializedName

@Entity(tableName = "chart_point", foreignKeys = [ForeignKey(entity = Chart::class,
                                                             parentColumns = ["id"],
                                                             childColumns = ["chartId"],
                                                             onDelete = CASCADE,
                                                             onUpdate = CASCADE)])
data class ChartPoint(
    @SerializedName("x")
    @ColumnInfo(name = "x")
    val pointX: String,
    @SerializedName("y")
    @ColumnInfo(name = "y")
    val pointY: String,
    @ColumnInfo(name = "chart_id")
    val chartId: Long
)