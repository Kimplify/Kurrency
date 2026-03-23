package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CurrencyTest {

    @Test
    fun testCurrencyCreation() {
        val currency = Kurrency.USD
        assertEquals("USD", currency.code)
        assertEquals(2, currency.fractionDigits.getOrNull())
    }

    @Test
    fun testJapaneseCurrencyFractionDigits() {
        val currency = Kurrency.JPY
        assertEquals("JPY", currency.code)
        assertEquals(0, currency.fractionDigits.getOrNull())
    }

    @Test
    fun testFromCodeWithSystemLocale() {
        val result = Kurrency.fromCode("USD")
        assertTrue(result.isSuccess)
        val currency = result.getOrNull()
        assertNotNull(currency)
        assertEquals("USD", currency?.code)
        assertEquals(2, currency?.fractionDigits?.getOrNull())
    }

    @Test
    fun testFromCodeWithJapaneseYen() {
        val result = Kurrency.fromCode("JPY")
        assertTrue(result.isSuccess)
        val currency = result.getOrNull()
        assertNotNull(currency)
        assertEquals("JPY", currency?.code)
        assertEquals(0, currency?.fractionDigits?.getOrNull())
    }

    @Test
    fun testFromCodeWithKuwaitiDinar() {
        val result = Kurrency.fromCode("KWD")
        assertTrue(result.isSuccess)
        val currency = result.getOrNull()
        assertNotNull(currency)
        assertEquals("KWD", currency?.code)
        assertEquals(3, currency?.fractionDigits?.getOrNull())
    }

    @Test
    fun testFromCodeWithInvalidCurrency() {
        val result = Kurrency.fromCode("INVALID")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is KurrencyError.InvalidCurrencyCode)
    }

    @Test
    fun testFromCodeWithShortCode() {
        val result = Kurrency.fromCode("US")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is KurrencyError.InvalidCurrencyCode)
    }

    @Test
    fun testFromCodeMultipleCurrencies() {
        val currencies = listOf("USD", "EUR", "GBP", "JPY", "CHF", "CAD")

        currencies.forEach { code ->
            val result = Kurrency.fromCode(code)
            assertTrue(result.isSuccess, "Should succeed for currency: $code")
            assertNotNull(result.getOrNull())
        }
    }

    @Test
    fun testFormatAmountReturnsSuccess() {
        val currency = Kurrency.USD
        val result = currency.formatAmount("100.50")

        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("100"))
    }

    @Test
    fun testFormatAmountWithDoubleReturnsSuccess() {
        val currency = Kurrency.USD
        val result = currency.formatAmount(100.50)

        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("100"))
    }

    @Test
    fun testFormatAmountStandardStyle() {
        val currency = Kurrency.USD
        val result = currency.formatAmount("1234.56", CurrencyStyle.Standard)

        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("1") && formatted.contains("234") && formatted.contains("56"))
    }

    @Test
    fun testFormatAmountIsoStyle() {
        val currency = Kurrency.USD
        val result = currency.formatAmount("1234.56", CurrencyStyle.Iso)

        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("USD"))
    }

    @Test
    fun testFormatAmountWithInvalidAmount() {
        val currency = Kurrency.USD
        val result = currency.formatAmount("invalid")

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is KurrencyError.InvalidAmount)
    }

    @Test
    fun testFormatAmountWithEmptyString() {
        val currency = Kurrency.USD
        val result = currency.formatAmount("")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is KurrencyError.InvalidAmount)
    }

    @Test
    fun testFormatAmountWithInvalidCurrencyCode() {
        val result = Kurrency.fromCode("INVALID")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is KurrencyError.InvalidCurrencyCode)
    }

    @Test
    fun testFormatAmountWithShortCurrencyCode() {
        val result = Kurrency.fromCode("US")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is KurrencyError.InvalidCurrencyCode)
    }

    @Test
    fun testFormatAmountWithLongCurrencyCode() {
        val result = Kurrency.fromCode("USDD")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is KurrencyError.InvalidCurrencyCode)
    }

    @Test
    fun testFormatAmountOrEmpty() {
        val currency = Kurrency.USD
        val formatted = currency.formatAmountOrEmpty("100.50")

        assertNotNull(formatted)
        assertFalse(formatted.isEmpty())
    }

    @Test
    fun testFormatAmountOrEmptyWithInvalidAmount() {
        val currency = Kurrency.USD
        val formatted = currency.formatAmountOrEmpty("invalid")

        assertEquals("", formatted)
    }

    @Test
    fun testFormatAmountWithZero() {
        val currency = Kurrency.USD
        val result = currency.formatAmount("0.00")

        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("0"))
    }

    @Test
    fun testFormatAmountWithNegativeNumber() {
        val currency = Kurrency.USD
        val result = currency.formatAmount("-100.50")

        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("100"))
        assertTrue(formatted.contains("-") || formatted.contains("("))
    }

    @Test
    fun testFormatAmountWithVeryLargeNumber() {
        val currency = Kurrency.USD
        val result = currency.formatAmount("999999999.99")

        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("999"))
    }

    @Test
    fun testFormatAmountWithCommaDecimalSeparator() {
        val currency = Kurrency.EUR
        val result = currency.formatAmount("100,50")

        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("100"))
    }

    @Test
    fun testFormatAmountWithUSGroupingSeparators() {
        val result = Kurrency.USD.formatAmount("1,234.56")

        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("1") && formatted.contains("234"))
    }

    @Test
    fun testFormatAmountWithLargeUSGroupedNumber() {
        val result = Kurrency.USD.formatAmount("1,234,567.89")

        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("1") && formatted.contains("234"))
    }

    @Test
    fun testFormatAmountWithEuropeanGroupingSeparators() {
        val result = Kurrency.EUR.formatAmount("1.234,56")

        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("1") && formatted.contains("234"))
    }

    @Test
    fun testFormatAmountWithSpaceGroupingSeparators() {
        val result = Kurrency.EUR.formatAmount("1 234 567,89")

        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("1") && formatted.contains("234"))
    }

    @Test
    fun testFormatAmountWithIndianGrouping() {
        val result = Kurrency.INR.formatAmount("12,34,567.89")

        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("1") && formatted.contains("234"))
    }

    @Test
    fun testPropertyDelegation() {
        val currency = Kurrency.USD
        val delegate = currency.format("100.50")
        assertNotNull(delegate)
        val formatted: String by delegate
        assertTrue(formatted.isNotBlank())
    }

    @Test
    fun testPropertyDelegationWithStyle() {
        val currency = Kurrency.USD
        val delegate = currency.format("100.50", CurrencyStyle.Iso)
        assertNotNull(delegate)
        val formatted: String by delegate
        assertTrue(formatted.isNotBlank())
    }

    @Test
    fun testEuroFormatting() {
        val currency = Kurrency.EUR
        val result = currency.formatAmount("1234.56")

        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("1") && formatted.contains("234"))
    }

    @Test
    fun testBritishPoundFormatting() {
        val currency = Kurrency.GBP
        val result = currency.formatAmount("1234.56")

        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("1") && formatted.contains("234"))
    }

    @Test
    fun testJapaneseYenFormatting() {
        val currency = Kurrency.JPY
        val result = currency.formatAmount("1234")

        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("1") && formatted.contains("234"))
    }

    @Test
    fun testSwissFrancFormatting() {
        val currency = Kurrency.CHF
        val result = currency.formatAmount("1234.56")

        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("1") && formatted.contains("234"))
    }

    @Test
    fun testCurrencyEquality() {
        val currency1 = Kurrency.USD
        val currency2 = Kurrency.USD

        assertEquals(currency1, currency2)
        assertTrue(currency1 == currency2)
    }

    @Test
    fun testCurrencyInequality() {
        val currency1 = Kurrency.USD
        val currency2 = Kurrency.EUR

        assertFalse(currency1 == currency2)
        assertTrue(currency1 != currency2)
    }

    @Test
    fun testCurrencyEqualityWithDifferentFractionDigits() {
        val currency1 = Kurrency.USD
        val currency2 = Kurrency.USD

        assertEquals(currency1, currency2)
        assertTrue(currency1 == currency2)
    }

    @Test
    fun testCurrencyHashCode() {
        val currency1 = Kurrency.USD
        val currency2 = Kurrency.USD

        assertEquals(currency1.hashCode(), currency2.hashCode())
    }

    @Test
    fun testCurrencyHashCodeDifferentCodes() {
        val currency1 = Kurrency.USD
        val currency2 = Kurrency.EUR

        assertTrue(currency1.hashCode() != currency2.hashCode())
    }

    @Test
    fun testIsValidWithValidCurrency() {
        assertTrue(Kurrency.isValid("USD"))
        assertTrue(Kurrency.isValid("EUR"))
        assertTrue(Kurrency.isValid("GBP"))
        assertTrue(Kurrency.isValid("JPY"))
    }

    @Test
    fun testIsValidWithInvalidFormat() {
        assertFalse(Kurrency.isValid("US"))
        assertFalse(Kurrency.isValid("USDD"))
        assertFalse(Kurrency.isValid("123"))
        assertFalse(Kurrency.isValid("US$"))
    }

    @Test
    fun testIsValidWithInvalidCurrencyCode() {
        assertFalse(Kurrency.isValid("INVALID"))
        assertFalse(Kurrency.isValid("XYZ"))
    }

    @Test
    fun testIsValidWithEmptyString() {
        assertFalse(Kurrency.isValid(""))
    }

    @Test
    fun testIsValidWithLowercase() {
        assertTrue(Kurrency.isValid("usd"))
        assertTrue(Kurrency.isValid("eur"))
        assertTrue(Kurrency.isValid("jpy"))
    }

    @Test
    fun testConvenienceCurrencyProperties() {
        assertEquals("USD", Kurrency.USD.code)
        assertEquals("EUR", Kurrency.EUR.code)
        assertEquals("GBP", Kurrency.GBP.code)
        assertEquals("JPY", Kurrency.JPY.code)
        assertEquals("AUD", Kurrency.AUD.code)
        assertEquals("CAD", Kurrency.CAD.code)
        assertEquals("CHF", Kurrency.CHF.code)
        assertEquals("CNY", Kurrency.CNY.code)
        assertEquals("INR", Kurrency.INR.code)
        assertEquals("KRW", Kurrency.KRW.code)
        assertEquals("MXN", Kurrency.MXN.code)
        assertEquals("BRL", Kurrency.BRL.code)
        assertEquals("ZAR", Kurrency.ZAR.code)
        assertEquals("NZD", Kurrency.NZD.code)
        assertEquals("SGD", Kurrency.SGD.code)
        assertEquals("HKD", Kurrency.HKD.code)
    }

    @Test
    fun testConvenienceCurrencyFractionDigits() {
        assertEquals(2, Kurrency.USD.fractionDigits.getOrNull())
        assertEquals(2, Kurrency.EUR.fractionDigits.getOrNull())
        assertEquals(2, Kurrency.GBP.fractionDigits.getOrNull())
        assertEquals(0, Kurrency.JPY.fractionDigits.getOrNull())
        assertEquals(2, Kurrency.AUD.fractionDigits.getOrNull())
        assertEquals(2, Kurrency.CAD.fractionDigits.getOrNull())
        assertEquals(2, Kurrency.CHF.fractionDigits.getOrNull())
    }

    @Test
    fun testConvenienceCurrencyEquality() {
        val usd1 = Kurrency.USD
        val usd2 = Kurrency.USD
        assertEquals(usd1, usd2)
        assertEquals(usd1.hashCode(), usd2.hashCode())
    }

    @Test
    fun testAllConvenienceCurrenciesAreValid() {
        val currencies = listOf(
            Kurrency.USD, Kurrency.EUR, Kurrency.GBP, Kurrency.JPY,
            Kurrency.AUD, Kurrency.CAD, Kurrency.CHF, Kurrency.CNY,
            Kurrency.INR, Kurrency.KRW, Kurrency.MXN, Kurrency.BRL,
            Kurrency.ZAR, Kurrency.NZD, Kurrency.SGD, Kurrency.HKD
        )

        currencies.forEach { currency ->
            assertTrue(Kurrency.isValid(currency.code), "${currency.code} should be valid")
        }
    }

    @Test
    fun testConveniencePropertyReturnsSameInstance() {
        assertSame(Kurrency.USD, Kurrency.USD)
    }

    @Test
    fun testDifferentConveniencePropertiesAreCached() {
        assertSame(Kurrency.EUR, Kurrency.EUR)
        assertSame(Kurrency.GBP, Kurrency.GBP)
    }

    @Test
    fun testForLocaleWithSystemLocaleReturnsSameInstance() {
        val f1 = CurrencyFormatter.forLocale()
        val f2 = CurrencyFormatter.forLocale()
        assertSame(f1, f2)
    }

    @Test
    fun testForLocaleWithDefaultParamReturnsSameInstance() {
        val f1 = CurrencyFormatter.forLocale(KurrencyLocale.systemLocale())
        val f2 = CurrencyFormatter.forLocale(KurrencyLocale.systemLocale())
        assertSame(f1, f2)
    }

    @Test
    fun testFormatterRejectsNonexistentCurrencyCode() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyleResult("100", "XYZ")
        assertTrue(result.isFailure)
    }

    @Test
    fun testFormatterStillAcceptsValidCurrencyCodes() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        assertTrue(formatter.formatCurrencyStyleResult("100", "USD").isSuccess)
        assertTrue(formatter.formatCurrencyStyleResult("100", "EUR").isSuccess)
        assertTrue(formatter.formatCurrencyStyleResult("100", "JPY").isSuccess)
    }

    @Test
    fun testCompactFormatReturnsNonEmptyResult() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCompactStyleResult("1234567.89", "USD")
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.isNotBlank())
        assertTrue(formatted.length < 20)
    }

    @Test
    fun testCompactFormatOnKurrency() {
        val result = Kurrency.USD.formatAmountCompact("1000000")
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("1"))
    }

    @Test
    fun testCompactFormatRejectsInvalidCurrency() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCompactStyleResult("1000", "XYZ")
        assertTrue(result.isFailure)
    }

    @Test
    fun testCompactFormatRejectsInvalidAmount() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCompactStyleResult("invalid", "USD")
        assertTrue(result.isFailure)
    }

    @Test
    fun testMinorUnitsFormatting() {
        val result = Kurrency.USD.formatMinorUnits(12345L)
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("123"))
    }

    @Test
    fun testMinorUnitsFormattingZeroFractionCurrency() {
        val result = Kurrency.JPY.formatMinorUnits(1000L)
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("1000") || formatted.contains("1,000"))
    }

    @Test
    fun testCurrencyAmountFormat() {
        val amount = CurrencyAmount.of(12345L, Kurrency.USD)
        assertEquals(12345L, amount.minorUnits)
        assertEquals(Kurrency.USD, amount.currency)
        val formatted = amount.formatOrEmpty()
        assertTrue(formatted.isNotBlank())
        assertTrue(formatted.contains("123"))
    }

    @Test
    fun testCurrencyAmountFromMajorUnits() {
        val result = CurrencyAmount.fromMajorUnits("123.45", Kurrency.USD)
        assertTrue(result.isSuccess)
        assertEquals(12345L, result.getOrNull()?.minorUnits)
    }

    @Test
    fun testAccountingStylePositiveAmount() {
        val result = Kurrency.USD.formatAmount("100.50", CurrencyStyle.Accounting)
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertFalse(formatted.contains("("))
    }

    @Test
    fun testAccountingStyleNegativeAmount() {
        val result = Kurrency.USD.formatAmount("-100.50", CurrencyStyle.Accounting)
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.startsWith("(") && formatted.endsWith(")"))
    }

    @Test
    fun testParseCurrencyAmount() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val formatted = formatter.formatCurrencyStyle("1234.56", "USD")
        val parsed = formatter.parseCurrencyAmountResult(formatted, "USD")
        assertTrue(parsed.isSuccess)
        val value = parsed.getOrNull()
        assertNotNull(value)
        assertTrue(value > 1234.0 && value < 1235.0)
    }

    @Test
    fun testParseCurrencyAmountInvalidText() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.parseCurrencyAmountResult("not a number", "USD")
        assertTrue(result.isFailure)
    }

    @Test
    fun testFormatterEquality() {
        val f1 = CurrencyFormatter(KurrencyLocale.US)
        val f2 = CurrencyFormatter(KurrencyLocale.US)
        assertEquals(f1, f2)
        assertEquals(f1.hashCode(), f2.hashCode())
    }

    @Test
    fun testCurrencyMetadataPluralNames() {
        assertEquals("US Dollars", CurrencyMetadata.USD.displayNamePlural)
        assertEquals("Japanese Yen", CurrencyMetadata.JPY.displayNamePlural)
        assertEquals("Euros", CurrencyMetadata.EUR.displayNamePlural)
        assertEquals("Brazilian Reais", CurrencyMetadata.BRL.displayNamePlural)
    }

    @Test
    fun testFractionDigitsFastFallback() {
        val result = CurrencyFormatter.getFractionDigits("USD")
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull())

        val jpyResult = CurrencyFormatter.getFractionDigits("JPY")
        assertTrue(jpyResult.isSuccess)
        assertEquals(0, jpyResult.getOrNull())
    }

    @Test
    fun testAccountingStyleZeroAmount() {
        val result = Kurrency.USD.formatAmount("0.00", CurrencyStyle.Accounting)
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertFalse(formatted.contains("("))
    }

    @Test
    fun testAccountingStyleLargeNegative() {
        val result = Kurrency.USD.formatAmount("-999999.99", CurrencyStyle.Accounting)
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.startsWith("(") && formatted.endsWith(")"))
        assertFalse(formatted.contains("-"))
    }

    @Test
    fun testCurrencyAmountAccountingStyle() {
        val positiveAmount = CurrencyAmount.of(10050L, Kurrency.USD)
        val positiveFormatted = positiveAmount.format(CurrencyStyle.Accounting)
        assertTrue(positiveFormatted.isSuccess)
        assertFalse(positiveFormatted.getOrNull()!!.contains("("))

        val negativeAmount = CurrencyAmount.of(-10050L, Kurrency.USD)
        val negativeFormatted = negativeAmount.format(CurrencyStyle.Accounting)
        assertTrue(negativeFormatted.isSuccess)
        assertTrue(negativeFormatted.getOrNull()!!.startsWith("("))
    }

    @Test
    fun testParseCurrencyRoundTrip() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val original = 1234.56
        val formatted = formatter.formatCurrencyStyle(original.toString(), "USD")
        val parsed = formatter.parseCurrencyAmountResult(formatted, "USD")
        assertTrue(parsed.isSuccess)
        assertEquals(original, parsed.getOrNull()!!, 0.01)
    }

    @Test
    fun testParseCurrencyAmountInvalidCurrency() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.parseCurrencyAmountResult("100", "XYZ")
        assertTrue(result.isFailure)
    }

    @Test
    fun testMinorUnitsUSD() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatMinorUnitsResult(12345L, "USD")
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("123"))
    }

    @Test
    fun testMinorUnitsJPY() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatMinorUnitsResult(1000L, "JPY")
        assertTrue(result.isSuccess)
    }

    @Test
    fun testMinorUnitsInvalidCurrency() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatMinorUnitsResult(1000L, "XYZ")
        assertTrue(result.isFailure)
    }

    @Test
    fun testMinorUnitsZero() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatMinorUnitsResult(0L, "USD")
        assertTrue(result.isSuccess)
    }

    @Test
    fun testMinorUnitsNegative() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatMinorUnitsResult(-500L, "USD")
        assertTrue(result.isSuccess)
    }

    @Test
    fun testCurrencyAmountOf() {
        val amount = CurrencyAmount.of(12345L, Kurrency.USD)
        assertEquals(12345L, amount.minorUnits)
        assertEquals(Kurrency.USD, amount.currency)
    }

    @Test
    fun testCurrencyAmountFromMajorUnitsDouble() {
        val result = CurrencyAmount.fromMajorUnits(123.45, Kurrency.USD)
        assertTrue(result.isSuccess)
        assertEquals(12345L, result.getOrNull()?.minorUnits)
    }

    @Test
    fun testCurrencyAmountFromMajorUnitsInvalid() {
        val result = CurrencyAmount.fromMajorUnits("abc", Kurrency.USD)
        assertTrue(result.isFailure)
    }

    @Test
    fun testCurrencyAmountFormatStandard() {
        val amount = CurrencyAmount.of(10050L, Kurrency.USD)
        val result = amount.format(CurrencyStyle.Standard)
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("100"))
    }

    @Test
    fun testCurrencyAmountFormatIso() {
        val amount = CurrencyAmount.of(10050L, Kurrency.USD)
        val result = amount.format(CurrencyStyle.Iso)
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("USD"))
    }

    @Test
    fun testCurrencyAmountEquality() {
        val a1 = CurrencyAmount.of(100L, Kurrency.USD)
        val a2 = CurrencyAmount.of(100L, Kurrency.USD)
        val a3 = CurrencyAmount.of(200L, Kurrency.USD)
        val a4 = CurrencyAmount.of(100L, Kurrency.EUR)
        assertEquals(a1, a2)
        assertNotEquals(a1, a3)
        assertNotEquals(a1, a4)
    }

    @Test
    fun testCompactFormatSmallAmount() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCompactStyleResult("500", "USD")
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("5"))
    }

    @Test
    fun testCompactFormatNegativeAmount() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCompactStyleResult("-1000000", "USD")
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("1"))
    }

    @Test
    fun testCompactFormatViaKurrency() {
        val result = Kurrency.USD.formatAmountCompact(1000000.0)
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("1"))
    }

    @Test
    fun testFormatterEqualityDifferentLocales() {
        val us = CurrencyFormatter(KurrencyLocale.US)
        val de = CurrencyFormatter(KurrencyLocale.GERMANY)
        assertNotEquals(us, de)
    }

    @Test
    fun testForLocaleNonSystemLocale() {
        val formatter = CurrencyFormatter.forLocale(KurrencyLocale.GERMANY)
        assertNotNull(formatter)
        val result = formatter.formatCurrencyStyleResult("100", "EUR")
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("100"))
    }

    @Test
    fun testMetadataPluralIrregularForms() {
        assertEquals("Japanese Yen", CurrencyMetadata.JPY.displayNamePlural)
        assertEquals("Chinese Yuan", CurrencyMetadata.CNY.displayNamePlural)
        assertEquals("South African Rand", CurrencyMetadata.ZAR.displayNamePlural)
        assertEquals("South Korean Won", CurrencyMetadata.KRW.displayNamePlural)
        assertEquals("Thai Baht", CurrencyMetadata.THB.displayNamePlural)
        assertEquals("Vietnamese Dong", CurrencyMetadata.VND.displayNamePlural)
        assertEquals("Bangladeshi Taka", CurrencyMetadata.BDT.displayNamePlural)
        assertEquals("Nigerian Naira", CurrencyMetadata.NGN.displayNamePlural)
        assertEquals("Swedish Kronor", CurrencyMetadata.SEK.displayNamePlural)
        assertEquals("Romanian Lei", CurrencyMetadata.RON.displayNamePlural)
        assertEquals("Bulgarian Leva", CurrencyMetadata.BGN.displayNamePlural)
        assertEquals("Brazilian Reais", CurrencyMetadata.BRL.displayNamePlural)
        assertEquals("Peruvian Soles", CurrencyMetadata.PEN.displayNamePlural)
    }
}
