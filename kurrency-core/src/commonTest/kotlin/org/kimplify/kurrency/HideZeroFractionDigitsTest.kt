package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HideZeroFractionDigitsTest {

    private val us = CurrencyFormatter(KurrencyLocale.US)
    private val de = CurrencyFormatter(KurrencyLocale.GERMANY)
    private val hide = CurrencyFormatOptions { hideZeroFractionDigits = true }
    private val show = CurrencyFormatOptions() // default: hideZeroFractionDigits = false

    @Test
    fun us_allZeroFraction_isDropped() {
        assertEquals("$34", us.formatWithOptions("34.00", "USD", hide).getOrThrow())
    }

    @Test
    fun us_nonZeroFraction_isKept() {
        assertEquals("$34.20", us.formatWithOptions("34.20", "USD", hide).getOrThrow())
        assertEquals("$34.88", us.formatWithOptions("34.88", "USD", hide).getOrThrow())
    }

    @Test
    fun us_partialTrailingZero_isKept() {
        assertEquals("$34.50", us.formatWithOptions("34.50", "USD", hide).getOrThrow())
    }

    @Test
    fun us_grouping_isPreservedWhenFractionDropped() {
        assertEquals("$1,000", us.formatWithOptions("1000.00", "USD", hide).getOrThrow())
    }

    @Test
    fun us_roundsDownToZero_isDropped() {
        assertEquals("$34", us.formatWithOptions("34.004", "USD", hide).getOrThrow())
    }

    @Test
    fun us_roundsToNonZeroFraction_isKept() {
        assertEquals("$34.02", us.formatWithOptions("34.019", "USD", hide).getOrThrow())
    }

    @Test
    fun us_nonZeroAmountRoundingToZero_isDropped() {
        assertEquals("$0", us.formatWithOptions("0.004", "USD", hide).getOrThrow())
    }

    @Test
    fun us_zeroAmount_isDropped() {
        assertEquals("$0", us.formatWithOptions("0.00", "USD", hide).getOrThrow())
    }

    @Test
    fun hideWinsOverMinFractionDigits() {
        val opts = CurrencyFormatOptions {
            hideZeroFractionDigits = true
            minFractionDigits = 2
        }
        assertEquals("$34", us.formatWithOptions("34.00", "USD", opts).getOrThrow())
    }

    @Test
    fun disabledByDefault_keepsDecimals() {
        assertEquals("$34.00", us.formatWithOptions("34.00", "USD", show).getOrThrow())
    }

    @Test
    fun negative_allZeroFraction_isDropped() {
        assertEquals("-$34", us.formatWithOptions("-34.00", "USD", hide).getOrThrow())
    }

    @Test
    fun negative_nonZeroFraction_hideHasNoEffect() {
        assertEquals(
            us.formatWithOptions("-34.20", "USD", show).getOrThrow(),
            us.formatWithOptions("-34.20", "USD", hide).getOrThrow(),
        )
    }

    @Test
    fun bhd_allZeroFraction_isDropped() {
        val r = us.formatWithOptions("10.000", "BHD", hide).getOrThrow()
        assertFalse(r.contains("."), "expected no decimals in: $r")
        assertTrue(r.contains("10"), "expected amount in: $r")
    }

    @Test
    fun bhd_nonZeroFraction_isKept() {
        assertEquals(
            us.formatWithOptions("10.500", "BHD", show).getOrThrow(),
            us.formatWithOptions("10.500", "BHD", hide).getOrThrow(),
        )
    }

    @Test
    fun germanLocale_allZeroFraction_dropsCommaSeparator() {
        val r = de.formatWithOptions("34.00", "EUR", hide).getOrThrow()
        assertFalse(r.contains(","), "German decimal separator should be gone in: $r")
        assertTrue(r.contains("34"), "expected amount in: $r")
    }

    @Test
    fun germanLocale_nonZeroFraction_isKept() {
        assertEquals(
            de.formatWithOptions("34.20", "EUR", show).getOrThrow(),
            de.formatWithOptions("34.20", "EUR", hide).getOrThrow(),
        )
    }

    @Test
    fun jpy_zeroDigitsCurrency_isNoOp() {
        assertEquals(
            us.formatWithOptions("34", "JPY", show).getOrThrow(),
            us.formatWithOptions("34", "JPY", hide).getOrThrow(),
        )
    }

    @Test
    fun kurrencyFormatAmountWithOptions_propagatesOption() {
        assertEquals(
            "$34",
            Kurrency.USD.formatAmountWithOptions("34.00", hide, KurrencyLocale.US).getOrThrow(),
        )
    }
}
