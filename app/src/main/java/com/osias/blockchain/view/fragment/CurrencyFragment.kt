package com.osias.blockchain.view.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.lifecycle.Observer
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.osias.blockchain.R
import com.osias.blockchain.databinding.FragmentActualCurrencyBinding
import com.osias.blockchain.model.entity.Chart
import com.osias.blockchain.model.entity.ChartPoint
import com.osias.blockchain.model.entity.CurrencyValue
import com.osias.blockchain.model.enumeration.ChartPeriod
import com.osias.blockchain.model.enumeration.CurrencyEnum
import com.osias.blockchain.view.dialog.CoinPickerDialog
import com.osias.blockchain.viewmodel.CurrencyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import org.koin.androidx.viewmodel.ext.android.viewModel

class CurrencyFragment : BaseFragment() {

    private var binding: FragmentActualCurrencyBinding? = null

    override val viewModel: CurrencyViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindItems()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentActualCurrencyBinding.inflate(inflater, container, false)
        return requireNotNull(binding).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.selectCoinButton?.setOnClickListener {
            CoinPickerDialog.newInstance(NumberPicker.OnValueChangeListener { _, _, newVal ->
                viewModel.coin.value = CurrencyEnum.values()[newVal]
            }, viewModel.coin.value).show(childFragmentManager, "Dialog")
        }

        binding?.periodSegmentedButton?.setOnCheckedChangeListener { _, buttonId ->
            viewModel.period.value = when (buttonId) {
                R.id.thirty_days             -> ChartPeriod.ONE_MONTH
                R.id.sixty_days              -> ChartPeriod.TWO_MONTHS
                R.id.one_hundred_eighty_days -> ChartPeriod.SIX_MONTHS
                R.id.one_year                -> ChartPeriod.ONE_YEAR
                R.id.two_years               -> ChartPeriod.TWO_YEARS
                R.id.all_time                -> ChartPeriod.ALL_TIME
                else                         -> ChartPeriod.ONE_MONTH
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun bindItems() {
        viewModel.coin.observe(this, Observer { coin ->
            binding?.selectCoinButton?.text = getString(R.string.moeda_selecionada, coin.symbol)
            getAsyncCurrency(coin)
        })

        viewModel.period.observe(this, Observer { period ->
            getChart(period)
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
            binding?.let {
                it.lastCurrencyTitle.text = getString(R.string.ultima_cotacao_formatted)
                it.lastCurrency.text = viewModel.formatCurrency(value.lastValue)
            }
        }
    }

    private fun getChart(period: ChartPeriod) {
        GlobalScope.launch {
            observePoints(viewModel.getChart(period))
        }
    }

    private fun observePoints(chart: Chart) {
        GlobalScope.launch(Dispatchers.Main) {
            buildGraph(viewModel.getPoints(chart.time, chart.period))
        }
    }

    private fun buildGraph(points: List<ChartPoint>) {
        binding?.let { b ->
            val entries = points.map { Entry(it.pointX, it.pointY) }

            val dataSet = LineDataSet(entries, "Currency Evolution")
            dataSet.color = Color.BLACK
            dataSet.valueTextColor = Color.DKGRAY
            dataSet.setDrawCircles(false)

            val lineData = LineData(dataSet)
            lineData.setValueFormatter(object : ValueFormatter() {
                override fun getPointLabel(entry: Entry?): String = ""
            })

            b.chart.data = lineData
            b.chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
            b.chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getAxisLabel(value: Float, axis: AxisBase?): String =
                    viewModel.formatUnixDate(value)
            }
            val yFormatter = object : ValueFormatter() {
                override fun getAxisLabel(value: Float, axis: AxisBase?): String =
                    viewModel.formatCurrency(value.toDouble())
            }
            b.chart.axisLeft.valueFormatter = yFormatter
            b.chart.axisRight.valueFormatter = yFormatter
            b.chart.invalidate()
        }
    }
}
