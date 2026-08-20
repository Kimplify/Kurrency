package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

/**
 * A platform that cannot format used to return the unformatted input from every path, so a failure
 * was indistinguishable from success and `Result` could never fail. The lenient contract the
 * [CurrencyFormat] methods document is unchanged — they still hand back the amount — but the
 * failure is now reachable through [CurrencyFormatterImpl.formatOrThrow], which is what
 * [CurrencyFormatter] uses for the paths that return a `Result`.
 */
class PlatformFailureSurfacesTest {

    private val impl = CurrencyFormatterImpl(KurrencyLocale.US)

    @Test
    fun theDocumentedMethodsStayLenient() {
        assertEquals("1234.56", impl.formatCurrencyStyle("1234.56", "XYZ"))
        assertEquals("1234.56", impl.formatIsoCurrencyStyle("1234.56", "XYZ"))
        assertEquals("1234.56", impl.formatCompactStyle("1234.56", "XYZ"))
    }

    @Test
    fun theThrowingPathReportsWhatTheLenientOneHides() {
        assertFails { impl.formatOrThrow("1234.56", "XYZ", PlatformFormatStyle.SYMBOL) }
        assertFails { impl.formatOrThrow("1234.56", "XYZ", PlatformFormatStyle.ISO_CODE) }
        assertFails { impl.formatOrThrow("1234.56", "XYZ", PlatformFormatStyle.COMPACT) }
    }

    @Test
    fun bothPathsAgreeOnASupportedCurrency() {
        assertEquals("$1,234.56", impl.formatCurrencyStyle("1234.56", "USD"))
        assertEquals(
            "$1,234.56",
            impl.formatOrThrow("1234.56", "USD", PlatformFormatStyle.SYMBOL),
        )
    }

    @Test
    fun aDefaultingAccessorKeepsItsDefault() {
        assertEquals(7, impl.getFractionDigitsOrDefault("XYZ", default = 7))
        assertEquals("fallback", impl.getCurrencySymbolOrDefault("XYZ", default = "fallback"))
    }
}
