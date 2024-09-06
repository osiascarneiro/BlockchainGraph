package com.osias.blockchain.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import com.google.gson.annotations.SerializedName
import com.osias.blockchain.model.enumeration.ChartPeriod
import java.util.Date

@Entity(
    tableName = "chart",
    primaryKeys = ["id","period"],
    indices = [Index("id", "period")]
)
data class Chart(
    @ColumnInfo(name = "id")
    var time: Date = Date(),

    @ColumnInfo(name = "name")
    @SerializedName("name")
    var name: String,

    @ColumnInfo(name = "description")
    @SerializedName("description")
    var description: String,

    @ColumnInfo(name = "period")
    var period: ChartPeriod
) {
    @Ignore
    @SerializedName("values")
    var values: List<ChartPoint>? = null
}
