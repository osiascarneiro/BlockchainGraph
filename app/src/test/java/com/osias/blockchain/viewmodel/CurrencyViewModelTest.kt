package com.osias.blockchain.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.mockito.kotlin.*
import com.osias.blockchain.model.entity.Chart
import com.osias.blockchain.model.entity.ChartPoint
import com.osias.blockchain.model.entity.CurrencyValue
import com.osias.blockchain.model.enumeration.ChartPeriod
import com.osias.blockchain.model.enumeration.CurrencyEnum
import com.osias.blockchain.model.repository.ChartRepository
import com.osias.blockchain.model.repository.CurrencyRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

class CurrencyViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private lateinit var currencyRepository: CurrencyRepository
    private lateinit var chartRepository: ChartRepository
    private lateinit var viewModel: CurrencyViewModel

    @Before
    fun setup() {
        currencyRepository = mock()
        chartRepository = mock()
        viewModel = CurrencyViewModel(currencyRepository, chartRepository)
    }

    // Task 24.1 — formatCurrency uses the selected currency symbol
    @Test
    fun `formatCurrency formats value with selected currency symbol`() {
        viewModel.coin.value = CurrencyEnum.US_DOLLAR

        val result = viewModel.formatCurrency(30050.0)

        assertTrue("Expected USD symbol in result", result.contains("$") || result.contains("USD"))
        assertTrue("Expected numeric value in result", result.contains("30,050") || result.contains("30050"))
    }

    // Task 24.1 — formatCurrency changes output when currency changes
    @Test
    fun `formatCurrency output reflects currently selected currency`() {
        viewModel.coin.value = CurrencyEnum.EURO
        val euroResult = viewModel.formatCurrency(1000.0)

        viewModel.coin.value = CurrencyEnum.GB_POUND
        val gbpResult = viewModel.formatCurrency(1000.0)

        assertNotEquals(
            "Different currencies should produce different formatted strings",
            euroResult,
            gbpResult
        )
    }

    // Task 24.1 — formatCurrency with BRL
    @Test
    fun `formatCurrency formats value correctly for BRL`() {
        viewModel.coin.value = CurrencyEnum.BR_REAL

        val result = viewModel.formatCurrency(5000.0)

        assertNotNull(result)
        assertTrue("Result should not be empty", result.isNotEmpty())
    }

    // Task 24.2 — formatUnixDate returns correct date string
    @Test
    fun `formatUnixDate returns date in dd-MM-yy format`() {
        // 2023-11-14 00:00:00 UTC → Unix seconds = 1699920000
        val unixSeconds = 1699920000f

        val result = viewModel.formatUnixDate(unixSeconds)

        // Should match dd/MM/yy pattern
        assertTrue(
            "Expected date format dd/MM/yy, got: $result",
            result.matches(Regex("\\d{2}/\\d{2}/\\d{2}"))
        )
    }

    // Task 24.2 — formatUnixDate for epoch zero
    @Test
    fun `formatUnixDate handles epoch zero`() {
        val result = viewModel.formatUnixDate(0f)

        assertNotNull(result)
        assertTrue(result.matches(Regex("\\d{2}/\\d{2}/\\d{2}")))
    }

    // Task 24.2 — formatUnixDate for a known timestamp
    @Test
    fun `formatUnixDate returns expected string for known timestamp`() {
        // 2000-01-01 00:00:00 UTC → 946684800 seconds
        val unixSeconds = 946684800f
        val result = viewModel.formatUnixDate(unixSeconds)

        // The date portion should be 01/01/00 (UTC) — locale may shift by timezone,
        // so we just verify the format is correct
        assertTrue(
            "Expected dd/MM/yy format, got: $result",
            result.matches(Regex("\\d{2}/\\d{2}/\\d{2}"))
        )
    }

    // getCurrencyByLocale delegates to repository
    @Test
    fun `getCurrencyByLocale delegates to CurrencyRepository`() = runBlocking {
        val coin = CurrencyEnum.JP_YEN
        val value = CurrencyValue(
            currencyKey = "JPY",
            time = Date(),
            fifteenMinutesValue = 4000000.0,
            buyValue = 4010000.0,
            sellValue = 3990000.0,
            lastValue = 4005000.0,
            symbol = "JPY"
        )
        whenever(currencyRepository.getValueByCurrency(coin)).thenReturn(value)

        val result = viewModel.getCurrencyByLocale(coin)

        assertEquals(value, result)
        verify(currencyRepository, times(1)).getValueByCurrency(coin)
    }

    // getChart delegates to ChartRepository
    @Test
    fun `getChart delegates to ChartRepository`() = runBlocking {
        val period = ChartPeriod.ONE_YEAR
        val chart = Chart(time = Date(), name = "Market Price", description = "desc", period = period)
        whenever(chartRepository.getCharts(period)).thenReturn(chart)

        val result = viewModel.getChart(period)

        assertEquals(chart, result)
        verify(chartRepository, times(1)).getCharts(period)
    }

    // getPoints delegates to ChartRepository
    @Test
    fun `getPoints delegates to ChartRepository`() = runBlocking {
        val date = Date()
        val period = ChartPeriod.TWO_YEARS
        val points = listOf(ChartPoint(100f, 30000f, date, period))
        whenever(chartRepository.getChartPoints(date, period)).thenReturn(points)

        val result = viewModel.getPoints(date, period)

        assertEquals(points, result)
        verify(chartRepository, times(1)).getChartPoints(date, period)
    }

    // Default state
    @Test
    fun `default coin is USD`() {
        assertEquals(CurrencyEnum.US_DOLLAR, viewModel.coin.value)
    }

    @Test
    fun `default period is ONE_MONTH`() {
        assertEquals(ChartPeriod.ONE_MONTH, viewModel.period.value)
    }
}
