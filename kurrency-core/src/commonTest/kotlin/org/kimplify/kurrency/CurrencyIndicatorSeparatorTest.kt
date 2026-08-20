package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A currency symbol abuts the amount; an ISO code or a currency name is a word and has to be
 * separated from it. Before these cases existed the assembly used one rule for every indicator,
 * so a leading code rendered as "AUD1,234.56".
 */
class CurrencyIndicatorSeparatorTest {

    private val aud = Kurrency.fromCode("AUD").getOrThrow()

    @Test
    fun symbolAbutsTheAmount() {
        assertEquals("$1,234.56", format(SymbolDisplay.SYMBOL))
    }

    @Test
    fun isoCodeIsSeparatedFromTheAmount() {
        assertEquals("AUD 1,234.56", format(SymbolDisplay.ISO_CODE))
    }

    @Test
    fun currencyNameIsSeparatedFromTheAmount() {
        assertEquals("Australian Dollars 1,234.56", format(SymbolDisplay.NAME))
    }

    @Test
    fun theIsoPresetIsSeparatedToo() {
        assertEquals(
            "AUD 1,234.56",
            aud.formatAmountWithOptions("1234.56", CurrencyFormatOptions.ISO).getOrThrow(),
        )
    }

    @Test
    fun noIndicatorLeavesNoStraySeparator() {
        assertEquals("1,234.56", format(SymbolDisplay.NONE))
    }

    @Test
    fun aTrailingIndicatorKeepsItsSingleSpace() {
        assertEquals(
            "1,234.56 $",
            aud.formatAmountWithOptions(
                "1234.56",
                CurrencyFormatOptions(
                    symbolDisplay = SymbolDisplay.SYMBOL,
                    symbolPosition = SymbolPosition.TRAILING,
                ),
            ).getOrThrow(),
        )
    }

    @Test
    fun aNegativeAmountKeepsTheSeparatorInsideTheSign() {
        assertEquals(
            "-AUD 1,234.56",
            aud.formatAmountWithOptions(
                "-1234.56",
                CurrencyFormatOptions(symbolDisplay = SymbolDisplay.ISO_CODE),
            ).getOrThrow(),
        )
    }

    @Test
    fun aParenthesisedNegativeWrapsTheWholeIndicatorAndAmount() {
        assertEquals(
            "(AUD 1,234.56)",
            aud.formatAmountWithOptions(
                "-1234.56",
                CurrencyFormatOptions(
                    symbolDisplay = SymbolDisplay.ISO_CODE,
                    negativeStyle = NegativeStyle.PARENTHESES,
                ),
            ).getOrThrow(),
        )
    }

    private fun format(symbolDisplay: SymbolDisplay): String =
        aud.formatAmountWithOptions(
            "1234.56",
            CurrencyFormatOptions(symbolDisplay = symbolDisplay, symbolPosition = SymbolPosition.LEADING),
        ).getOrThrow()
}
