package com.osias.blockchain.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "chart")
data class Chart(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    @ColumnInfo(name = "name")
    @SerializedName("name")
    var name: String,
    @ColumnInfo(name = "description")
    @SerializedName("description")
    var description: String
) {
    //    @ColumnInfo(name = "values")
    //TODO: ver como colocar isso no banco
    @Ignore
    @SerializedName("values")
    var values: List<ChartPoint>? = null
}
