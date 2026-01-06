package org.kimplify.kurrency.deci

import org.kimplify.deci.Deci
import org.kimplify.kurrency.CurrencyFormatter
import org.kimplify.kurrency.Kurrency
import org.kimplify.kurrency.KurrencyLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeciExtensionsTest {

    @Test
    fun formatCurrencyStyle_withDeciAndCurrencyCode_formatsCorrectly() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val amount = Deci("1234.56")

        val result = formatter.formatCurrencyStyle(amount, "USD")

        assertEquals("$1,234.56", result)
    }

    @Test
    fun formatCurrencyStyle_withDeciAndKurrency_formatsCorrectly() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val amount = Deci("999.99")
        val currency = Kurrency.fromCode("EUR").getOrThrow()

        val result = formatter.formatCurrencyStyle(amount, currency)

        assertTrue(result.contains("999.99"))
    }

    @Test
    fun formatCurrencyStyle_withZeroDeci_formatsCorrectly() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val amount = Deci("0.00")

        val result = formatter.formatCurrencyStyle(amount, "USD")

        assertEquals("$0.00", result)
    }

    @Test
    fun formatCurrencyStyle_withNegativeDeci_formatsCorrectly() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val amount = Deci("-500.25")

        val result = formatter.formatCurrencyStyle(amount, "USD")

        assertTrue(result.contains("500.25"))
        assertTrue(result.contains("-") || result.contains("("))
    }

    @Test
    fun formatCurrencyStyle_withLargeDeci_formatsCorrectly() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val amount = Deci("1000000.50")

        val result = formatter.formatCurrencyStyle(amount, "USD")

        assertEquals("$1,000,000.50", result)
    }

    @Test
    fun formatCurrencyStyle_withSmallDeci_formatsCorrectly() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val amount = Deci("0.01")

        val result = formatter.formatCurrencyStyle(amount, "USD")

        assertEquals("$0.01", result)
    }

    @Test
    fun formatCurrencyStyle_withHighPrecisionDeci_roundsToFractionDigits() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val amount = Deci("99.999")

        val result = formatter.formatCurrencyStyle(amount, "USD")

        assertTrue(result.contains("100.00") || result.contains("99.99"))
    }

    @Test
    fun formatCurrencyStyle_withDeciInGermanLocale_formatsWithGermanConventions() {
        val formatter = CurrencyFormatter(KurrencyLocale.GERMANY)
        val amount = Deci("1234.56")

        val result = formatter.formatCurrencyStyle(amount, "EUR")

        assertTrue(result.contains("1.234,56") || result.contains("1 234,56"))
    }

    @Test
    fun formatIsoCurrencyStyle_withDeciAndCurrencyCode_formatsWithISOCode() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val amount = Deci("500.00")

        val result = formatter.formatIsoCurrencyStyle(amount, "USD")

        assertTrue(result.contains("USD"))
        assertTrue(result.contains("500.00"))
    }

    @Test
    fun formatIsoCurrencyStyle_withDeciAndKurrency_formatsWithISOCode() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val amount = Deci("250.75")
        val currency = Kurrency.fromCode("GBP").getOrThrow()

        val result = formatter.formatIsoCurrencyStyle(amount, currency)

        assertTrue(result.contains("GBP") || result.contains("250.75"))
    }

    @Test
    fun formatIsoCurrencyStyle_withZeroDeci_formatsCorrectly() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val amount = Deci("0.00")

        val result = formatter.formatIsoCurrencyStyle(amount, "EUR")

        assertTrue(result.contains("EUR"))
        assertTrue(result.contains("0.00"))
    }

    @Test
    fun formatIsoCurrencyStyle_withLargeDeci_formatsCorrectly() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val amount = Deci("9999999.99")

        val result = formatter.formatIsoCurrencyStyle(amount, "USD")

        assertTrue(result.contains("USD"))
        assertTrue(result.contains("9,999,999.99"))
    }

    @Test
    fun formatCurrencyStyle_withJPYAndDeci_formatsWithoutDecimals() {
        val formatter = CurrencyFormatter(KurrencyLocale.JAPAN)
        val amount = Deci("1234")

        val result = formatter.formatCurrencyStyle(amount, "JPY")

        assertTrue(result.contains("1,234") || result.contains("¥"))
    }

    @Test
    fun formatCurrencyStyle_withDeciStringRepresentation_matchesStringFormatting() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val deciAmount = Deci("777.88")
        val stringAmount = "777.88"

        val deciResult = formatter.formatCurrencyStyle(deciAmount, "USD")
        val stringResult = formatter.formatCurrencyStyle(stringAmount, "USD")

        assertEquals(stringResult, deciResult)
    }

    @Test
    fun formatIsoCurrencyStyle_withDeciStringRepresentation_matchesStringFormatting() {
        val formatter = CurrencyFormatter(KurrencyLocale.UK)
        val deciAmount = Deci("456.78")
        val stringAmount = "456.78"

        val deciResult = formatter.formatIsoCurrencyStyle(deciAmount, "GBP")
        val stringResult = formatter.formatIsoCurrencyStyle(stringAmount, "GBP")

        assertEquals(stringResult, deciResult)
    }

    @Test
    fun formatCurrencyStyle_withMultipleDecisSequentially_formatsAllCorrectly() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val amounts = listOf(
            Deci("100.00"),
            Deci("200.50"),
            Deci("300.99")
        )

        val results = amounts.map { formatter.formatCurrencyStyle(it, "USD") }

        assertEquals("$100.00", results[0])
        assertEquals("$200.50", results[1])
        assertEquals("$300.99", results[2])
    }

    @Test
    fun formatCurrencyStyle_withDeciAndMultipleCurrencies_formatsEachCorrectly() {
        val usFormatter = CurrencyFormatter(KurrencyLocale.US)
        val amount = Deci("100.00")

        val usdResult = usFormatter.formatCurrencyStyle(amount, "USD")
        val eurResult = usFormatter.formatCurrencyStyle(amount, "EUR")
        val gbpResult = usFormatter.formatCurrencyStyle(amount, "GBP")

        assertTrue(usdResult.contains("$") && usdResult.contains("100.00"))
        assertTrue(eurResult.contains("100.00"))
        assertTrue(gbpResult.contains("100.00"))
    }

    @Test
    fun formatCurrencyStyle_withVerySmallDeci_formatsCorrectly() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val amount = Deci("0.0001")

        val result = formatter.formatCurrencyStyle(amount, "USD")

        assertTrue(result.contains("0.00"))
    }
}
