package com.osias.blockchain.model.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "currency_list")
data class CurrencyList(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @Embedded(prefix = "usd")
    @SerializedName("USD")
    val unitedStatesDollar: CurrencyValue,
    @Embedded(prefix = "aud")
    @SerializedName("AUD")
    val austrianDollar: CurrencyValue,
    @Embedded(prefix = "brl")
    @SerializedName("BRL")
    val brazilianReal: CurrencyValue,
    @Embedded(prefix = "cad")
    @SerializedName("CAD")
    val canadianDollar: CurrencyValue,
    @Embedded(prefix = "chf")
    @SerializedName("CHF")
    val swissFranc: CurrencyValue,
    @Embedded(prefix = "clp")
    @SerializedName("CLP")
    val chileanPeso: CurrencyValue,
    @Embedded(prefix = "cny")
    @SerializedName("CNY")
    val chineseYuan: CurrencyValue,
    @Embedded(prefix = "dkk")
    @SerializedName("DKK")
    val danishKrone: CurrencyValue,
    @Embedded(prefix = "eur")
    @SerializedName("EUR")
    val euro: CurrencyValue,
    @Embedded(prefix = "gpb")
    @SerializedName("GBP")
    val poundSterling: CurrencyValue,
    @Embedded(prefix = "hkd")
    @SerializedName("HKD")
    val hongKongDollar: CurrencyValue,
    @Embedded(prefix = "inr")
    @SerializedName("INR")
    val indianRupee: CurrencyValue,
    @Embedded(prefix = "isk")
    @SerializedName("ISK")
    val icelandKrona: CurrencyValue,
    @Embedded(prefix = "jpy")
    @SerializedName("JPY")
    val japaneseYen: CurrencyValue,
    @Embedded(prefix = "krw")
    @SerializedName("KRW")
    val southKoreanWon: CurrencyValue,
    @Embedded(prefix = "nzd")
    @SerializedName("NZD")
    val newZealandDollar: CurrencyValue,
    @Embedded(prefix = "pln")
    @SerializedName("PLN")
    val polanZloty: CurrencyValue,
    @Embedded(prefix = "rub")
    @SerializedName("RUB")
    val russianRuble: CurrencyValue,
    @Embedded(prefix = "sek")
    @SerializedName("SEK")
    val swedishKrona: CurrencyValue,
    @Embedded(prefix = "sgd")
    @SerializedName("SGD")
    val singaporeDollar: CurrencyValue,
    @Embedded(prefix = "thb")
    @SerializedName("THB")
    val thaiBaht: CurrencyValue,
    @Embedded(prefix = "twd")
    @SerializedName("TWD")
    val taiwanDollar: CurrencyValue
)