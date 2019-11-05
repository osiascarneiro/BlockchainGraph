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

    private var listener: NumberPicker.OnValueChangeListener? = null
    private var selectedItem: CurrencyEnum? = null

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
        numberPicker.displayedValues = CurrencyEnum.values().map { getString(it.resName) }.toTypedArray()
        selectedItem?.let { numberPicker.value = it.ordinal }
        listener?.let { numberPicker.setOnValueChangedListener(it) }
    }

}