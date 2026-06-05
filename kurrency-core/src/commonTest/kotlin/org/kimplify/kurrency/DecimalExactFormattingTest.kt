package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals

class DecimalExactFormattingTest {

    private val us = CurrencyFormatter(KurrencyLocale.US)

    @Test
    fun exactBeyondDoublePrecision() {
        val r = us.formatWithOptions("9007199254740993.01", "USD", CurrencyFormatOptions()).getOrThrow()
        assertEquals("$9,007,199,254,740,993.01", r)
    }

    @Test
    fun halfEvenDefault_correctsDoubleArtifact() {
        assertEquals("$2.68", us.formatWithOptions("2.675", "USD", CurrencyFormatOptions()).getOrThrow())
    }

    @Test
    fun roundingMode_halfUp() {
        val opts = CurrencyFormatOptions { roundingMode = RoundingMode.HALF_UP }
        assertEquals("$2.67", us.formatWithOptions("2.665", "USD", opts).getOrThrow())
    }

    @Test
    fun roundingMode_down_truncates() {
        val opts = CurrencyFormatOptions { roundingMode = RoundingMode.DOWN }
        assertEquals("$2.67", us.formatWithOptions("2.679", "USD", opts).getOrThrow())
    }

    @Test
    fun roundingMode_up_awayFromZero() {
        val opts = CurrencyFormatOptions { roundingMode = RoundingMode.UP }
        assertEquals("$2.68", us.formatWithOptions("2.671", "USD", opts).getOrThrow())
    }

    @Test
    fun leadingPlus_isHandled() {
        assertEquals("$5.00", us.formatWithOptions("+5", "USD", CurrencyFormatOptions()).getOrThrow())
    }

    @Test
    fun scientificNotation_isFormatted() {
        assertEquals("$1,000.00", us.formatWithOptions("1e3", "USD", CurrencyFormatOptions()).getOrThrow())
    }

    @Test
    fun defaultHalfEven_tieRoundsToEven() {
        assertEquals("$2.66", us.formatWithOptions("2.665", "USD", CurrencyFormatOptions()).getOrThrow())
    }

    @Test
    fun negativeAmount_isRounded_minusSign() {
        assertEquals("-$2.68", us.formatWithOptions("-2.675", "USD", CurrencyFormatOptions()).getOrThrow())
    }

    @Test
    fun negativeAmount_isRounded_parentheses() {
        val opts = CurrencyFormatOptions { negativeStyle = NegativeStyle.PARENTHESES }
        assertEquals("($2.68)", us.formatWithOptions("-2.675", "USD", opts).getOrThrow())
    }
}
