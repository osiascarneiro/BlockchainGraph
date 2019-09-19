package com.osias.blockchain.model.entity

sealed class ApiResponse<T>(
    val result: T?,
    val error: Throwable?
) {
}