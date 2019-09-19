package com.osias.blockchain.model.entity

import com.google.gson.annotations.SerializedName

data class ChartPoint(
    @SerializedName("x")
    val pointX: String,
    @SerializedName("y")
    val pointY: String
)