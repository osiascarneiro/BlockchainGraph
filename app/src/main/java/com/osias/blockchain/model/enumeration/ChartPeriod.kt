package com.osias.blockchain.model.enumeration

import com.google.gson.annotations.SerializedName

enum class ChartPeriod {

    @SerializedName("30days")
    ONE_MONTH,
    @SerializedName("60days")
    TWO_MONTHS,
    @SerializedName("180days")
    SIX_MONTHS,
    @SerializedName("1year")
    ONE_YEAR,
    @SerializedName("2years")
    TWO_YEARS,
    @SerializedName("all")
    ALL_TIME

}