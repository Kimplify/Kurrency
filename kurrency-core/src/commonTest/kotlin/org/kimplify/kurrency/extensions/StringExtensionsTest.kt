package org.kimplify.kurrency.extensions

import kotlin.test.Test
import kotlin.test.assertEquals

class StringExtensionsTest {

    @Test fun plainDotDecimal() = assertEquals("1234.56", "1234.56".normalizeAmount())
    @Test fun plainCommaDecimal() = assertEquals("1234.56", "1234,56".normalizeAmount())
    @Test fun plainInteger() = assertEquals("1234", "1234".normalizeAmount())
    @Test fun subUnitDot() = assertEquals("0.99", "0.99".normalizeAmount())
    @Test fun subUnitComma() = assertEquals("0.99", "0,99".normalizeAmount())

    @Test fun usGroupedSimple() = assertEquals("1234.56", "1,234.56".normalizeAmount())
    @Test fun usGroupedLarge() = assertEquals("1234567.89", "1,234,567.89".normalizeAmount())
    @Test fun usGroupedThousand() = assertEquals("12345.00", "12,345.00".normalizeAmount())

    @Test fun euGroupedSimple() = assertEquals("1234.56", "1.234,56".normalizeAmount())
    @Test fun euGroupedLarge() = assertEquals("1234567.89", "1.234.567,89".normalizeAmount())

    @Test fun spaceGroupedCommaDecimal() = assertEquals("1234567.89", "1 234 567,89".normalizeAmount())
    @Test fun spaceGroupedDotDecimal() = assertEquals("1234567.89", "1 234 567.89".normalizeAmount())
    @Test fun nbspGrouped() = assertEquals("1234.56", "1\u00A0234,56".normalizeAmount())
    @Test fun narrowNbspGrouped() = assertEquals("1234.56", "1\u202F234,56".normalizeAmount())
    @Test fun thinSpaceGrouped() = assertEquals("1234.56", "1\u2009234,56".normalizeAmount())

    @Test fun apostropheGrouped() = assertEquals("1234567.89", "1'234'567.89".normalizeAmount())
    @Test fun rightQuoteGrouped() = assertEquals("1234567.89", "1\u2019234\u2019567.89".normalizeAmount())

    @Test fun negativeDotDecimal() = assertEquals("-1234.56", "-1234.56".normalizeAmount())
    @Test fun negativeUsGrouped() = assertEquals("-1234.56", "-1,234.56".normalizeAmount())
    @Test fun negativeEuGrouped() = assertEquals("-1234.56", "-1.234,56".normalizeAmount())

    @Test fun positiveSigned() = assertEquals("+1234.56", "+1234.56".normalizeAmount())

    @Test fun multipleDotsAreGrouping() = assertEquals("1234567", "1.234.567".normalizeAmount())
    @Test fun multipleCommasAreGrouping() = assertEquals("1234567", "1,234,567".normalizeAmount())

    @Test fun ambiguousCommaThreeDigits() = assertEquals("1234", "1,234".normalizeAmount())
    @Test fun ambiguousCommaThreeDigitsLarger() = assertEquals("12345", "12,345".normalizeAmount())

    @Test fun singleCommaTwoDigits() = assertEquals("1.50", "1,50".normalizeAmount())
    @Test fun singleCommaOneDigit() = assertEquals("1.5", "1,5".normalizeAmount())
    @Test fun singleCommaFourDigits() = assertEquals("1.1234", "1,1234".normalizeAmount())

    @Test fun indianGrouping() = assertEquals("1234567.89", "12,34,567.89".normalizeAmount())

    @Test fun trailingDot() = assertEquals("1234.", "1234.".normalizeAmount())
    @Test fun trailingComma() = assertEquals("1234.", "1234,".normalizeAmount())

