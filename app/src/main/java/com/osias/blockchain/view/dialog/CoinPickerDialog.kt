package com.osias.blockchain.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import com.osias.blockchain.R
import com.osias.blockchain.model.enumeration.CurrencyEnum
import kotlinx.android.synthetic.main.dialog_coin_picker.*

class CoinPickerDialog(val listener: NumberPicker.OnValueChangeListener): DialogFragment() {

    companion object {
        fun newInstance(listener: NumberPicker.OnValueChangeListener): CoinPickerDialog {
            return CoinPickerDialog(listener)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_coin_picker, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        configurePicker()
        okButton.setOnClickListener { dismiss() }
    }

    private fun configurePicker() {
        numberPicker.minValue = 0
        numberPicker.maxValue = CurrencyEnum.values().size -1
        numberPicker.displayedValues = CurrencyEnum.values().map { it.symbol }.toTypedArray()
        numberPicker.setOnValueChangedListener(listener)
    }

}