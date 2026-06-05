package org.kimplify.kurrency.deci

import org.kimplify.deci.Deci
import org.kimplify.kurrency.CurrencyFormatOptions
import org.kimplify.kurrency.CurrencyFormatter
import org.kimplify.kurrency.KurrencyLocale
import kotlin.test.Test
import kotlin.test.assertEquals

class DeciExactFormattingTest {

    @Test
    fun deci_beyondDoublePrecision_isExact() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val r = formatter.formatWithOptions(Deci("9007199254740993.01"), "USD", CurrencyFormatOptions()).getOrThrow()
        assertEquals("$9,007,199,254,740,993.01", r)
    }
}
