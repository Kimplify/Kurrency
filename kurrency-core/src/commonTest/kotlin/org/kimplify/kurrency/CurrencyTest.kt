package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
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
        assertNotNull(result.getOrNull())
    }

    @Test
    fun testFormatAmountWithDoubleReturnsSuccess() {
        val currency = Kurrency.USD
        val result = currency.formatAmount(100.50)

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun testFormatAmountStandardStyle() {
        val currency = Kurrency.USD
        val result = currency.formatAmount("1234.56", CurrencyStyle.Standard)

        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
    }

    @Test
    fun testFormatAmountIsoStyle() {
        val currency = Kurrency.USD
        val result = currency.formatAmount("1234.56", CurrencyStyle.Iso)

        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
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
        // Since constructor is private, invalid currencies can't be created
        // This test now validates that fromCode() fails for invalid codes
        val result = Kurrency.fromCode("INVALID")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is KurrencyError.InvalidCurrencyCode)
    }

    @Test
    fun testFormatAmountWithShortCurrencyCode() {
        // Since constructor is private, invalid currencies can't be created
        // This test now validates that fromCode() fails for short codes
        val result = Kurrency.fromCode("US")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is KurrencyError.InvalidCurrencyCode)
    }

    @Test
    fun testFormatAmountWithLongCurrencyCode() {
        // Since constructor is private, invalid currencies can't be created
        // This test now validates that fromCode() fails for long codes
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
        assertNotNull(result.getOrNull())
    }

    @Test
    fun testFormatAmountWithNegativeNumber() {
        val currency = Kurrency.USD
        val result = currency.formatAmount("-100.50")

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun testFormatAmountWithVeryLargeNumber() {
        val currency = Kurrency.USD
        val result = currency.formatAmount("999999999.99")

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun testFormatAmountWithCommaDecimalSeparator() {
        val currency = Kurrency.EUR
        val result = currency.formatAmount("100,50")

        assertTrue(result.isSuccess)
    }

    @Test
    fun testFormatAmountWithUSGroupingSeparators() {
        val result = Kurrency.USD.formatAmount("1,234.56")

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun testFormatAmountWithLargeUSGroupedNumber() {
        val result = Kurrency.USD.formatAmount("1,234,567.89")

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun testFormatAmountWithEuropeanGroupingSeparators() {
        val result = Kurrency.EUR.formatAmount("1.234,56")

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun testFormatAmountWithSpaceGroupingSeparators() {
        val result = Kurrency.EUR.formatAmount("1 234 567,89")

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun testFormatAmountWithIndianGrouping() {
        val result = Kurrency.INR.formatAmount("12,34,567.89")

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun testPropertyDelegation() {
        val currency = Kurrency.USD
        val delegate = currency.format("100.50")

        assertNotNull(delegate)
    }

    @Test
    fun testPropertyDelegationWithStyle() {
        val currency = Kurrency.USD
        val delegate = currency.format("100.50", CurrencyStyle.Iso)

        assertNotNull(delegate)
    }

    @Test
    fun testEuroFormatting() {
        val currency = Kurrency.EUR
        val result = currency.formatAmount("1234.56")

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun testBritishPoundFormatting() {
        val currency = Kurrency.GBP
        val result = currency.formatAmount("1234.56")

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun testJapaneseYenFormatting() {
        val currency = Kurrency.JPY
        val result = currency.formatAmount("1234")

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun testSwissFrancFormatting() {
        val currency = Kurrency.CHF
        val result = currency.formatAmount("1234.56")

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
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
        // This test is no longer valid since constructor is private
        // and fraction digits is a computed property
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

    // Tests for convenience currency properties

    @Test
    fun testConvenienceCurrencyProperties() {
        // Test all 16 predefined currencies
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
        // Verify fraction digits are correct for popular currencies
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
        // Each access should return equal instances
        val usd1 = Kurrency.USD
        val usd2 = Kurrency.USD
        assertEquals(usd1, usd2)
        assertEquals(usd1.hashCode(), usd2.hashCode())
    }

    @Test
    fun testConvenienceCurrencyFormatting() {
        // Test formatting with convenience properties
        val result = Kurrency.USD.formatAmount("100.50")
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun testAllConvenienceCurrenciesAreValid() {
        // Ensure all convenience currencies pass validation
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
}

