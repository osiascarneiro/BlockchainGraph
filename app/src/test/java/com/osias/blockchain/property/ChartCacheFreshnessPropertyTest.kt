package com.osias.blockchain.property

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.mockito.kotlin.*
import com.osias.blockchain.model.entity.Chart
import com.osias.blockchain.model.enumeration.ChartPeriod
import com.osias.blockchain.model.local.dao.ChartDao
import com.osias.blockchain.model.local.dao.ChartPointDao
import com.osias.blockchain.model.remote.Service
import com.osias.blockchain.model.repository.ChartRepository
import com.osias.blockchain.model.repository.DateProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import retrofit2.Call
import retrofit2.Response
import java.util.*

/**
 * Property-Based Tests for CP-2: Cache Freshness — Chart
 *
 * Property: For any two calls to getCharts(period) on the same date,
 * the second call shall not trigger a network request if the first succeeded.
 *
 * We verify this property across all ChartPeriod values and multiple dates.
 */
class ChartCacheFreshnessPropertyTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private fun makeChart(date: Date, period: ChartPeriod) = Chart(
        time = date,
        name = "Market Price",
        description = "BTC market price",
        period = period
    )

    /**
     * CP-2 Property: For every ChartPeriod, two calls on the same date
     * must result in exactly one network request.
     */
    @Test
    fun `CP-2 for every ChartPeriod two calls on same date produce exactly one network request`() {
        val testDates = listOf(
            Date(1_700_000_000_000L),
            Date(1_710_000_000_000L),
            Date(1_720_000_000_000L)
        )

        ChartPeriod.values().forEach { period ->
            testDates.forEach { date ->
                verifyAtMostOneNetworkCallPerDay(period, date)
            }
        }
    }

    /**
     * CP-2 Property: Calls for the same period on different days each trigger a network request.
     */
    @Test
    fun `CP-2 same period on different days each triggers a network request`() {
        val period = ChartPeriod.ONE_MONTH
        val day1 = Date(1_700_000_000_000L)
        val day2 = Date(1_700_086_400_000L) // +1 day

        val service: Service = mock()
        val chartDao: ChartDao = mock()
        val chartPointDao: ChartPointDao = mock()

        val chart1 = makeChart(day1, period)
        val chart2 = makeChart(day2, period)

        val call: Call<Chart> = mock()
        whenever(call.execute())
            .thenReturn(Response.success(chart1.also { it.values = emptyList() }))
            .thenReturn(Response.success(chart2.also { it.values = emptyList() }))
        whenever(service.getCurrencyChart(period)).thenReturn(call)

        // Day 1
        val dp1: DateProvider = mock { on { getDate() } doReturn day1 }
        val repo1 = ChartRepository(service, chartDao, chartPointDao, dp1)
        whenever(chartDao.hasChartByTimeAndPeriod(day1, period)).thenReturn(null)
        whenever(chartDao.getChartByTimeAndPeriod(day1, period)).thenReturn(chart1)
        runBlocking { repo1.getCharts(period) }

        // Day 2
        val dp2: DateProvider = mock { on { getDate() } doReturn day2 }
        val repo2 = ChartRepository(service, chartDao, chartPointDao, dp2)
        whenever(chartDao.hasChartByTimeAndPeriod(day2, period)).thenReturn(null)
        whenever(chartDao.getChartByTimeAndPeriod(day2, period)).thenReturn(chart2)
        runBlocking { repo2.getCharts(period) }

        verify(service, times(2)).getCurrencyChart(period)
    }

    /**
     * CP-2 Property: Different periods on the same day each trigger their own network request.
     */
    @Test
    fun `CP-2 different periods on same day each trigger independent network requests`() {
        val date = Date(1_700_000_000_000L)
        val service: Service = mock()
        val chartDao: ChartDao = mock()
        val chartPointDao: ChartPointDao = mock()
        val dateProvider: DateProvider = mock { on { getDate() } doReturn date }

        ChartPeriod.values().forEach { period ->
            val chart = makeChart(date, period)
            val call: Call<Chart> = mock()
            whenever(call.execute()).thenReturn(Response.success(chart.also { it.values = emptyList() }))
            whenever(service.getCurrencyChart(period)).thenReturn(call)
            whenever(chartDao.hasChartByTimeAndPeriod(date, period)).thenReturn(null)
            whenever(chartDao.getChartByTimeAndPeriod(date, period)).thenReturn(chart)
        }

        val repo = ChartRepository(service, chartDao, chartPointDao, dateProvider)

        runBlocking {
            ChartPeriod.values().forEach { period ->
                repo.getCharts(period)
            }
        }

        // Each period should have triggered exactly one network call
        ChartPeriod.values().forEach { period ->
            verify(service, times(1)).getCurrencyChart(period)
        }
    }

    /**
     * CP-2 Property: N calls for the same period on the same day produce exactly 1 network call,
     * regardless of N.
     */
    @Test
    fun `CP-2 N calls for same period on same day always produce exactly 1 network call`() {
        val callCounts = listOf(2, 3, 5, 10)
        val period = ChartPeriod.ONE_YEAR
        val date = Date(1_700_000_000_000L)

        callCounts.forEach { n ->
            val service: Service = mock()
            val chartDao: ChartDao = mock()
            val chartPointDao: ChartPointDao = mock()
            val dateProvider: DateProvider = mock { on { getDate() } doReturn date }
            val chart = makeChart(date, period)

            val call: Call<Chart> = mock()
            whenever(call.execute()).thenReturn(Response.success(chart.also { it.values = emptyList() }))
            whenever(service.getCurrencyChart(period)).thenReturn(call)

            // First call: no cache
            whenever(chartDao.hasChartByTimeAndPeriod(date, period)).thenReturn(null)
            whenever(chartDao.getChartByTimeAndPeriod(date, period)).thenReturn(chart)

            val repo = ChartRepository(service, chartDao, chartPointDao, dateProvider)

            runBlocking { repo.getCharts(period) }

            // Subsequent calls: cache exists
            whenever(chartDao.hasChartByTimeAndPeriod(date, period)).thenReturn(chart)

            repeat(n - 1) {
                runBlocking { repo.getCharts(period) }
            }

            verify(service, times(1)).getCurrencyChart(period)
        }
    }

    // Helper: verifies exactly one network call for two calls on the same date+period
    private fun verifyAtMostOneNetworkCallPerDay(period: ChartPeriod, date: Date) {
        val service: Service = mock()
        val chartDao: ChartDao = mock()
        val chartPointDao: ChartPointDao = mock()
        val dateProvider: DateProvider = mock { on { getDate() } doReturn date }
        val chart = makeChart(date, period)

        val call: Call<Chart> = mock()
        whenever(call.execute()).thenReturn(Response.success(chart.also { it.values = emptyList() }))
        whenever(service.getCurrencyChart(period)).thenReturn(call)

        // First call: no cache
        whenever(chartDao.hasChartByTimeAndPeriod(date, period)).thenReturn(null)
        whenever(chartDao.getChartByTimeAndPeriod(date, period)).thenReturn(chart)

        val repo = ChartRepository(service, chartDao, chartPointDao, dateProvider)
        runBlocking { repo.getCharts(period) }

        // Second call: cache exists
        whenever(chartDao.hasChartByTimeAndPeriod(date, period)).thenReturn(chart)
        runBlocking { repo.getCharts(period) }

        verify(service, times(1)).getCurrencyChart(period)
    }
}
