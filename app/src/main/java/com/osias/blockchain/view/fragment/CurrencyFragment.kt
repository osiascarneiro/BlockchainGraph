package com.osias.blockchain.view.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.annotation.MainThread
import androidx.lifecycle.Observer
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.osias.blockchain.R
import com.osias.blockchain.common.utils.DateUtil
import com.osias.blockchain.model.entity.Chart
import com.osias.blockchain.model.entity.ChartPoint
import com.osias.blockchain.model.entity.CurrencyValue
import com.osias.blockchain.model.enumeration.ChartPeriod
import com.osias.blockchain.model.enumeration.CurrencyEnum
import com.osias.blockchain.view.dialog.CoinPickerDialog
import com.osias.blockchain.viewmodel.CurrencyViewModel
import kotlinx.android.synthetic.main.fragment_actual_currency.*
import kotlinx.coroutines.launch

class CurrencyFragment: BaseFragment<CurrencyViewModel>(CurrencyViewModel::class.java),
        NumberPicker.OnValueChangeListener {

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
            CoinPickerDialog.newInstance(viewModel.coin.value).show(childFragmentManager, "Dialog")
        }

        periodSegmentedButton.setOnCheckedChangeListener { _, buttonId ->
            viewModel.period.value = when(buttonId) {
                R.id.thirty_days -> ChartPeriod.ONE_MONTH
                R.id.sixty_days -> ChartPeriod.TWO_MONTHS
                R.id.one_hundred_eighty_days -> ChartPeriod.SIX_MONTHS
                R.id.one_year -> ChartPeriod.ONE_YEAR
                R.id.two_years -> ChartPeriod.TWO_YEARS
                R.id.all_time -> ChartPeriod.ALL_TIME
                else -> ChartPeriod.ONE_MONTH
            }
        }
    }

    override fun onValueChange(picker: NumberPicker?, oldValue: Int, newVal: Int) {
        viewModel.coin.value = CurrencyEnum.values()[newVal]
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

    @MainThread
    private fun loadingDays(loading: Boolean) {
        when(viewModel.period.value) {
            ChartPeriod.ONE_MONTH -> {
                thirty_days.setText(if(loading) R.string.loading else R.string.thirty_days)
            }
            ChartPeriod.TWO_MONTHS -> {
                sixty_days.setText(if(loading) R.string.loading else R.string.sixty_days)
            }
            ChartPeriod.SIX_MONTHS -> {
                one_hundred_eighty_days.setText(if(loading) R.string.loading else R.string.one_hundred_eighty_days)
            }
            ChartPeriod.ONE_YEAR -> {
                one_year.setText(if(loading) R.string.loading else R.string.one_year)
            }
            ChartPeriod.TWO_YEARS -> {
                two_years.setText(if(loading) R.string.loading else R.string.two_years)
            }
            ChartPeriod.ALL_TIME -> {
                all_time.setText(if(loading) R.string.loading else R.string.all_time)
            }
        }
    }

    private fun getAsyncCurrency(coin: CurrencyEnum) {
        viewModel.ioScope.launch {
            viewModel.getCurrencyByLocale(coin)?.let { value ->
                updateLabel(value)
            }
        }
    }

    private fun updateLabel(value: CurrencyValue) {
        viewModel.mainScope.launch {
            lastCurrencyTitle.text = getString(R.string.ultima_cotacao_formatted)
            lastCurrency.text = viewModel.formatCurrency(value.lastValue)
            lastUpdate.text = getString(R.string.last_update, DateUtil.formatDate(value.time))
        }
    }

    private fun getChart(period: ChartPeriod) {
        viewModel.ioScope.launch {
            viewModel.mainScope.launch { loadingDays(true) }
            observePoints(viewModel.getChart(period))
        }
    }

    private suspend fun observePoints(chart: Chart) {
        buildGraph(viewModel.getPoints(chart.time, chart.period))
        viewModel.mainScope.launch { loadingDays(false) }
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