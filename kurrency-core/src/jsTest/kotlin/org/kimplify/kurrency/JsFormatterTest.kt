package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * JS platform-specific formatter tests.
 *
 * Verifies that [CurrencyFormatterImpl] (backed by Intl.NumberFormat)
 * produces correct locale-sensitive formatting in the JS/Node environment.
 */
class JsFormatterTest {

    // ---- Formatting consistency across styles ----

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

    @Test
    fun formatCompactStyle_million_abbreviates() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCompactStyleResult("1500000", "USD")
        assertTrue(result.isSuccess)
        val formatted = result.getOrThrow()
        assertNotNull(formatted)
        assertTrue(formatted.isNotBlank(), "Compact format should not be blank")
        // Compact notation should be shorter than fully spelled-out number
        assertTrue(formatted.length < 20, "Compact should be short: $formatted")
    }

    // ---- Intl.NumberFormat behavior verification ----

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

    // ---- Multiple locales produce distinct output ----

    @Test
    fun formatCurrencyStyle_differentLocales_produceDifferentOutput() {
        val usFormatter = CurrencyFormatter(KurrencyLocale.US)
        val deFormatter = CurrencyFormatter(KurrencyLocale.GERMANY)

        val usResult = usFormatter.formatCurrencyStyleResult("1234.56", "EUR").getOrThrow()
        val deResult = deFormatter.formatCurrencyStyleResult("1234.56", "EUR").getOrThrow()

        // Both should contain the digits, but formatting should differ
        assertTrue(usResult.contains("1") && usResult.contains("234"), "US: $usResult")
        assertTrue(deResult.contains("1") && deResult.contains("234"), "DE: $deResult")
        // German uses comma as decimal separator, US uses period
        assertTrue(usResult != deResult, "US and DE formatting should differ: US=$usResult, DE=$deResult")
    }

    // ---- Edge cases ----

    @Test
    fun formatCurrencyStyle_zeroAmount_succeeds() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyleResult("0", "USD")
        assertTrue(result.isSuccess)
        val formatted = result.getOrThrow()
        assertTrue(formatted.contains("0"), "Expected zero in: $formatted")
    }

    @Test
    fun formatCurrencyStyle_negativeAmount_containsMinusIndicator() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyleResult("-750.00", "USD")
        assertTrue(result.isSuccess)
        val formatted = result.getOrThrow()
        assertTrue(
            formatted.contains("-") || formatted.contains("\u2212"),
            "Expected negative indicator in: $formatted"
        )
    }

    @Test
    fun formatCurrencyStyle_veryLargeAmount_succeeds() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyleResult("999999999999.99", "USD")
        assertTrue(result.isSuccess)
        val formatted = result.getOrThrow()
        assertTrue(formatted.contains("999"), "Expected digits in: $formatted")
    }
}
