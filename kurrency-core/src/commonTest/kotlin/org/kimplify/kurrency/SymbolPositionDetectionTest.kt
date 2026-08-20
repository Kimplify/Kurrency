package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * [SymbolPosition.LOCALE_DEFAULT] asks the platform where the locale puts the symbol, and the symbol
 * itself comes from the platform too. Both are fixed for a formatter — its locale cannot change — so
 * both are resolved once per currency and cached, and a platform that cannot answer the placement
 * logs its fallback instead of silently choosing English placement.
 */
class SymbolPositionDetectionTest {

    private val symbolFirst = CurrencyFormatOptions(
        symbolDisplay = SymbolDisplay.SYMBOL,
        symbolPosition = SymbolPosition.LOCALE_DEFAULT,
    )

    @Test
    fun aUsReaderGetsTheSymbolBeforeTheAmount() {
        val formatted = CurrencyFormatter(KurrencyLocale.US)
            .formatWithOptions("1234.56", "USD", symbolFirst)
            .getOrThrow()
        assertEquals("$1,234.56", formatted)
    }

    @Test
    fun aGermanReaderGetsTheSymbolAfterTheAmount() {
        val formatted = CurrencyFormatter(KurrencyLocale.GERMANY)
            .formatWithOptions("1234.56", "EUR", symbolFirst)
            .getOrThrow()
        assertEquals("1.234,56 €", formatted)
    }

    @Test
    fun theCachedAnswerMatchesTheFirstOne() {
        val formatter = CurrencyFormatter(KurrencyLocale.GERMANY)
        val first = formatter.formatWithOptions("1234.56", "EUR", symbolFirst).getOrThrow()
        val second = formatter.formatWithOptions("1234.56", "EUR", symbolFirst).getOrThrow()
        val third = formatter.formatWithOptions("9.99", "EUR", symbolFirst).getOrThrow()
        assertEquals(first, second)
        assertEquals("9,99 €", third)
    }

    @Test
    fun interleavedCurrenciesKeepTheirOwnCachedSymbolAndPlacement() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val expected = listOf("A$1,234.56", "$1,234.56", "€1,234.56")
        val codes = listOf("AUD", "USD", "EUR")
        repeat(3) {
            codes.forEachIndexed { index, code ->
                assertEquals(
                    expected[index],
                    formatter.formatWithOptions("1234.56", code, symbolFirst).getOrThrow(),
                )
            }
        }
    }

    @Test
    fun oneCurrencysPlacementDoesNotLeakToAnother() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val euro = formatter.formatWithOptions("1234.56", "EUR", symbolFirst).getOrThrow()
        val dollar = formatter.formatWithOptions("1234.56", "USD", symbolFirst).getOrThrow()
        assertEquals("€1,234.56", euro)
        assertEquals("$1,234.56", dollar)
    }
}
