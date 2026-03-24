package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CurrencyParsingTest {

    // ---- Round-trip Standard style ----

    @Test
    fun testRoundTrip_US_Standard() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val formatted = formatter.formatCurrencyStyle("1234.56", "USD")
        val parsed = formatter.parseCurrencyAmountResult(formatted, "USD")
        assertTrue(parsed.isSuccess, "Should parse US formatted amount: '$formatted'")
        assertEquals(1234.56, parsed.getOrNull()!!, 0.01)
    }

    @Test
    fun testRoundTrip_Germany_Standard() {
        val formatter = CurrencyFormatter(KurrencyLocale.GERMANY)
        val formatted = formatter.formatCurrencyStyle("1234.56", "EUR")
        val parsed = formatter.parseCurrencyAmountResult(formatted, "EUR")
        assertTrue(parsed.isSuccess, "Should parse German formatted amount: '$formatted'")
        assertEquals(1234.56, parsed.getOrNull()!!, 0.01)
    }

    @Test
    fun testRoundTrip_Japan_Standard() {
        val formatter = CurrencyFormatter(KurrencyLocale.JAPAN)
        val formatted = formatter.formatCurrencyStyle("1234", "JPY")
        val parsed = formatter.parseCurrencyAmountResult(formatted, "JPY")
        assertTrue(parsed.isSuccess, "Should parse Japanese formatted amount: '$formatted'")
        assertEquals(1234.0, parsed.getOrNull()!!, 0.01)
    }

    @Test
    fun testRoundTrip_Brazil_Standard() {
        val formatter = CurrencyFormatter(KurrencyLocale.BRAZIL)
        val formatted = formatter.formatCurrencyStyle("1234.56", "BRL")
        val parsed = formatter.parseCurrencyAmountResult(formatted, "BRL")
        assertTrue(parsed.isSuccess, "Should parse Brazilian formatted amount: '$formatted'")
        assertEquals(1234.56, parsed.getOrNull()!!, 0.01)
    }

    // ---- Round-trip ISO style ----

    @Test
    fun testRoundTrip_US_IsoStyle() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val formatted = formatter.formatIsoCurrencyStyle("1234.56", "USD")
        val parsed = formatter.parseCurrencyAmountResult(formatted, "USD")
        assertTrue(parsed.isSuccess, "Should parse ISO formatted amount: '$formatted'")
        assertEquals(1234.56, parsed.getOrNull()!!, 0.01)
    }

    // ---- Accounting negatives ----

    @Test
    fun testParseAccountingNegative() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val parsed = formatter.parseCurrencyAmountResult("(\$1,234.56)", "USD")
        assertTrue(parsed.isSuccess, "Should parse accounting negative")
        assertEquals(-1234.56, parsed.getOrNull()!!, 0.01)
    }

    @Test
    fun testParseAccountingNegativeWithoutSymbol() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val parsed = formatter.parseCurrencyAmountResult("(1,234.56)", "USD")
        assertTrue(parsed.isSuccess, "Should parse accounting negative without symbol")
        assertEquals(-1234.56, parsed.getOrNull()!!, 0.01)
    }

    // ---- Compact parsing (English only) ----

    @Test
    fun testParseCompactK() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val parsed = formatter.parseCurrencyAmountResult("\$1.2K", "USD")
        assertTrue(parsed.isSuccess, "Should parse compact K suffix")
        assertEquals(1200.0, parsed.getOrNull()!!, 0.01)
    }

    @Test
    fun testParseCompactM() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val parsed = formatter.parseCurrencyAmountResult("\$1.5M", "USD")
        assertTrue(parsed.isSuccess, "Should parse compact M suffix")
        assertEquals(1500000.0, parsed.getOrNull()!!, 0.01)
    }

    @Test
    fun testParseCompactB() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val parsed = formatter.parseCurrencyAmountResult("\$2B", "USD")
        assertTrue(parsed.isSuccess, "Should parse compact B suffix")
        assertEquals(2000000000.0, parsed.getOrNull()!!, 0.01)
    }

    @Test
    fun testParseCompactT() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val parsed = formatter.parseCurrencyAmountResult("\$1T", "USD")
        assertTrue(parsed.isSuccess, "Should parse compact T suffix")
        assertEquals(1000000000000.0, parsed.getOrNull()!!, 0.01)
    }

    // ---- Non-English compact rejected ----

    @Test
    fun testNonEnglishCompactRejected() {
        val formatter = CurrencyFormatter(KurrencyLocale.GERMANY)
        val result = formatter.parseCurrencyAmountResult("1,5Mio", "EUR")
        assertTrue(result.isFailure, "Non-English compact suffix should be rejected")
        assertTrue(result.exceptionOrNull() is KurrencyError.InvalidAmount)
    }

    // ---- Edge cases ----

    @Test
    fun testParseZero() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val formatted = formatter.formatCurrencyStyle("0.00", "USD")
        val parsed = formatter.parseCurrencyAmountResult(formatted, "USD")
        assertTrue(parsed.isSuccess, "Should parse zero amount: '$formatted'")
        assertEquals(0.0, parsed.getOrNull()!!, 0.01)
    }

    @Test
    fun testParseEmptyString() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.parseCurrencyAmountResult("", "USD")
        assertTrue(result.isFailure, "Empty string should fail")
        assertTrue(result.exceptionOrNull() is KurrencyError.InvalidAmount)
    }

    @Test
    fun testParseWhitespace() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.parseCurrencyAmountResult("   ", "USD")
        assertTrue(result.isFailure, "Whitespace should fail")
        assertTrue(result.exceptionOrNull() is KurrencyError.InvalidAmount)
    }

    @Test
    fun testParseInvalidInput() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.parseCurrencyAmountResult("abc", "USD")
        assertTrue(result.isFailure, "Invalid input should fail")
        assertTrue(result.exceptionOrNull() is KurrencyError.InvalidAmount)
    }

    @Test
    fun testParseInvalidCurrencyCode() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.parseCurrencyAmountResult("$100.00", "XYZ")
        assertTrue(result.isFailure, "Invalid currency code should fail")
        assertTrue(result.exceptionOrNull() is KurrencyError.InvalidCurrencyCode)
    }

    // ---- parseToMinorUnitsResult ----

    @Test
    fun testParseToMinorUnits_USD() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val formatted = formatter.formatCurrencyStyle("1234.56", "USD")
        val result = formatter.parseToMinorUnitsResult(formatted, "USD")
        assertTrue(result.isSuccess, "Should parse to minor units: '$formatted'")
        assertEquals(123456L, result.getOrNull())
    }

    @Test
    fun testParseToMinorUnits_JPY() {
        val formatter = CurrencyFormatter(KurrencyLocale.JAPAN)
        val formatted = formatter.formatCurrencyStyle("1234", "JPY")
        val result = formatter.parseToMinorUnitsResult(formatted, "JPY")
        assertTrue(result.isSuccess, "Should parse JPY to minor units: '$formatted'")
        assertEquals(1234L, result.getOrNull())
    }

    @Test
    fun testParseToMinorUnits_3FractionDigits() {
        // KWD has 3 fraction digits. Use parseToMinorUnitsResult on a known clean string.
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        // Format a 2-decimal value, which the JVM will preserve for KWD
        val formatted = formatter.formatCurrencyStyle("1234.56", "KWD")
        val result = formatter.parseToMinorUnitsResult(formatted, "KWD")
        assertTrue(result.isSuccess, "Should parse KWD to minor units: '$formatted'")
        // 1234.56 with 3 fraction digits = 1234560
        assertEquals(1234560L, result.getOrNull())
    }

    // ---- parseToCurrencyAmountResult ----

    @Test
    fun testParseToCurrencyAmount_USD() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val formatted = formatter.formatCurrencyStyle("1234.56", "USD")
        val result = formatter.parseToCurrencyAmountResult(formatted, Kurrency.USD)
        assertTrue(result.isSuccess, "Should parse to CurrencyAmount: '$formatted'")
        val amount = result.getOrNull()
        assertNotNull(amount)
        assertEquals(123456L, amount.minorUnits)
        assertEquals(Kurrency.USD, amount.currency)
    }

    // ---- CurrencyAmount.parse convenience ----

    @Test
    fun testCurrencyAmountParse() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val formatted = formatter.formatCurrencyStyle("99.99", "USD")
        val result = CurrencyAmount.parse(formatted, Kurrency.USD, KurrencyLocale.US)
        assertTrue(result.isSuccess, "CurrencyAmount.parse should succeed: '$formatted'")
        val amount = result.getOrNull()
        assertNotNull(amount)
        assertEquals(9999L, amount.minorUnits)
        assertEquals(Kurrency.USD, amount.currency)
    }

    // ---- RTL parsing ----

    @Test
    fun testParseRtlFormattedSAR() {
        val formatter = CurrencyFormatter(KurrencyLocale.SAUDI_ARABIA)
        val formatted = formatter.formatCurrencyStyle("1234.56", "SAR")
        val parsed = formatter.parseCurrencyAmountResult(formatted, "SAR")
        assertTrue(parsed.isSuccess, "Should parse RTL formatted SAR: '$formatted'")
        assertEquals(1234.56, parsed.getOrNull()!!, 0.01)
    }

    // ---- Negative amount parsing ----

    @Test
    fun testParseNegativeAmount() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val formatted = formatter.formatCurrencyStyle("-500.25", "USD")
        val parsed = formatter.parseCurrencyAmountResult(formatted, "USD")
        assertTrue(parsed.isSuccess, "Should parse negative amount: '$formatted'")
        assertEquals(-500.25, parsed.getOrNull()!!, 0.01)
    }

    // ---- Minor units edge cases ----

    @Test
    fun testParseToMinorUnits_Zero() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val formatted = formatter.formatCurrencyStyle("0.00", "USD")
        val result = formatter.parseToMinorUnitsResult(formatted, "USD")
        assertTrue(result.isSuccess, "Should parse zero to minor units: '$formatted'")
        assertEquals(0L, result.getOrNull())
    }

    @Test
    fun testParseToMinorUnits_NegativeUSD() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val formatted = formatter.formatCurrencyStyle("-50.75", "USD")
        val result = formatter.parseToMinorUnitsResult(formatted, "USD")
        assertTrue(result.isSuccess, "Should parse negative to minor units: '$formatted'")
        assertEquals(-5075L, result.getOrNull())
    }
}
