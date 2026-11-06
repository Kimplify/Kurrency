package org.kimplify.kurrency.compose

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.text.AnnotatedString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.kimplify.kurrency.KurrencyLocale

class CurrencyOutputTransformationTest {

    @Test
    fun formatsMinorUnitDigits() {
        val examples = listOf(
            "" to "$0.00",
            "0" to "$0.00",
            "00" to "$0.00",
            "1" to "$0.01",
            "12" to "$0.12",
            "123" to "$1.23",
            "1234" to "$12.34",
            "123456" to "$1,234.56",
            "0001" to "$0.01",
            "0001234" to "$12.34",
        )

        for ((raw, expected) in examples) {
            assertEquals(expected, transform(raw), "Raw input: $raw")
        }
    }

    @Test
    fun filtersNonDigitsFromDisplay() {
        val examples = listOf(
            "abc" to "$0.00",
            "a1b2" to "$0.12",
            "1.23" to "$1.23",
            "12.3" to "$1.23",
            "-1234" to "$12.34",
            "1e3" to "$0.13",
            " 1,234.56 " to "$1,234.56",
        )

        for ((raw, expected) in examples) {
            assertEquals(expected, transform(raw), "Raw input: $raw")
        }
    }

    @Test
    fun customScaleChangesInputUnitsRatherThanDisplayPrecision() {
        assertEquals("$1,234.00", transform("1234", fractionDigits = 0))
        assertEquals("$123.40", transform("1234", fractionDigits = 1))
        assertEquals("$12.34", transform("1234", fractionDigits = 2))
        assertEquals("$1.23", transform("1234", fractionDigits = 3))
        assertEquals("$10.00", transform("9999", fractionDigits = 3))
    }

    @Test
    fun preservesLegacyOutputAcrossCurrenciesAndLocales() {
        val configurations = listOf(
            "USD" to KurrencyLocale.US,
            "EUR" to KurrencyLocale.GERMANY,
            "EUR" to KurrencyLocale.FRANCE,
            "JPY" to KurrencyLocale.JAPAN,
            "KWD" to KurrencyLocale.US,
            "INR" to KurrencyLocale.INDIA,
            "SAR" to KurrencyLocale.SAUDI_ARABIA,
        )
        val inputs = listOf("", "1", "1234", "123456789", "0001234", "a1b2", "١٢", "۱۲")

        for ((currencyCode, locale) in configurations) {
            for (raw in inputs) {
                val expected = CurrencyVisualTransformation(currencyCode, locale)
                    .filter(AnnotatedString(raw)).text.text
                assertEquals(expected, transform(raw, currencyCode, locale), "$currencyCode ${locale.languageTag}: $raw")
            }
        }
    }

    @Test
    fun invalidCurrencyFallsBackToSynthesizedAmount() {
        assertEquals("12.34", transform("1234", currencyCode = "INVALID"))
        assertEquals("0.00", transform("", currencyCode = "INVALID"))
        assertEquals("0.001", transform("1", currencyCode = "INVALID", fractionDigits = 3))
    }

    @Test
    fun failedAmountFormattingUsesInputScaleDespiteSuccessfulZeroFormatting() {
        assertEquals("9".repeat(397) + ".999", transform("9".repeat(400), fractionDigits = 3))
    }

    @Test
    fun unicodeDigitsRetainExistingFormatterFallback() {
        assertEquals("0.١٢", transform("١٢"))
        assertEquals("0.۱۲", transform("۱۲"))
        assertEquals("$0.12", transform("😀1😀2"))
    }

    @Test
    fun rejectsNegativeInputScale() {
        assertFailsWith<IllegalArgumentException> {
            CurrencyOutputTransformation("USD", KurrencyLocale.US, fractionDigits = -1)
        }
    }

    private fun transform(
        raw: String,
        currencyCode: String = "USD",
        locale: KurrencyLocale = KurrencyLocale.US,
        fractionDigits: Int? = null,
    ): String {
        val transformation = if (fractionDigits == null) {
            CurrencyOutputTransformation(currencyCode, locale)
        } else {
            CurrencyOutputTransformation(currencyCode, locale, fractionDigits)
        }
        val state = TextFieldState(raw)
        state.edit {
            with(transformation) { transformOutput() }
        }
        return state.text.toString()
    }
}
