package com.osias.blockchain.viewmodel

import androidx.lifecycle.LiveData
import com.osias.blockchain.model.entity.CurrencyList
import com.osias.blockchain.model.repository.CurrencyRepository
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

class CurrencyViewModel @Inject constructor(
    private val repository: CurrencyRepository
): BaseViewModel() {

    val currency: LiveData<List<CurrencyList>> by lazy { repository.getCurrency() }

    override fun refreshItens() {}

    fun formatCurrency(value: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        return format.format(value)
    }

}