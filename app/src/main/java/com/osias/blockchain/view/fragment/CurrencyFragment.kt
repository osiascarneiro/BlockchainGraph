package com.osias.blockchain.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.osias.blockchain.R
import com.osias.blockchain.viewmodel.CurrencyViewModel
import kotlinx.android.synthetic.main.fragment_actual_currency.*

class CurrencyFragment: BaseFragment<CurrencyViewModel>(CurrencyViewModel::class.java) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindItems()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_actual_currency, container, false)
    }

    private fun bindItems() {
        viewModel.currency.observe(this, Observer { list ->
            val value = list.lastOrNull()?.unitedStatesDollar?.lastValue
            value?.let { last_currency.text = viewModel.formatCurrency(it) }
        })
    }

}