    @Test fun zero() = assertEquals("0", "0".normalizeAmount())
    @Test fun zeroWithDotDecimals() = assertEquals("0.00", "0.00".normalizeAmount())
    @Test fun zeroWithCommaDecimals() = assertEquals("0.00", "0,00".normalizeAmount())

    @Test fun emptyString() = assertEquals("", "".normalizeAmount())
    @Test fun whitespaceOnly() = assertEquals("", "   ".normalizeAmount())
    @Test fun trimWhitespace() = assertEquals("1234.56", "  1234.56  ".normalizeAmount())
    @Test fun nonNumericPassesThrough() = assertEquals("abc", "abc".normalizeAmount())

    @Test fun singleDotAlwaysDecimal() = assertEquals("1.234", "1.234".normalizeAmount())
    @Test fun ambiguousThreeDecimalCurrency() = assertEquals("1234", "1,234".normalizeAmount())
    @Test fun existingCommaDecimalCase() = assertEquals("100.50", "100,50".normalizeAmount())

    @Test fun negativeEuGroupedLarge() = assertEquals("-1234567.89", "-1.234.567,89".normalizeAmount())
    @Test fun negativeCommaGroupedInteger() = assertEquals("-1234", "-1,234".normalizeAmount())
    @Test fun leadingDotDecimal() = assertEquals(".5", ".5".normalizeAmount())
    @Test fun leadingCommaDecimal() = assertEquals(".5", ",5".normalizeAmount())
    @Test fun largeEuInteger() = assertEquals("1000000", "1.000.000".normalizeAmount())
    @Test fun signOnly() = assertEquals("-", "-".normalizeAmount())
    @Test fun plusSignOnly() = assertEquals("+", "+".normalizeAmount())
    @Test fun apostropheWithCommaDecimal() = assertEquals("1234.56", "1'234,56".normalizeAmount())
    @Test fun positiveUsGrouped() = assertEquals("+1234.56", "+1,234.56".normalizeAmount())
    @Test fun positiveEuGrouped() = assertEquals("+1234.56", "+1.234,56".normalizeAmount())
    @Test fun singleDigit() = assertEquals("5", "5".normalizeAmount())
    @Test fun veryLargeUsGrouped() = assertEquals("999999999999.99", "999,999,999,999.99".normalizeAmount())
    @Test fun singleCommaZeroDigitsAfter() = assertEquals("1234.", "1234,".normalizeAmount())

    @Test fun currencySymbolPrefix() = assertEquals("$1234.56", "$1,234.56".normalizeAmount())
    @Test fun currencySymbolSuffix() = assertEquals("1234.56€", "1.234,56€".normalizeAmount())
    @Test fun currencyCodePrefix() = assertEquals("USD1234.56", "USD1,234.56".normalizeAmount())
    @Test fun onlyDot() = assertEquals(".", ".".normalizeAmount())
    @Test fun onlyComma() = assertEquals(".", ",".normalizeAmount())
    @Test fun dotsAndCommasNoDigits() = assertEquals("..", ".,.".normalizeAmount())
    @Test fun multipleSignsPassThrough() = assertEquals("--1234", "--1234".normalizeAmount())
    @Test fun spacesOnlyAfterSign() = assertEquals("-", "-   ".normalizeAmount())
    @Test fun lettersWithCommas() = assertEquals("abc", "a,b,c".normalizeAmount())
    @Test fun unicodeDigits() = assertEquals("١٢٣", "١٢٣".normalizeAmount())
    @Test fun mixedLettersAndDigits() = assertEquals("abc123.45", "abc123.45".normalizeAmount())
    @Test fun newlinesAndTabs() = assertEquals("1234.56", "\t1234.56\n".normalizeAmount())
    @Test fun infinityString() = assertEquals("Infinity", "Infinity".normalizeAmount())
    @Test fun nanString() = assertEquals("NaN", "NaN".normalizeAmount())
    @Test fun emptyAfterStrippingGrouping() = assertEquals("' '", "' '".normalizeAmount())
}
