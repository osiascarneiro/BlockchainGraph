package com.osias.blockchain.property

import com.osias.blockchain.R
import com.osias.blockchain.model.enumeration.ChartPeriod
import org.junit.Assert.*
import org.junit.Test

/**
 * Property-Based Tests for CP-5: Period Mapping Completeness
 *
 * Property: Every ChartPeriod enum value shall map to exactly one radio button ID,
 * and every radio button ID shall map to exactly one ChartPeriod value.
 *
 * This mirrors the mapping logic in CurrencyFragment.periodSegmentedButton listener.
 */
class PeriodMappingCompletenessPropertyTest {

    /**
     * The canonical mapping from radio button IDs to ChartPeriod values,
     * mirroring CurrencyFragment's setOnCheckedChangeListener.
     */
    private val buttonIdToPeriod: Map<Int, ChartPeriod> = mapOf(
        R.id.thirty_days              to ChartPeriod.ONE_MONTH,
        R.id.sixty_days               to ChartPeriod.TWO_MONTHS,
        R.id.one_hundred_eighty_days  to ChartPeriod.SIX_MONTHS,
        R.id.one_year                 to ChartPeriod.ONE_YEAR,
        R.id.two_years                to ChartPeriod.TWO_YEARS,
        R.id.all_time                 to ChartPeriod.ALL_TIME
    )

    /**
     * CP-5 Property: Every ChartPeriod value has exactly one corresponding button ID.
     */
    @Test
    fun `CP-5 every ChartPeriod value maps to exactly one button ID`() {
        val allPeriods = ChartPeriod.values().toSet()
        val mappedPeriods = buttonIdToPeriod.values.toSet()

        assertEquals(
            "All ChartPeriod values must be covered by a button mapping. Missing: ${allPeriods - mappedPeriods}",
            allPeriods,
            mappedPeriods
        )
    }

    /**
     * CP-5 Property: Every button ID maps to exactly one ChartPeriod (no duplicates).
     */
    @Test
    fun `CP-5 every button ID maps to a unique ChartPeriod with no duplicates`() {
        val periods = buttonIdToPeriod.values.toList()
        val uniquePeriods = periods.toSet()

        assertEquals(
            "Each button must map to a distinct ChartPeriod. Duplicates found.",
            uniquePeriods.size,
            periods.size
        )
    }

    /**
     * CP-5 Property: The number of button mappings equals the number of ChartPeriod values.
     */
    @Test
    fun `CP-5 button count equals ChartPeriod enum count`() {
        assertEquals(
            "Number of radio buttons must equal number of ChartPeriod values",
            ChartPeriod.values().size,
            buttonIdToPeriod.size
        )
    }

    /**
     * CP-5 Property: The mapping is bijective — every period maps back to exactly one button.
     */
    @Test
    fun `CP-5 mapping is bijective - each period maps back to exactly one button`() {
        val periodToButtonId = buttonIdToPeriod.entries.associate { (k, v) -> v to k }

        ChartPeriod.values().forEach { period ->
            assertTrue(
                "ChartPeriod.$period has no corresponding button ID",
                periodToButtonId.containsKey(period)
            )
        }

        assertEquals(
            "Inverse mapping must have same size as forward mapping",
            buttonIdToPeriod.size,
            periodToButtonId.size
        )
    }

    /**
     * CP-5 Property: Specific known mappings are correct.
     */
    @Test
    fun `CP-5 specific period-to-button mappings are correct`() {
        assertEquals(ChartPeriod.ONE_MONTH,   buttonIdToPeriod[R.id.thirty_days])
        assertEquals(ChartPeriod.TWO_MONTHS,  buttonIdToPeriod[R.id.sixty_days])
        assertEquals(ChartPeriod.SIX_MONTHS,  buttonIdToPeriod[R.id.one_hundred_eighty_days])
        assertEquals(ChartPeriod.ONE_YEAR,    buttonIdToPeriod[R.id.one_year])
        assertEquals(ChartPeriod.TWO_YEARS,   buttonIdToPeriod[R.id.two_years])
        assertEquals(ChartPeriod.ALL_TIME,    buttonIdToPeriod[R.id.all_time])
    }

    /**
     * CP-5 Property: No ChartPeriod is left unmapped (exhaustiveness check).
     */
    @Test
    fun `CP-5 no ChartPeriod value is left without a button mapping`() {
        val unmapped = ChartPeriod.values().filter { period ->
            buttonIdToPeriod.values.none { it == period }
        }

        assertTrue(
            "The following ChartPeriod values have no button mapping: $unmapped",
            unmapped.isEmpty()
        )
    }
}
