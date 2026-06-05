package org.kimplify.kurrency

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Regression test for issue #10 on Android (java.text.NumberFormat / ICU backed):
 * JPY has 0 fraction digits and must not show decimals under a non-JP locale.
 */
@RunWith(AndroidJUnit4::class)
class JpyFractionDigitsTestInstrumented {

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
