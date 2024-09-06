package com.osias.blockchain.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import java.util.Date

@Entity(tableName = "currency",
        primaryKeys = ["time","currency_symbol"])
data class CurrencyValue(
    @ColumnInfo(name = "currency_symbol")
    var currencyKey: String,
    @ColumnInfo(name = "time")
    var time: Date = Date(),
    @ColumnInfo(name = "fifteen_minutes")
    @SerializedName("15m")
    val fifteenMinutesValue: Double,
    @ColumnInfo(name = "buy_value")
    @SerializedName("buy")
    val buyValue: Double,
    @ColumnInfo(name = "sell_value")
    @SerializedName("sell")
    val sellValue: Double,
    @ColumnInfo(name = "last_value")
    @SerializedName("last")
    val lastValue: Double,
    @ColumnInfo(name = "symbol")
    @SerializedName("symbol")
    val symbol: String
)