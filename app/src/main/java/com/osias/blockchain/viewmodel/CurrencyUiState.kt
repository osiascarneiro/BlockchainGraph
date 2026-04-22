package com.osias.blockchain.viewmodel

import com.osias.blockchain.model.entity.ChartPoint

data class CurrencyUiState(
    val isLoading: Boolean = true,
    val formattedPrice: String = "",
    val chartPoints: List<ChartPoint> = emptyList(),
    val errorMessage: String? = null
)
