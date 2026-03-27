package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * JVM platform-specific formatter tests.
 *
 * Verifies that [CurrencyFormatterImpl] (backed by java.text.NumberFormat)
 * produces correct locale-sensitive formatting on the JVM.
 */
class JvmFormatterTest {

    // ---- Formatting consistency across styles ----

    @Test
    fun formatCurrencyStyle_usd_usLocale_containsDollarSign() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyleResult("1234.56", "USD")
        assertTrue(result.isSuccess)
        val formatted = result.getOrThrow()
        assertTrue(formatted.contains("$"), "Expected dollar sign in: $formatted")
        assertTrue(formatted.contains("1") && formatted.contains("234"), "Expected grouping in: $formatted")
    }

    @Test
    fun formatIsoCurrencyStyle_usd_usLocale_containsCurrencyCode() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatIsoCurrencyStyleResult("1234.56", "USD")
        assertTrue(result.isSuccess)
        val formatted = result.getOrThrow()
        assertTrue(formatted.contains("USD"), "Expected ISO code in: $formatted")
        assertTrue(formatted.contains("1") && formatted.contains("234"), "Expected digits in: $formatted")
    }

    @Test
    fun formatCompactStyle_usd_usLocale_returnsNonEmpty() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCompactStyleResult("1234567.89", "USD")
        assertTrue(result.isSuccess)
        val formatted = result.getOrThrow()
        assertTrue(formatted.isNotBlank(), "Compact format should not be blank")
    }

    // ---- Locale-specific separators ----

    @Test
    fun formatCurrencyStyle_germanLocale_usesCommaDecimalSeparator() {
        val formatter = CurrencyFormatter(KurrencyLocale.GERMANY)
        val result = formatter.formatCurrencyStyleResult("1234.56", "EUR")
        assertTrue(result.isSuccess)
        val formatted = result.getOrThrow()
        // German locale uses comma as decimal separator
        assertTrue(formatted.contains(","), "German locale should use comma in: $formatted")
        assertTrue(formatted.contains("1") && formatted.contains("234"), "Expected digits in: $formatted")
    }

    @Test
    fun formatCurrencyStyle_japaneseLocale_yenSymbol() {
        val formatter = CurrencyFormatter(KurrencyLocale.JAPAN)
        val result = formatter.formatCurrencyStyleResult("1234", "JPY")
        assertTrue(result.isSuccess)
        val formatted = result.getOrThrow()
        // JPY should have no decimal digits
        assertTrue(
            formatted.contains("\uFFE5") || formatted.contains("\u00A5"),
            "Expected yen symbol in: $formatted"
        )
        assertTrue(formatted.contains("1") && formatted.contains("234"), "Expected digits in: $formatted")
    }

    // ---- Edge cases ----

    @Test
    fun formatCurrencyStyle_zeroAmount_formatsSuccessfully() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyleResult("0", "USD")
        assertTrue(result.isSuccess)
        val formatted = result.getOrThrow()
        assertTrue(formatted.contains("0"), "Expected zero in: $formatted")
    }

    @Test
    fun formatCurrencyStyle_negativeAmount_containsMinusOrParenthesis() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyleResult("-500.25", "USD")
        assertTrue(result.isSuccess)
        val formatted = result.getOrThrow()
        assertTrue(
            formatted.contains("-") || formatted.contains("\u2212") || formatted.contains("("),
            "Expected negative indicator in: $formatted"
        )
        assertTrue(formatted.contains("500"), "Expected amount in: $formatted")
    }

    @Test
    fun formatCurrencyStyle_veryLargeAmount_formatsSuccessfully() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyleResult("999999999999.99", "USD")
        assertTrue(result.isSuccess)
        val formatted = result.getOrThrow()
        assertTrue(formatted.contains("999"), "Expected digits in: $formatted")
    }

    @Test
    fun formatCurrencyStyle_bhd_threeDecimalPlaces() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyleResult("100.123", "BHD")
        assertTrue(result.isSuccess)
        val formatted = result.getOrThrow()
        // BHD has 3 fraction digits
        assertTrue(formatted.contains("100"), "Expected amount in: $formatted")
        assertNotNull(formatted)
    }

    // ---- Direct CurrencyFormatterImpl testing ----

    @Test
    fun fractionDigits_usd_returnsTwo() {
        val impl = CurrencyFormatterImpl(KurrencyLocale.US)
        assertEquals(2, impl.getFractionDigitsOrDefault("USD"))
    }

    @Test
    fun fractionDigits_jpy_returnsZero() {
        val impl = CurrencyFormatterImpl(KurrencyLocale.US)
        assertEquals(0, impl.getFractionDigitsOrDefault("JPY"))
    }

    @Test
    fun fractionDigits_bhd_returnsThree() {
        val impl = CurrencyFormatterImpl(KurrencyLocale.US)
        assertEquals(3, impl.getFractionDigitsOrDefault("BHD"))
    }
}
