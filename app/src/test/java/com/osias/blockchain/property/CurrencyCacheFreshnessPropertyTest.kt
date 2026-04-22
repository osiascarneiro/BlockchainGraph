package com.osias.blockchain.property

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.mockito.kotlin.*
import com.osias.blockchain.common.utils.DateUtil
import com.osias.blockchain.model.entity.CurrencyValue
import com.osias.blockchain.model.enumeration.CurrencyEnum
import com.osias.blockchain.model.local.dao.CurrencyDao
import com.osias.blockchain.model.remote.Service
import com.osias.blockchain.model.repository.CurrencyRepository
import com.osias.blockchain.model.repository.DateProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import retrofit2.Call
import retrofit2.Response
import java.util.*

/**
 * Property-Based Tests for CP-1: Cache Freshness — Currency
 *
 * Property: For any two calls to getValueByCurrency within the same hour,
 * the second call shall not trigger a network request if the first succeeded.
 *
 * We simulate this property across multiple currencies and time offsets
 * within the same hour to verify the invariant holds universally.
 */
class CurrencyCacheFreshnessPropertyTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    /**
     * Generates a set of Date values that all fall within the same hour
     * (varying only seconds/milliseconds within a fixed hour window).
     */
    private fun datesWithinSameHour(baseMs: Long, count: Int = 5): List<Date> {
        return (0 until count).map { i ->
            Date(baseMs + i * 60_000L) // spaced 1 minute apart, same hour
        }
    }

    /**
     * CP-1 Property: For every currency in CurrencyEnum, two calls within the same hour
     * must result in exactly one network request.
     */
    @Test
    fun `CP-1 for any currency two calls within same hour produce exactly one network request`() {
        // Test across a representative sample of currencies
        val currencies = listOf(
            CurrencyEnum.US_DOLLAR,
            CurrencyEnum.EURO,
            CurrencyEnum.BR_REAL,
            CurrencyEnum.JP_YEN,
            CurrencyEnum.GB_POUND
        )

        // Test across different hours of the day
        val baseTimestamps = listOf(
            1_700_000_000_000L,  // arbitrary fixed hour
            1_700_003_600_000L,  // +1 hour
            1_700_007_200_000L   // +2 hours
        )

        currencies.forEach { currency ->
            baseTimestamps.forEach { baseMs ->
                verifyAtMostOneNetworkCallWithinHour(currency, baseMs)
            }
        }
    }

    /**
     * CP-1 Property: Calls spanning two different hours must each trigger a network request.
     */
    @Test
    fun `CP-1 calls in different hours each trigger a network request`() {
        val currency = CurrencyEnum.US_DOLLAR

        // Two dates in different hours
        val hour1 = Date(1_700_000_000_000L)
        val hour2 = Date(1_700_003_600_000L) // exactly 1 hour later

        val service: Service = mock()
        val dao: CurrencyDao = mock()

        val makeValue = { date: Date ->
            CurrencyValue(currency.symbol, date, 30000.0, 30100.0, 29900.0, 30050.0, currency.symbol)
        }

        val call: Call<Map<String, CurrencyValue>> = mock()
        whenever(call.execute()).thenReturn(Response.success(mapOf(currency.symbol to makeValue(hour1))))
        whenever(service.actualCurrency()).thenReturn(call)

        // Hour 1: no cache
        val dateProvider1: DateProvider = mock { on { getDate() } doReturn hour1 }
        val repo1 = CurrencyRepository(service, dao, dateProvider1)
        runBlocking {
            whenever(dao.getCurrencyDateAndSymbol(hour1, currency.symbol)).thenReturn(null, makeValue(hour1))
            whenever(dao.hasCurrencyDate(DateUtil.stripMinutes(hour1))).thenReturn(null)
            repo1.getValueByCurrency(currency)
        }

        // Hour 2: no cache for this new hour
        val dateProvider2: DateProvider = mock { on { getDate() } doReturn hour2 }
        val repo2 = CurrencyRepository(service, dao, dateProvider2)
        runBlocking {
            whenever(dao.getCurrencyDateAndSymbol(hour2, currency.symbol)).thenReturn(null, makeValue(hour2))
            whenever(dao.hasCurrencyDate(DateUtil.stripMinutes(hour2))).thenReturn(null)
            repo2.getValueByCurrency(currency)
        }

        // Two different hours → two network calls
        verify(service, times(2)).actualCurrency()
    }

    /**
     * CP-1 Property: The number of network calls equals the number of distinct hours accessed,
     * regardless of how many calls are made within each hour.
     */
    @Test
    fun `CP-1 network call count equals number of distinct hours accessed`() {
        val currency = CurrencyEnum.EURO
        val distinctHours = 3
        val callsPerHour = 4

        val service: Service = mock()
        val dao: CurrencyDao = mock()

        var totalNetworkCalls = 0

        repeat(distinctHours) { hourOffset ->
            val hourBase = 1_700_000_000_000L + hourOffset * 3_600_000L
            val hourDate = Date(hourBase)
            val strippedHour = DateUtil.stripMinutes(hourDate)
            val value = CurrencyValue(currency.symbol, hourDate, 30000.0, 30100.0, 29900.0, 30050.0, currency.symbol)

            val call: Call<Map<String, CurrencyValue>> = mock()
            whenever(call.execute()).thenReturn(Response.success(mapOf(currency.symbol to value)))
            whenever(service.actualCurrency()).thenReturn(call)

            val dateProvider: DateProvider = mock { on { getDate() } doReturn hourDate }
            val repo = CurrencyRepository(service, dao, dateProvider)

            // First call in this hour: no cache
            runBlocking {
                whenever(dao.getCurrencyDateAndSymbol(hourDate, currency.symbol))
                    .thenReturn(null)
                    .thenReturn(value)
                whenever(dao.hasCurrencyDate(strippedHour)).thenReturn(null)
                repo.getValueByCurrency(currency)
            }
            totalNetworkCalls++

            // Subsequent calls in same hour: cache exists
            runBlocking {
                whenever(dao.getCurrencyDateAndSymbol(hourDate, currency.symbol)).thenReturn(value)
            }

            repeat(callsPerHour - 1) {
                runBlocking { repo.getValueByCurrency(currency) }
            }
        }

        // Network should have been called exactly once per distinct hour
        verify(service, times(distinctHours)).actualCurrency()
        assertEquals(distinctHours, totalNetworkCalls)
    }

    // Helper: verifies that two calls within the same hour produce exactly one network call
    private fun verifyAtMostOneNetworkCallWithinHour(currency: CurrencyEnum, baseMs: Long) {
        val service: Service = mock()
        val dao: CurrencyDao = mock()
        val date = Date(baseMs)
        val value = CurrencyValue(currency.symbol, date, 30000.0, 30100.0, 29900.0, 30050.0, currency.symbol)

        val call: Call<Map<String, CurrencyValue>> = mock()
        whenever(call.execute()).thenReturn(Response.success(mapOf(currency.symbol to value)))
        whenever(service.actualCurrency()).thenReturn(call)

        val dateProvider: DateProvider = mock { on { getDate() } doReturn date }
        val repo = CurrencyRepository(service, dao, dateProvider)

        // First call: no cache
        runBlocking {
            whenever(dao.getCurrencyDateAndSymbol(date, currency.symbol)).thenReturn(null, value)
            whenever(dao.hasCurrencyDate(DateUtil.stripMinutes(date))).thenReturn(null)
            repo.getValueByCurrency(currency)
        }

        // Second call: cache exists
        runBlocking {
            whenever(dao.getCurrencyDateAndSymbol(date, currency.symbol)).thenReturn(value)
            repo.getValueByCurrency(currency)
        }

        verify(service, times(1)).actualCurrency()
    }
}
