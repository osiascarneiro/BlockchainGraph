package com.osias.blockchain.model.remote

import com.osias.blockchain.model.entity.Chart
import com.osias.blockchain.model.entity.CurrencyList
import com.osias.blockchain.model.enumeration.ChartPeriod
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface Service  {

    @GET("ticker")
    fun actualCurrency(): Call<List<Chart>>

    @GET("charts/market-price?format=json")
    fun getCurrencyChart(@Query("timespan") time: ChartPeriod): Call<CurrencyList?>

}