package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * WasmJS platform-specific formatter tests.
 *
 * Verifies that [CurrencyFormatterImpl] (backed by Intl.NumberFormat via @JsFun bindings)
 * produces correct formatting in the WasmJS/browser environment.
 */
class WasmJsFormatterTest {

    // ---- Basic formatting ----

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
        val result = formatter.formatIsoCurrencyStyleResult("500.00", "USD")
        assertTrue(result.isSuccess)
        val formatted = result.getOrThrow()
        assertTrue(formatted.contains("USD"), "Expected ISO code in: $formatted")
    }

    @Test
    fun formatCompactStyle_returns_abbreviated() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCompactStyleResult("2500000", "USD")
        assertTrue(result.isSuccess)
        val formatted = result.getOrThrow()
        assertNotNull(formatted)
        assertTrue(formatted.isNotBlank(), "Compact format should not be blank")
    }

    // ---- Wasm number precision (large numbers) ----

    @Test
    fun formatCurrencyStyle_veryLargeNumber_succeeds() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyleResult("999999999999.99", "USD")
        assertTrue(result.isSuccess)
        val formatted = result.getOrThrow()
        assertTrue(formatted.contains("999"), "Expected digits in: $formatted")
    }

    @Test
    fun formatCurrencyStyle_negativeAmount_indicatesNegative() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyleResult("-1234.56", "USD")
        assertTrue(result.isSuccess)
        val formatted = result.getOrThrow()
        assertTrue(
            formatted.contains("-") || formatted.contains("\u2212"),
            "Expected negative indicator in: $formatted"
        )
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
}
