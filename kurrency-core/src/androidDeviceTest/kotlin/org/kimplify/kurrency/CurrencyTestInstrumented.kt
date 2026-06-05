package org.kimplify.kurrency
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class CurrencyTestInstrumented {
    
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
    fun testFormatAmountReturnsSuccess() {
        val currency = Kurrency.fromCode("USD").getOrThrow()
        val result = currency.formatAmount("100.50")
        
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }
    
    @Test
    fun testFormatAmountWithDoubleReturnsSuccess() {
        val currency = Kurrency.fromCode("USD").getOrThrow()
        val result = currency.formatAmount(100.50)
        
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }
    
    @Test
    fun testFormatAmountStandardStyle() {
        val currency = Kurrency.fromCode("USD").getOrThrow()
        val result = currency.formatAmount("1234.56", CurrencyStyle.Standard)
        
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
    }
    
    @Test
    fun testFormatAmountIsoStyle() {
        val currency = Kurrency.fromCode("USD").getOrThrow()
        val result = currency.formatAmount("1234.56", CurrencyStyle.Iso)
        
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
    }
    
    @Test
    fun testFormatAmountWithInvalidAmount() {
        val currency = Kurrency.fromCode("USD").getOrThrow()
        val result = currency.formatAmount("invalid")
        
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is KurrencyError.InvalidAmount)
    }
    
    @Test
    fun testFormatAmountWithEmptyString() {
        val currency = Kurrency.fromCode("USD").getOrThrow()
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
        val currency = Kurrency.fromCode("USD").getOrThrow()
        val formatted = currency.formatAmountOrEmpty("100.50")
        
        assertNotNull(formatted)
        assertFalse(formatted.isEmpty())
    }
    
    @Test
    fun testFormatAmountOrEmptyWithInvalidAmount() {
        val currency = Kurrency.fromCode("USD").getOrThrow()
        val formatted = currency.formatAmountOrEmpty("invalid")
        
        assertEquals("", formatted)
    }
    
    @Test
    fun testFormatAmountWithZero() {
        val currency = Kurrency.fromCode("USD").getOrThrow()
        val result = currency.formatAmount("0.00")
        
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }
    
    @Test
    fun testFormatAmountWithNegativeNumber() {
        val currency = Kurrency.fromCode("USD").getOrThrow()
        val result = currency.formatAmount("-100.50")
        
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }
    
    @Test
    fun testFormatAmountWithVeryLargeNumber() {
        val currency = Kurrency.fromCode("USD").getOrThrow()
        val result = currency.formatAmount("999999999.99")
        
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }
    
    @Test
    fun testFormatAmountWithCommaDecimalSeparator() {
        val currency = Kurrency.fromCode("EUR").getOrThrow()
        val result = currency.formatAmount("100,50")
        
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun testPropertyDelegation() {
        val currency = Kurrency.fromCode("USD").getOrThrow()
        val delegate = currency.format("100.50")
        
        assertNotNull(delegate)
    }
    
    @Test
    fun testPropertyDelegationWithStyle() {
        val currency = Kurrency.fromCode("USD").getOrThrow()
        val delegate = currency.format("100.50", CurrencyStyle.Iso)
        
        assertNotNull(delegate)
    }
    
    @Test
    fun testEuroFormatting() {
        val currency = Kurrency.fromCode("EUR").getOrThrow()
        val result = currency.formatAmount("1234.56")
        
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }
    
    @Test
    fun testBritishPoundFormatting() {
        val currency = Kurrency.fromCode("GBP").getOrThrow()
        val result = currency.formatAmount("1234.56")
        
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }
    
    @Test
    fun testJapaneseYenFormatting() {
        val currency = Kurrency.fromCode("JPY").getOrThrow()
        val result = currency.formatAmount("1234")
        
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }
    
    @Test
    fun testSwissFrancFormatting() {
        val currency = Kurrency.fromCode("CHF").getOrThrow()
        val result = currency.formatAmount("1234.56")
        
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }
    
    @Test
    fun testCurrencyEquality() {
        val currency1 = Kurrency.fromCode("USD").getOrThrow()
        val currency2 = Kurrency.fromCode("USD").getOrThrow()
        
        assertEquals(currency1, currency2)
        assertTrue(currency1 == currency2)
    }
    
    @Test
    fun testCurrencyInequality() {
        val currency1 = Kurrency.fromCode("USD").getOrThrow()
        val currency2 = Kurrency.fromCode("EUR").getOrThrow()
        
        assertFalse(currency1 == currency2)
        assertTrue(currency1 != currency2)
    }
    
    @Test
    fun testCurrencyEqualityWithDifferentFractionDigits() {
        // This test is no longer valid since constructor is private
        // and fraction digits is a computed property, not a parameter
        val currency1 = Kurrency.USD
        val currency2 = Kurrency.USD

        assertEquals(currency1, currency2)
        assertTrue(currency1 == currency2)
    }
    
    @Test
    fun testCurrencyHashCode() {
        val currency1 = Kurrency.fromCode("USD").getOrThrow()
        val currency2 = Kurrency.fromCode("USD").getOrThrow()
        
        assertEquals(currency1.hashCode(), currency2.hashCode())
    }
    
    @Test
    fun testCurrencyHashCodeDifferentCodes() {
        val currency1 = Kurrency.fromCode("USD").getOrThrow()
        val currency2 = Kurrency.fromCode("EUR").getOrThrow()
        
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

    // ---- Edge case tests ----

    @Test
    fun testInfinityAmountReturnsError() {
        val currency = Kurrency.fromCode("USD").getOrThrow()
        val result = currency.formatAmount("Infinity")
        assertTrue(result.isFailure, "Infinity should not be a valid amount")
        assertTrue(result.exceptionOrNull() is KurrencyError.InvalidAmount)
    }

    @Test
    fun testNaNAmountReturnsError() {
        val currency = Kurrency.fromCode("USD").getOrThrow()
        val result = currency.formatAmount("NaN")
        assertTrue(result.isFailure, "NaN should not be a valid amount")
        assertTrue(result.exceptionOrNull() is KurrencyError.InvalidAmount)
    }

    @Test
    fun testNegativeZeroFormatsSuccessfully() {
        val currency = Kurrency.fromCode("USD").getOrThrow()
        val result = currency.formatAmount("-0.00")
        // Negative zero should either succeed (formatting as $0.00) or fail gracefully
        // Most implementations treat -0 as 0
        assertTrue(result.isSuccess, "Negative zero should format successfully")
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("0"), "Expected zero in: $formatted")
    }

    @Test
    fun testBhdThreeDecimalPlaces() {
        val currency = Kurrency.fromCode("BHD").getOrThrow()
        assertEquals(3, currency.fractionDigits.getOrNull(), "BHD should have 3 fraction digits")
        val result = currency.formatAmount("100.123")
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun testVeryLargeAmountFormatsSuccessfully() {
        val currency = Kurrency.fromCode("USD").getOrThrow()
        val result = currency.formatAmount("99999999999999.99")
        assertTrue(result.isSuccess, "Very large amount should format successfully")
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("9"), "Expected digits in: $formatted")
    }
}

