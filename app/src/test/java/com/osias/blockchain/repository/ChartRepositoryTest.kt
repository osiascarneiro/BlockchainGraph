package com.osias.blockchain.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.mockito.kotlin.*
import com.osias.blockchain.model.entity.Chart
import com.osias.blockchain.model.entity.ChartPoint
import com.osias.blockchain.model.enumeration.ChartPeriod
import com.osias.blockchain.model.local.dao.ChartDao
import com.osias.blockchain.model.local.dao.ChartPointDao
import com.osias.blockchain.model.remote.Service
import com.osias.blockchain.model.repository.ChartRepository
import com.osias.blockchain.model.repository.DateProvider
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Call
import retrofit2.Response
import java.util.*

class ChartRepositoryTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private lateinit var service: Service
    private lateinit var chartDao: ChartDao
    private lateinit var chartPointDao: ChartPointDao
    private lateinit var dateProvider: DateProvider
    private lateinit var repository: ChartRepository

    private val fixedDate = Date(1_700_000_000_000L)

    private fun makeChart(period: ChartPeriod) = Chart(
        time = fixedDate,
        name = "Market Price",
        description = "BTC market price",
        period = period
    )

    private fun makeChartPoint(chartDate: Date, period: ChartPeriod) = ChartPoint(
        pointX = 1_700_000f,
        pointY = 30000f,
        chartTime = chartDate,
        chartPeriod = period
    )

    @Before
    fun setup() {
        service = mock()
        chartDao = mock()
        chartPointDao = mock()
        dateProvider = mock { on { getDate() } doReturn fixedDate }
        repository = ChartRepository(service, chartDao, chartPointDao, dateProvider)
    }

    // Task 23.1 — cached chart exists → does NOT trigger network request
    @Test
    fun `getCharts does not fetch from network when chart is already cached for today and period`() = runBlocking {
        val period = ChartPeriod.ONE_MONTH
        val cachedChart = makeChart(period)

        whenever(chartDao.hasChartByTimeAndPeriod(fixedDate, period)).thenReturn(cachedChart)
        whenever(chartDao.getChartByTimeAndPeriod(fixedDate, period)).thenReturn(cachedChart)

        repository.getCharts(period)

        verify(service, never()).getCurrencyChart(any())
    }

    // Task 23.1 — second call same day same period reuses cache
    @Test
    fun `getCharts second call on same day for same period skips network request`() = runBlocking {
        val period = ChartPeriod.SIX_MONTHS
        val chart = makeChart(period)
        val point = makeChartPoint(fixedDate, period)

        val call: Call<Chart> = mock()
        val chartWithValues = makeChart(period).also { it.values = listOf(point) }
        whenever(call.execute()).thenReturn(Response.success(chartWithValues))
        whenever(service.getCurrencyChart(period)).thenReturn(call)

        // First call: no cache
        whenever(chartDao.hasChartByTimeAndPeriod(fixedDate, period)).thenReturn(null)
        whenever(chartDao.getChartByTimeAndPeriod(fixedDate, period)).thenReturn(chart)

        repository.getCharts(period)

        // Second call: cache now exists
        whenever(chartDao.hasChartByTimeAndPeriod(fixedDate, period)).thenReturn(chart)

        repository.getCharts(period)

        verify(service, times(1)).getCurrencyChart(period)
    }

    // Task 23.2 — chart points are persisted with correct foreign key references
    @Test
    fun `getCharts persists chart points with correct chart time and period references`() = runBlocking {
        val period = ChartPeriod.ONE_YEAR
        val point1 = makeChartPoint(fixedDate, period)
        val point2 = ChartPoint(1_700_100f, 31000f, fixedDate, period)
        val chartWithValues = makeChart(period).also { it.values = listOf(point1, point2) }

        val call: Call<Chart> = mock()
        whenever(call.execute()).thenReturn(Response.success(chartWithValues))
        whenever(service.getCurrencyChart(period)).thenReturn(call)
        whenever(chartDao.hasChartByTimeAndPeriod(fixedDate, period)).thenReturn(null)
        whenever(chartDao.getChartByTimeAndPeriod(fixedDate, period)).thenReturn(chartWithValues)

        repository.getCharts(period)

        // Verify each point was inserted with the correct chart reference
        val pointCaptor = argumentCaptor<List<ChartPoint>>()
        verify(chartPointDao, times(2)).insert(pointCaptor.capture())

        pointCaptor.allValues.flatten().forEach { insertedPoint ->
            assertEquals(fixedDate, insertedPoint.chartTime)
            assertEquals(period, insertedPoint.chartPeriod)
        }
    }

    // Task 23.2 — no cached chart → triggers network request and persists chart
    @Test
    fun `getCharts fetches from network and persists chart when no cache exists`() = runBlocking {
        val period = ChartPeriod.TWO_MONTHS
        val chart = makeChart(period).also { it.values = emptyList() }

        val call: Call<Chart> = mock()
        whenever(call.execute()).thenReturn(Response.success(chart))
        whenever(service.getCurrencyChart(period)).thenReturn(call)
        whenever(chartDao.hasChartByTimeAndPeriod(fixedDate, period)).thenReturn(null)
        whenever(chartDao.getChartByTimeAndPeriod(fixedDate, period)).thenReturn(chart)

        repository.getCharts(period)

        verify(service, times(1)).getCurrencyChart(period)
        verify(chartDao, times(1)).insert(any<List<Chart>>())
    }

    // Network error is propagated via delegate
    @Test
    fun `getCharts propagates error when network call fails`() = runBlocking {
        val period = ChartPeriod.ALL_TIME
        val errorBody: ResponseBody = mock()
        val call: Call<Chart> = mock()
        whenever(call.execute()).thenReturn(Response.error(500, errorBody))
        whenever(service.getCurrencyChart(period)).thenReturn(call)
        whenever(chartDao.hasChartByTimeAndPeriod(fixedDate, period)).thenReturn(null)

        val delegate: com.osias.blockchain.model.repository.RepositoryErrorDelegate = mock()
        repository.delegate = delegate

        // getChartByTimeAndPeriod won't be called on error, but we need to handle the crash
        // The repository returns early after error, so we stub a fallback
        whenever(chartDao.getChartByTimeAndPeriod(fixedDate, period))
            .thenReturn(makeChart(period))

        try {
            repository.getCharts(period)
        } catch (e: Exception) {
            // expected if DB has no fallback row
        }

        verify(delegate, times(1)).onError(any())
    }

    // getChartPoints returns points from DAO
    @Test
    fun `getChartPoints returns all points for given chart reference`() = runBlocking {
        val period = ChartPeriod.TWO_YEARS
        val points = listOf(
            makeChartPoint(fixedDate, period),
            ChartPoint(1_700_200f, 32000f, fixedDate, period)
        )
        whenever(chartPointDao.getAllFromChart(fixedDate, period)).thenReturn(points)

        val result = repository.getChartPoints(fixedDate, period)

        assertEquals(2, result.size)
        verify(chartPointDao, times(1)).getAllFromChart(fixedDate, period)
    }
}
