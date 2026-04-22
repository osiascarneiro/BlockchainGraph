package com.osias.blockchain.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import com.osias.blockchain.databinding.DialogCoinPickerBinding
import com.osias.blockchain.model.enumeration.CurrencyEnum

class CoinPickerDialog(
    private val listener: NumberPicker.OnValueChangeListener,
    private val selectedItem: CurrencyEnum? = null
) : DialogFragment() {

    private var binding: DialogCoinPickerBinding? = null

    companion object {
        fun newInstance(
            listener: NumberPicker.OnValueChangeListener,
            selectedItem: CurrencyEnum?
        ): CoinPickerDialog = CoinPickerDialog(listener, selectedItem)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogCoinPickerBinding.inflate(inflater, container, false)
        return requireNotNull(binding).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let { b ->
            configurePicker(b)
            b.okButton.setOnClickListener { dismiss() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun configurePicker(b: DialogCoinPickerBinding) {
        b.numberPicker.minValue = 0
        b.numberPicker.maxValue = CurrencyEnum.values().size - 1
        b.numberPicker.displayedValues = CurrencyEnum.values().map { it.symbol }.toTypedArray()
        selectedItem?.let { b.numberPicker.value = it.ordinal }
        b.numberPicker.setOnValueChangedListener(listener)
    }
}
