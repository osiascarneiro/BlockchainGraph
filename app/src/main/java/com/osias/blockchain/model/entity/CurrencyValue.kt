package com.osias.blockchain.model.entity

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

data class CurrencyValue(
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