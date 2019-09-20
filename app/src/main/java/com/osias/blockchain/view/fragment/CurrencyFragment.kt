package com.osias.blockchain.view.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.osias.blockchain.R
import com.osias.blockchain.model.entity.ChartPoint
import com.osias.blockchain.model.enumeration.ChartPeriod
import com.osias.blockchain.viewmodel.CurrencyViewModel
import kotlinx.android.synthetic.main.fragment_actual_currency.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
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

    private fun bindItems() {
        viewModel.currency.observe(this, Observer { list ->
            val value = list.lastOrNull()?.unitedStatesDollar?.lastValue
            value?.let { last_currency.text = viewModel.formatCurrency(it) }
        })

        viewModel.period.observe(this, Observer {
            getChart(it)
        })
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
        dataSet.color = Color.BLACK
        dataSet.valueTextColor = Color.DKGRAY
        dataSet.setDrawCircles(false)
        val lineData = LineData(dataSet)
        chart.data = lineData
        chart.invalidate()
    }

}