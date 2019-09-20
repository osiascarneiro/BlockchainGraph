package com.osias.blockchain.view.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.osias.blockchain.R
import com.osias.blockchain.model.entity.ChartPoint
import com.osias.blockchain.model.entity.CurrencyValue
import com.osias.blockchain.model.enumeration.ChartPeriod
import com.osias.blockchain.model.enumeration.CurrencyEnum
import com.osias.blockchain.view.dialog.CoinPickerDialog
import com.osias.blockchain.viewmodel.CurrencyViewModel
import kotlinx.android.synthetic.main.fragment_actual_currency.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        selectCoinButton.setOnClickListener {
            CoinPickerDialog.newInstance(NumberPicker.OnValueChangeListener { _, _, newVal ->
                viewModel.coin.value = CurrencyEnum.values()[newVal]
            }).show(childFragmentManager, "Dialog")
        }
    }

    private fun bindItems() {
        viewModel.coin.observe(this, Observer {
            selectCoinButton.text = getString(R.string.moeda_selecionada, it.symbol)
            getAsyncCurrency(it)
        })

        viewModel.period.observe(this, Observer {
            getChart(it)
        })
    }

    private fun getAsyncCurrency(coin: CurrencyEnum) {
        GlobalScope.launch {
            viewModel.getCurrencyByLocale(coin)?.let { value ->
                updateLabel(value)
            }
        }
    }

    private fun updateLabel(value: CurrencyValue) {
        GlobalScope.launch(Dispatchers.Main) {
            lastCurrencyTitle.text = getString(R.string.ultima_cotacao_formatted)
            lastCurrency.text = viewModel.formatCurrency(value.lastValue)
        }
    }

    private fun getChart(period: ChartPeriod) {
        viewModel.getChart(period).observe(this, Observer { charts ->
            charts.lastOrNull()?.let {
                observePoints(viewModel.getPoints(it.time, it.period))
            }
        })
    }

    private fun observePoints(points: LiveData<List<ChartPoint>>) {
        points.observe(this, Observer {
            buildGraph(it)
        })
    }

    private fun buildGraph(points: List<ChartPoint>) {
        val entries = points.map { Entry(it.pointX, it.pointY) }

        val dataSet = LineDataSet(entries, "Currency Evolution")
        dataSet.label
        dataSet.color = Color.BLACK
        dataSet.valueTextColor = Color.DKGRAY
        dataSet.setDrawCircles(false)

        val lineData = LineData(dataSet)
        lineData.setValueFormatter(object : ValueFormatter() {
            override fun getPointLabel(entry: Entry?): String = ""
        })

        chart.data = lineData
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.valueFormatter = object: ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String = viewModel.formatUnixDate(value)
        }
        val yFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String = viewModel.formatCurrency(value.toDouble())
        }
        chart.axisLeft.valueFormatter = yFormatter
        chart.axisRight.valueFormatter = yFormatter
        chart.invalidate()
    }

}