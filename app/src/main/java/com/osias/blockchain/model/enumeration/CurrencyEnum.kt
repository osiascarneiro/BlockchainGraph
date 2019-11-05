package com.osias.blockchain.model.enumeration

import com.osias.blockchain.R

enum class CurrencyEnum(val symbol: String, val resName: Int) {

    US_DOLLAR("USD", R.string.us_dollar),
    AUS_DOLLAR("AUD", R.string.aus_dollar),
    BR_REAL("BRL", R.string.br_real),
    CA_DOLLAR("CAD", R.string.ca_dollar),
    SW_FRANC("CHF", R.string.sw_franc),
    CH_PESO("CLP", R.string.ch_peso),
    CH_YUAN("CNY", R.string.ch_yuan),
    DN_KRONA("DKK", R.string.dn_krona),
    EURO("EUR", R.string.euro),
    GB_POUND("GBP", R.string.gp_pound),
    HK_DOLLAR("HKD", R.string.hk_dollar),
    IN_RUPEE("INR", R.string.in_rupee),
    IS_KRONA("ISK", R.string.is_krona),
    JP_YEN("JPY", R.string.jp_yen),
    SK_WON("KRW", R.string.sk_won),
    NZ_DOLLAR("NZD", R.string.nz_dollar),
    PL_ZLOTY("PLN", R.string.pl_zloty),
    RU_RUBLE("RUB", R.string.ru_ruble),
    SD_KRONA("SEK", R.string.sd_krona),
    SG_DOLLAR("SGD", R.string.sg_dollar),
    TH_BAIT("THB", R.string.th_bait),
    TW_DOLLAR("TWD", R.string.tw_dollar)

}