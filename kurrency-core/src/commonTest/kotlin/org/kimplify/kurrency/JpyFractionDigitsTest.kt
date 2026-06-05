package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Regression tests for issue #10: a currency's fraction digits must follow the
 * currency (JPY = 0), not the display locale's default currency.
 */
class JpyFractionDigitsTest {

    @Test
    fun formatCurrencyStyle_jpy_nonJapaneseLocale_hasNoDecimalDigits() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyleResult("1234", "JPY")
        assertTrue(result.isSuccess, "Formatting should succeed")
        val formatted = result.getOrThrow()
        assertFalse(
            Regex("""\.\d""").containsMatchIn(formatted),
            "JPY must not show decimal places under a non-JP locale: $formatted"
        )
        assertTrue(formatted.contains("1") && formatted.contains("234"), "Expected digits in: $formatted")
    }
}
