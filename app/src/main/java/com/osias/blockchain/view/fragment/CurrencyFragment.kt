package com.osias.blockchain.view.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.annotation.MainThread
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.osias.blockchain.R
import com.osias.blockchain.common.utils.DateUtil
import com.osias.blockchain.databinding.FragmentActualCurrencyBinding
import com.osias.blockchain.model.entity.Chart
import com.osias.blockchain.model.entity.ChartPoint
import com.osias.blockchain.model.entity.CurrencyValue
import com.osias.blockchain.model.enumeration.ChartPeriod
import com.osias.blockchain.model.enumeration.CurrencyEnum
import com.osias.blockchain.view.dialog.CoinPickerDialog
import com.osias.blockchain.viewmodel.CurrencyViewModel
import kotlinx.coroutines.launch

class CurrencyFragment: BaseFragment<CurrencyViewModel>(CurrencyViewModel::class.java),
        NumberPicker.OnValueChangeListener {

    private var _binding: FragmentActualCurrencyBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindItems()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActualCurrencyBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selectCoinButton.setOnClickListener {
            CoinPickerDialog.newInstance(viewModel.coin.value).show(childFragmentManager, "Dialog")
        }

        binding.periodSegmentedButton.setOnCheckedChangeListener { _, buttonId ->
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

        binding.forceUpdateFab.setOnClickListener {
            forceUpdateCurrency()
        }
    }

    override fun onValueChange(picker: NumberPicker?, oldValue: Int, newVal: Int) {
        viewModel.coin.value = CurrencyEnum.entries[newVal]
    }

    private fun bindItems() {
        viewModel.coin.observe(this) {
            binding.selectCoinButton.text = getString(R.string.moeda_selecionada, it.symbol)
            getAsyncCurrency(it)
        }

        viewModel.period.observe(this) {
            getChart(it)
        }
    }

    @MainThread
    private fun loadingDays(loading: Boolean) {
        when(viewModel.period.value) {
            ChartPeriod.ONE_MONTH ->
                binding.thirtyDays.setText(if(loading) R.string.loading else R.string.thirty_days)
            ChartPeriod.TWO_MONTHS ->
                binding.sixtyDays.setText(if(loading) R.string.loading else R.string.sixty_days)
            ChartPeriod.SIX_MONTHS ->
                binding.oneHundredEightyDays.setText(if(loading) R.string.loading else R.string.one_hundred_eighty_days)
            ChartPeriod.ONE_YEAR ->
                binding.oneYear.setText(if(loading) R.string.loading else R.string.one_year)
            ChartPeriod.TWO_YEARS ->
                binding.twoYears.setText(if(loading) R.string.loading else R.string.two_years)
            ChartPeriod.ALL_TIME ->
                binding.allTime.setText(if(loading) R.string.loading else R.string.all_time)
            null -> return
        }
    }

    private fun getAsyncCurrency(coin: CurrencyEnum) {
        viewModel.ioScope.launch {
            viewModel.getCurrencyByLocale(coin)?.let { value ->
                updateLabel(value)
            }
        }
    }

    private fun forceUpdateCurrency() {
        viewModel.ioScope.launch {
            viewModel.forceUpdateCurrency()?.let { value ->
                updateLabel(value)
            }
        }
    }

    private fun updateLabel(value: CurrencyValue) {
        viewModel.mainScope.launch {
            binding.lastCurrencyTitle.text = getString(R.string.ultima_cotacao_formatted)
            binding.lastCurrency.text = viewModel.formatCurrency(value.lastValue)
            binding.lastUpdate.text = getString(R.string.last_update, DateUtil.formatDate(value.time))
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

        binding.chart.data = lineData
        binding.chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.chart.xAxis.valueFormatter = object: ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String = viewModel.formatUnixDate(value)
        }
        val yFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String = viewModel.formatCurrency(value.toDouble())
        }
        binding.chart.axisLeft.valueFormatter = yFormatter
        binding.chart.axisRight.valueFormatter = yFormatter
        binding.chart.invalidate()
    }

}