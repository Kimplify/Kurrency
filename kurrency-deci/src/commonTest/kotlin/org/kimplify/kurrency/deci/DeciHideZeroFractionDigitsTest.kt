package org.kimplify.kurrency.deci

import org.kimplify.deci.Deci
import org.kimplify.kurrency.CurrencyFormatOptions
import org.kimplify.kurrency.CurrencyFormatter
import org.kimplify.kurrency.Kurrency
import org.kimplify.kurrency.KurrencyLocale
import kotlin.test.Test
import kotlin.test.assertEquals

class DeciHideZeroFractionDigitsTest {

    private val formatter = CurrencyFormatter(KurrencyLocale.US)
    private val hide = CurrencyFormatOptions { hideZeroFractionDigits = true }

    @Test
    fun deci_allZeroFraction_isDropped_withCurrencyCode() {
        assertEquals("$34", formatter.formatWithOptions(Deci("34.00"), "USD", hide).getOrThrow())
    }

    @Test
    fun deci_nonZeroFraction_isKept_withCurrencyCode() {
        assertEquals("$34.20", formatter.formatWithOptions(Deci("34.20"), "USD", hide).getOrThrow())
    }

    @Test
    fun deci_allZeroFraction_isDropped_withKurrency() {
        val usd = Kurrency.fromCode("USD").getOrThrow()
        assertEquals("$34", formatter.formatWithOptions(Deci("34.00"), usd, hide).getOrThrow())
    }

    @Test
    fun deci_nonZeroFraction_isKept_withKurrency() {
        val usd = Kurrency.fromCode("USD").getOrThrow()
        assertEquals("$34.20", formatter.formatWithOptions(Deci("34.20"), usd, hide).getOrThrow())
    }

    @Test
    fun deci_highPrecisionFractionRoundsToZero_isDropped() {
        assertEquals("$34", formatter.formatWithOptions(Deci("34.0000001"), "USD", hide).getOrThrow())
    }
}
