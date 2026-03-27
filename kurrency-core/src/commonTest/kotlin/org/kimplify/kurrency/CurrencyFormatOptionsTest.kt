package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CurrencyFormatOptionsTest {

    // ---- Data class & builder tests ----

    @Test
    fun defaultOptionsMatchStandardPreset() {
        val defaults = CurrencyFormatOptions()
        assertEquals(CurrencyFormatOptions.STANDARD, defaults)
        assertEquals(SymbolPosition.LOCALE_DEFAULT, defaults.symbolPosition)
        assertEquals(true, defaults.grouping)
        assertEquals(null, defaults.minFractionDigits)
        assertEquals(null, defaults.maxFractionDigits)
        assertEquals(NegativeStyle.MINUS_SIGN, defaults.negativeStyle)
        assertEquals(SymbolDisplay.SYMBOL, defaults.symbolDisplay)
        assertEquals(ZeroDisplay.SHOW, defaults.zeroDisplay)
    }

    @Test
    fun isoPresetUsesIsoCode() {
        assertEquals(SymbolDisplay.ISO_CODE, CurrencyFormatOptions.ISO.symbolDisplay)
    }

    @Test
    fun accountingPresetUsesParentheses() {
        assertEquals(NegativeStyle.PARENTHESES, CurrencyFormatOptions.ACCOUNTING.negativeStyle)
    }

    @Test
    fun builderProducesCorrectOptions() {
        val options = CurrencyFormatOptions.builder()
            .symbolPosition(SymbolPosition.TRAILING)
            .grouping(false)
            .minFractionDigits(0)
            .maxFractionDigits(4)
            .negativeStyle(NegativeStyle.PARENTHESES)
            .symbolDisplay(SymbolDisplay.ISO_CODE)
            .zeroDisplay(ZeroDisplay.DASH)
            .build()

        assertEquals(SymbolPosition.TRAILING, options.symbolPosition)
        assertEquals(false, options.grouping)
        assertEquals(0, options.minFractionDigits)
        assertEquals(4, options.maxFractionDigits)
        assertEquals(NegativeStyle.PARENTHESES, options.negativeStyle)
        assertEquals(SymbolDisplay.ISO_CODE, options.symbolDisplay)
        assertEquals(ZeroDisplay.DASH, options.zeroDisplay)
    }

    @Test
    fun dslProducesCorrectOptions() {
        val options = CurrencyFormatOptions {
            symbolDisplay = SymbolDisplay.NONE
            grouping = false
            zeroDisplay = ZeroDisplay.EMPTY
        }

        assertEquals(SymbolDisplay.NONE, options.symbolDisplay)
        assertEquals(false, options.grouping)
        assertEquals(ZeroDisplay.EMPTY, options.zeroDisplay)
        // Rest should be defaults
        assertEquals(SymbolPosition.LOCALE_DEFAULT, options.symbolPosition)
        assertEquals(NegativeStyle.MINUS_SIGN, options.negativeStyle)
    }

    @Test
    fun invalidFractionDigitsThrows() {
        assertFailsWith<IllegalArgumentException> {
            CurrencyFormatOptions(minFractionDigits = 5, maxFractionDigits = 2)
        }
    }

    @Test
    fun negativeFractionDigitsThrows() {
        assertFailsWith<IllegalArgumentException> {
            CurrencyFormatOptions(minFractionDigits = -1)
        }
        assertFailsWith<IllegalArgumentException> {
            CurrencyFormatOptions(maxFractionDigits = -1)
        }
    }

    // ---- Formatting tests (using US locale for deterministic output) ----

    private val usLocale = KurrencyLocale.US

    @Test
    fun formatStandardDefaultOptions() {
        val formatter = CurrencyFormatter(usLocale)
        val result = formatter.formatWithOptions("1234.56", "USD", CurrencyFormatOptions.STANDARD)
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()!!
        // Should contain the dollar sign and digits
        assertTrue(formatted.contains("$"), "Should contain dollar sign: $formatted")
        assertTrue(formatted.contains("1") && formatted.contains("234"), "Should contain digits: $formatted")
        assertTrue(formatted.contains("56"), "Should contain cents: $formatted")
    }

    @Test
    fun formatGroupingDisabledRemovesGroupingSeparators() {
        val formatter = CurrencyFormatter(usLocale)
        val opts = CurrencyFormatOptions { grouping = false }
        val result = formatter.formatWithOptions("1234567.89", "USD", opts)
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()!!
        // No commas should be present
        assertFalse(formatted.contains(","), "Should not contain grouping separator: $formatted")
        assertTrue(formatted.contains("1234567"), "Should contain ungrouped digits: $formatted")
    }

    @Test
    fun formatSymbolDisplayNoneRemovesSymbolAndCode() {
        val formatter = CurrencyFormatter(usLocale)
        val opts = CurrencyFormatOptions { symbolDisplay = SymbolDisplay.NONE }
        val result = formatter.formatWithOptions("100.50", "USD", opts)
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()!!
        assertFalse(formatted.contains("$"), "Should not contain dollar sign: $formatted")
        assertFalse(formatted.contains("USD"), "Should not contain ISO code: $formatted")
        assertTrue(formatted.contains("100"), "Should contain digits: $formatted")
    }

    @Test
    fun formatSymbolDisplayIsoCodeShowsCode() {
        val formatter = CurrencyFormatter(usLocale)
        val opts = CurrencyFormatOptions { symbolDisplay = SymbolDisplay.ISO_CODE }
        val result = formatter.formatWithOptions("100.50", "USD", opts)
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()!!
        assertTrue(formatted.contains("USD"), "Should contain ISO code: $formatted")
        assertTrue(formatted.contains("100"), "Should contain digits: $formatted")
    }

    @Test
    fun formatSymbolDisplayNameShowsCurrencyName() {
        val formatter = CurrencyFormatter(usLocale)
        val opts = CurrencyFormatOptions { symbolDisplay = SymbolDisplay.NAME }
        val result = formatter.formatWithOptions("100.50", "USD", opts)
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()!!
        assertTrue(
            formatted.contains("US Dollars") || formatted.contains("US Dollar"),
            "Should contain currency name: $formatted"
        )
    }

    @Test
    fun formatSymbolDisplayNameUsesPluralForNonOne() {
        val formatter = CurrencyFormatter(usLocale)
        val opts = CurrencyFormatOptions { symbolDisplay = SymbolDisplay.NAME }

        val pluralResult = formatter.formatWithOptions("2.00", "USD", opts)
        assertTrue(pluralResult.getOrNull()!!.contains("US Dollars"), "Should use plural: ${pluralResult.getOrNull()}")

        val singularResult = formatter.formatWithOptions("1.00", "USD", opts)
        assertTrue(singularResult.getOrNull()!!.contains("US Dollar"), "Should use singular: ${singularResult.getOrNull()}")
    }

    @Test
    fun formatNegativeStyleParenthesesWrapsNegatives() {
        val formatter = CurrencyFormatter(usLocale)
        val opts = CurrencyFormatOptions { negativeStyle = NegativeStyle.PARENTHESES }
        val result = formatter.formatWithOptions("-100.50", "USD", opts)
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()!!
        assertTrue(formatted.startsWith("("), "Should start with '(': $formatted")
        assertTrue(formatted.endsWith(")"), "Should end with ')': $formatted")
        assertFalse(formatted.contains("-"), "Should not contain minus sign: $formatted")
    }

    @Test
    fun formatNegativeStyleParenthesesPositiveUnchanged() {
        val formatter = CurrencyFormatter(usLocale)
        val opts = CurrencyFormatOptions { negativeStyle = NegativeStyle.PARENTHESES }
        val result = formatter.formatWithOptions("100.50", "USD", opts)
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()!!
        assertFalse(formatted.contains("("), "Positive should not have parentheses: $formatted")
    }

    @Test
    fun formatZeroDisplayDashShowsEmDash() {
        val formatter = CurrencyFormatter(usLocale)
        val opts = CurrencyFormatOptions { zeroDisplay = ZeroDisplay.DASH }
        val result = formatter.formatWithOptions("0", "USD", opts)
        assertTrue(result.isSuccess)
        assertEquals("\u2014", result.getOrNull())
    }

    @Test
    fun formatZeroDisplayEmptyReturnsEmptyString() {
        val formatter = CurrencyFormatter(usLocale)
        val opts = CurrencyFormatOptions { zeroDisplay = ZeroDisplay.EMPTY }
        val result = formatter.formatWithOptions("0.00", "USD", opts)
        assertTrue(result.isSuccess)
        assertEquals("", result.getOrNull())
    }

    @Test
    fun formatZeroDisplayShowFormatsNormally() {
        val formatter = CurrencyFormatter(usLocale)
        val opts = CurrencyFormatOptions { zeroDisplay = ZeroDisplay.SHOW }
        val result = formatter.formatWithOptions("0", "USD", opts)
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()!!
        assertTrue(formatted.contains("0"), "Should show zero: $formatted")
    }

    @Test
    fun formatCustomFractionDigitsMaxZero() {
        val formatter = CurrencyFormatter(usLocale)
        val opts = CurrencyFormatOptions { maxFractionDigits = 0 }
        val result = formatter.formatWithOptions("100.99", "USD", opts)
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()!!
        // Should be rounded, no decimal part
        assertFalse(formatted.contains("."), "Should not contain decimal separator: $formatted")
        assertTrue(formatted.contains("101"), "Should be rounded: $formatted")
    }

    @Test
    fun formatCustomFractionDigitsMore() {
        val formatter = CurrencyFormatter(usLocale)
        val opts = CurrencyFormatOptions {
            minFractionDigits = 4
            maxFractionDigits = 4
        }
        val result = formatter.formatWithOptions("100.5", "USD", opts)
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()!!
        assertTrue(formatted.contains("5000"), "Should pad to 4 fraction digits: $formatted")
    }

    @Test
    fun formatSymbolPositionLeading() {
        val formatter = CurrencyFormatter(usLocale)
        val opts = CurrencyFormatOptions { symbolPosition = SymbolPosition.LEADING }
        val result = formatter.formatWithOptions("100.50", "USD", opts)
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()!!
        assertTrue(formatted.contains("$"), "Should contain $: $formatted")
        val dollarIndex = formatted.indexOf('$')
        val digitIndex = formatted.indexOfFirst { it.isDigit() }
        assertTrue(dollarIndex < digitIndex, "Symbol should come before digits: $formatted")
    }

    @Test
    fun formatSymbolPositionTrailing() {
        val formatter = CurrencyFormatter(usLocale)
        val opts = CurrencyFormatOptions { symbolPosition = SymbolPosition.TRAILING }
        val result = formatter.formatWithOptions("100.50", "USD", opts)
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()!!
        assertTrue(formatted.contains("$"), "Should contain $: $formatted")
        val dollarIndex = formatted.indexOf('$')
        val digitIndex = formatted.indexOfLast { it.isDigit() }
        assertTrue(dollarIndex > digitIndex, "Symbol should come after digits: $formatted")
    }

    // ---- Integration with Kurrency and CurrencyAmount ----

    @Test
    fun kurrencyFormatAmountWithOptions() {
        val result = Kurrency.USD.formatAmountWithOptions(
            "1234.56",
            CurrencyFormatOptions { symbolDisplay = SymbolDisplay.ISO_CODE },
            usLocale,
        )
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()!!
        assertTrue(formatted.contains("USD"), "Should contain USD: $formatted")
        assertTrue(formatted.contains("1") && formatted.contains("234"), "Should contain digits: $formatted")
    }

    @Test
    fun kurrencyFormatAmountWithOptionsDouble() {
        val result = Kurrency.EUR.formatAmountWithOptions(
            1234.56,
            CurrencyFormatOptions.STANDARD,
            usLocale,
        )
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()!!
        assertTrue(formatted.contains("1") && formatted.contains("234"), "Should contain digits: $formatted")
    }

    @Test
    fun currencyAmountFormatWithOptions() {
        val amount = CurrencyAmount.of(12345L, Kurrency.USD) // $123.45
        val opts = CurrencyFormatOptions {
            symbolDisplay = SymbolDisplay.NONE
            grouping = false
        }
        val result = amount.format(opts, usLocale)
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()!!
        assertFalse(formatted.contains("$"), "Should not contain $: $formatted")
        assertTrue(formatted.contains("123"), "Should contain 123: $formatted")
    }

    @Test
    fun currencyAmountFormatOrEmptyWithOptions() {
        val amount = CurrencyAmount.of(10050L, Kurrency.USD) // $100.50
        val formatted = amount.formatOrEmpty(CurrencyFormatOptions.ISO, usLocale)
        assertTrue(formatted.contains("USD"), "Should contain USD: $formatted")
    }

    @Test
    fun formatInvalidAmountReturnsFailure() {
        val formatter = CurrencyFormatter(usLocale)
        val result = formatter.formatWithOptions("not_a_number", "USD", CurrencyFormatOptions.STANDARD)
        assertTrue(result.isFailure)
    }

    @Test
    fun formatInvalidCurrencyCodeReturnsFailure() {
        val formatter = CurrencyFormatter(usLocale)
        val result = formatter.formatWithOptions("100", "XYZ", CurrencyFormatOptions.STANDARD)
        assertTrue(result.isFailure)
    }

    @Test
    fun formatWithGermanyLocale() {
        val formatter = CurrencyFormatter(KurrencyLocale.GERMANY)
        val opts = CurrencyFormatOptions { symbolDisplay = SymbolDisplay.NONE }
        val result = formatter.formatWithOptions("1234.56", "EUR", opts)
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()!!
        // German locale uses . as grouping and , as decimal
        assertTrue(formatted.contains("1") && formatted.contains("234"), "Should contain digits: $formatted")
    }

    @Test
    fun formatJpyZeroFractionDigits() {
        val formatter = CurrencyFormatter(usLocale)
        val result = formatter.formatWithOptions("1234", "JPY", CurrencyFormatOptions.STANDARD)
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()!!
        // JPY has 0 fraction digits by default
        assertTrue(formatted.contains("1") && formatted.contains("234"), "Should contain digits: $formatted")
        // Should not have a decimal separator if JPY defaults to 0 fraction digits
        assertFalse(formatted.contains("."), "JPY should not have decimal: $formatted")
    }

    @Test
    fun formatCombinedOptionsNoGroupingParenthesesIsoCode() {
        val formatter = CurrencyFormatter(usLocale)
        val opts = CurrencyFormatOptions {
            grouping = false
            negativeStyle = NegativeStyle.PARENTHESES
            symbolDisplay = SymbolDisplay.ISO_CODE
        }
        val result = formatter.formatWithOptions("-1234567.89", "USD", opts)
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()!!
        assertTrue(formatted.startsWith("("), "Should start with (: $formatted")
        assertTrue(formatted.endsWith(")"), "Should end with ): $formatted")
        assertTrue(formatted.contains("USD"), "Should contain USD: $formatted")
        assertFalse(formatted.contains(","), "Should not have grouping: $formatted")
        assertFalse(formatted.contains("-"), "Should not contain minus: $formatted")
    }
}
