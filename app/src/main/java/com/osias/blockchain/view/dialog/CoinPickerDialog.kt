package com.osias.blockchain.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import com.osias.blockchain.databinding.DialogCoinPickerBinding
import com.osias.blockchain.model.enumeration.CurrencyEnum

class CoinPickerDialog : DialogFragment() {

    companion object {
        const val SELECTED_ITEM_KEY = "selected_item_key_CoinPickerDialog"

        fun newInstance(selectedItem: CurrencyEnum?): CoinPickerDialog {
            val dialog = CoinPickerDialog()
            val bundle = Bundle()
            selectedItem?.let { bundle.putSerializable(SELECTED_ITEM_KEY, it) }
            dialog.arguments = bundle
            return dialog
        }
    }

    private lateinit var binding: DialogCoinPickerBinding

    private var listener: NumberPicker.OnValueChangeListener? = null
    private var selectedItem: CurrencyEnum? = null

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        arguments?.getSerializable(SELECTED_ITEM_KEY)?.let {
            selectedItem = it as? CurrencyEnum
        }

        listener = parentFragment as? NumberPicker.OnValueChangeListener
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogCoinPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurePicker()
        binding.okButton.setOnClickListener { dismiss() }
    }

    private fun configurePicker() {
        binding.numberPicker.minValue = 0
        binding.numberPicker.maxValue = CurrencyEnum.entries.size -1
        binding.numberPicker.displayedValues = CurrencyEnum.entries.map { getString(it.resName) }.toTypedArray()
        selectedItem?.let { binding.numberPicker.value = it.ordinal }
        listener?.let { binding.numberPicker.setOnValueChangedListener(it) }
    }

}