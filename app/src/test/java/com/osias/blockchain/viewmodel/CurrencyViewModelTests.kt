package com.osias.blockchain.viewmodel

import com.nhaarman.mockitokotlin2.whenever
import com.osias.blockchain.model.entity.Chart
import com.osias.blockchain.model.entity.ChartPoint
import com.osias.blockchain.model.entity.CurrencyValue
import com.osias.blockchain.model.enumeration.ChartPeriod
import com.osias.blockchain.model.enumeration.CurrencyEnum
import com.osias.blockchain.model.local.dao.ChartDao
import com.osias.blockchain.model.local.dao.ChartPointDao
import com.osias.blockchain.model.local.dao.CurrencyDao
import com.osias.blockchain.model.repository.ChartRepository
import com.osias.blockchain.model.repository.CurrencyRepository
import com.osias.blockchain.model.repository.DateProvider
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import java.util.*

class CurrencyViewModelTests: BaseViewModelTests() {

    lateinit var viewModel: CurrencyViewModel

    @Mock
    lateinit var chartDao: ChartDao

    @Mock
    lateinit var chartPointDao: ChartPointDao

    @Mock
    lateinit var currencyDao: CurrencyDao

    lateinit var chartRepository: ChartRepository

    lateinit var currencyRepository: CurrencyRepository

    @Mock
    lateinit var dateProvider: DateProvider

    private val date = Date()

    @Before
    override fun setUp() {
        super.setUp()
        whenever(dateProvider.getDate()).thenReturn(date)
        chartRepository = ChartRepository(blockchainService, chartDao, chartPointDao, dateProvider)
        currencyRepository = CurrencyRepository(blockchainService, currencyDao, dateProvider)
        viewModel = CurrencyViewModel(currencyRepository, chartRepository)
    }

    @Test
    fun `Requisitando moeda atual sem atualizar db`() {
        runBlockingTest {
            val valueMock = CurrencyValue("USD", date, 123.43, 123.42, 123.32,123.32, "$")

            whenever(currencyDao.getCurrencyDateAndSymbol(date, CurrencyEnum.US_DOLLAR.symbol)).thenReturn(valueMock)

            val value = viewModel.getCurrencyByLocale(CurrencyEnum.US_DOLLAR)

            assert(value == valueMock)
        }
    }

    @Test
    fun `Formatando moeda`() {
        viewModel.coin.value = CurrencyEnum.US_DOLLAR
        val usDollar = viewModel.formatCurrency(12.34)

        assert(usDollar == "$12.34")
    }

    @Test
    fun `Formatando data`() {
        //1569192481
        //09/22/2019 @ 10:48pm (UTC)
        val data = viewModel.formatUnixDate(1569192481f)

        assert(data == "22/09/19")
    }

    @Test
    fun `Requisitando grafico`() {
        runBlockingTest {
            val mockChart = Chart(Date(), "Market Price (USD)", "Average USD market price across major bitcoin exchanges.", ChartPeriod.ONE_MONTH)

            whenever(chartDao.hasChartByTimeAndPeriod(date, ChartPeriod.ONE_MONTH)).thenReturn(mockChart)
            whenever(chartDao.getChartByTimeAndPeriod(date, ChartPeriod.ONE_MONTH)).thenReturn(mockChart)

            val chart = viewModel.getChart(ChartPeriod.ONE_MONTH)

            assert(chart == mockChart)
        }
    }

    @Test
    fun `Requisitando pontos do grafico`() {
        runBlockingTest {
            val mockPoints = arrayListOf<ChartPoint>()
            for(i in 0..10) {
                val point = ChartPoint(123.32f, 123.32f, date, ChartPeriod.ONE_MONTH)
                mockPoints.add(point)
            }

            whenever(chartPointDao.getAllFromChart(date, ChartPeriod.ONE_MONTH)).thenReturn(mockPoints)

            val points = viewModel.getPoints(date, ChartPeriod.ONE_MONTH)

            assert(points == mockPoints)
        }
    }
}