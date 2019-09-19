package com.osias.blockchain.model.entity

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

data class CurrencyValue(
    @ColumnInfo(name = "fifteen_minutes")
    @SerializedName("15m")
    val fifteenMinutesValue: String,
    @ColumnInfo(name = "buy_value")
    @SerializedName("buy")
    val buyValue: String,
    @ColumnInfo(name = "sell_value")
    @SerializedName("sell")
    val sellValue: String,
    @ColumnInfo(name = "last_value")
    @SerializedName("last")
    val lastValue: String,
    @ColumnInfo(name = "symbol")
    @SerializedName("symbol")
    val symbol: String
)