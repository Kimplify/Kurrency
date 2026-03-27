package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * iOS platform-specific formatter tests.
 *
 * Verifies that [CurrencyFormatterImpl] (backed by NSNumberFormatter)
 * produces correct locale-sensitive formatting on iOS/macOS.
 */
class IosFormatterTest {

    // ---- NSNumberFormatter behavior ----

    @Test
    fun formatCurrencyStyle_usd_containsDollarSign() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyleResult("1234.56", "USD")
        assertTrue(result.isSuccess)
        val formatted = result.getOrThrow()
        assertTrue(formatted.contains("$"), "Expected dollar sign in: $formatted")
        assertTrue(formatted.contains("1") && formatted.contains("234"), "Expected grouping in: $formatted")
    }

    @Test
    fun formatIsoCurrencyStyle_usd_containsCurrencyCode() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatIsoCurrencyStyleResult("1234.56", "USD")
        assertTrue(result.isSuccess)
        val formatted = result.getOrThrow()
        assertTrue(formatted.contains("USD"), "Expected ISO code in: $formatted")
    }

    // ---- Compact style with trailing symbol (EUR) ----

    @Test
    fun formatCompactStyle_largeAmount_abbreviates() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCompactStyleResult("2500000", "USD")
        assertTrue(result.isSuccess)
        val formatted = result.getOrThrow()
        assertNotNull(formatted)
        assertTrue(formatted.isNotBlank(), "Compact format should not be blank")
        // Compact should be shorter than full representation
        assertTrue(formatted.length < 25, "Compact should be short: $formatted")
    }

    @Test
    fun formatCompactStyle_eur_formatsSuccessfully() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCompactStyleResult("1500000", "EUR")
        assertTrue(result.isSuccess)
        val formatted = result.getOrThrow()
        assertNotNull(formatted)
        assertTrue(formatted.isNotBlank())
    }

    // ---- Fraction digits ----

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

    // ---- Edge cases ----

    @Test
    fun formatCurrencyStyle_negativeAmount_containsMinusIndicator() {
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
}
