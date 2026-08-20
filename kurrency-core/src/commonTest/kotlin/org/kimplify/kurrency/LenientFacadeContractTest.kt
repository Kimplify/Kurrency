package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Platform failures now propagate out of the implementations, so the lenient contract the
 * non-`Result` API documents — "returns the original amount on error" — has exactly one home left:
 * this facade. These tests pin it there so the leniency cannot quietly disappear with the swallow.
 */
class LenientFacadeContractTest {

    private val formatter = CurrencyFormatter(KurrencyLocale.US)

    @Test
    fun anUnusableCurrencyCodeGivesTheAmountBack() {
        assertEquals("1234.56", formatter.formatCurrencyStyle("1234.56", "XYZ"))
        assertEquals("1234.56", formatter.formatIsoCurrencyStyle("1234.56", "XYZ"))
        assertEquals("1234.56", formatter.formatCompactStyle("1234.56", "XYZ"))
    }

    @Test
    fun theResultApiReportsWhatTheLenientApiHides() {
        val result = formatter.formatCurrencyStyleResult("1234.56", "XYZ")
        assertTrue(result.isFailure)
        assertIs<KurrencyError>(result.exceptionOrNull())
    }

    @Test
    fun aSupportedCurrencyIsUnaffected() {
        assertEquals("$1,234.56", formatter.formatCurrencyStyle("1234.56", "USD"))
    }
}
