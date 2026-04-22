package com.osias.blockchain.repository

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
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Call
import retrofit2.Response
import java.util.*

class CurrencyRepositoryTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private lateinit var service: Service
    private lateinit var dao: CurrencyDao
    private lateinit var dateProvider: DateProvider
    private lateinit var repository: CurrencyRepository

    private val fixedDate = Date(1_700_000_000_000L) // fixed timestamp for tests

    private fun makeCurrencyValue(symbol: String) = CurrencyValue(
        currencyKey = symbol,
        time = fixedDate,
        fifteenMinutesValue = 30000.0,
        buyValue = 30100.0,
        sellValue = 29900.0,
        lastValue = 30050.0,
        symbol = symbol
    )

    @Before
    fun setup() {
        service = mock()
        dao = mock()
        dateProvider = mock { on { getDate() } doReturn fixedDate }
        repository = CurrencyRepository(service, dao, dateProvider)
    }

    // Task 22.2 — no cached data → triggers network request
    @Test
    fun `getValueByCurrency fetches from network when no cached data exists`() {
        runBlocking {
            val currency = CurrencyEnum.US_DOLLAR
            val currencyValue = makeCurrencyValue(currency.symbol)
            val responseMap = mapOf(currency.symbol to currencyValue)

            val call: Call<Map<String, CurrencyValue>> = mock()
            whenever(call.execute()).thenReturn(Response.success(responseMap))
            whenever(service.actualCurrency()).thenReturn(call)

            // First DB check returns null (no cache)
            whenever(dao.getCurrencyDateAndSymbol(fixedDate, currency.symbol)).thenReturn(null, currencyValue)
            // Hour-stripped check also returns null
            whenever(dao.hasCurrencyDate(DateUtil.stripMinutes(fixedDate))).thenReturn(null)

            repository.getValueByCurrency(currency)

            verify(service, times(1)).actualCurrency()
        }
    }

    // Task 22.1 — cached data exists → does NOT trigger network request
    @Test
    fun `getValueByCurrency does not fetch from network when cached data exists`() {
        runBlocking {
            val currency = CurrencyEnum.US_DOLLAR
            val currencyValue = makeCurrencyValue(currency.symbol)

            // DB already has data for this date+symbol
            whenever(dao.getCurrencyDateAndSymbol(fixedDate, currency.symbol)).thenReturn(currencyValue)

            repository.getValueByCurrency(currency)

            verify(service, never()).actualCurrency()
        }
    }

    // Task 22.1 — second call within same hour reuses cache
    @Test
    fun `getValueByCurrency second call within same hour skips network request`() {
        runBlocking {
            val currency = CurrencyEnum.EURO
            val currencyValue = makeCurrencyValue(currency.symbol)
            val responseMap = mapOf(currency.symbol to currencyValue)

            val call: Call<Map<String, CurrencyValue>> = mock()
            whenever(call.execute()).thenReturn(Response.success(responseMap))
            whenever(service.actualCurrency()).thenReturn(call)

            // First call: no cache
            whenever(dao.getCurrencyDateAndSymbol(fixedDate, currency.symbol))
                .thenReturn(null)
                .thenReturn(currencyValue)
            whenever(dao.hasCurrencyDate(DateUtil.stripMinutes(fixedDate))).thenReturn(null)

            repository.getValueByCurrency(currency)

            // Second call: cache now exists (same hour)
            whenever(dao.getCurrencyDateAndSymbol(fixedDate, currency.symbol)).thenReturn(currencyValue)

            repository.getValueByCurrency(currency)

            // Network should only have been called once
            verify(service, times(1)).actualCurrency()
        }
    }

    // Task 22.2 — network error is propagated via delegate
    @Test
    fun `getValueByCurrency propagates error when network call fails`() {
        runBlocking {
            val currency = CurrencyEnum.US_DOLLAR
            val errorBody: ResponseBody = mock()
            val call: Call<Map<String, CurrencyValue>> = mock()
            val errorResponse: Response<Map<String, CurrencyValue>> = Response.error(500, errorBody)

            whenever(call.execute()).thenReturn(errorResponse)
            whenever(service.actualCurrency()).thenReturn(call)
            whenever(dao.getCurrencyDateAndSymbol(fixedDate, currency.symbol)).thenReturn(null)
            whenever(dao.hasCurrencyDate(DateUtil.stripMinutes(fixedDate))).thenReturn(null)

            val delegate: com.osias.blockchain.model.repository.RepositoryErrorDelegate = mock()
            repository.delegate = delegate

            repository.getValueByCurrency(currency)

            verify(delegate, times(1)).onError(any())
        }
    }
}
