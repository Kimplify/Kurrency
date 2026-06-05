package org.kimplify.kurrency
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class KurrencyErrorTestInstrumented {
    
    @Test
    fun testInvalidCurrencyCodeError() {
        val error = KurrencyError.InvalidCurrencyCode("XYZ")
        
        assertEquals("XYZ", error.code)
        assertEquals("Invalid currency code: XYZ", error.errorMessage)
        assertNotNull(error.message)
    }
    
    @Test
    fun testInvalidAmountError() {
        val error = KurrencyError.InvalidAmount("abc")
        
        assertEquals("abc", error.amount)
        assertEquals("Invalid amount: abc", error.errorMessage)
        assertNotNull(error.message)
    }
    
    @Test
    fun testFormattingFailureError() {
        val cause = RuntimeException("Test exception")
        val error = KurrencyError.FormattingFailure("USD", "100.00", cause)
        
        assertEquals("USD", error.currencyCode)
        assertEquals("100.00", error.amount)
        assertEquals("Formatting failed for USD: 100.00", error.errorMessage)
        assertEquals(cause, error.cause)
        assertNotNull(error.message)
    }
    
    @Test
    fun testFractionDigitsFailureError() {
        val cause = RuntimeException("Test exception")
        val error = KurrencyError.FractionDigitsFailure("USD", cause)
        
        assertEquals("USD", error.currencyCode)
        assertEquals("Failed to get fraction digits for USD", error.errorMessage)
        assertEquals(cause, error.cause)
        assertNotNull(error.message)
    }
    
    @Test
    fun testErrorIsException() {
        val error = KurrencyError.InvalidCurrencyCode("XYZ")
        
        assertTrue(error is Exception)
        assertTrue(error is Throwable)
    }
    
    @Test
    fun testErrorCanBeCaught() {
        var caught = false
        
        try {
            throw KurrencyError.InvalidAmount("test")
        } catch (e: KurrencyError) {
            caught = true
        }
        
        assertTrue(caught)
    }
    
    @Test
    fun testInvalidCurrencyCodeInResult() {
        val fromCodeResult = Kurrency.fromCode("INVALID")
        assertTrue(fromCodeResult.isFailure)
        val error = fromCodeResult.exceptionOrNull()
        assertTrue(error is KurrencyError.InvalidCurrencyCode)
        assertEquals("INVALID", (error as KurrencyError.InvalidCurrencyCode).code)
    }

    @Test
    fun testInvalidAmountInResult() {
        val currency = Kurrency.USD
        val result = currency.formatAmount("invalid")

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is KurrencyError.InvalidAmount)
        assertEquals("invalid", (error as KurrencyError.InvalidAmount).amount)
    }
    
    @Test
    fun testErrorMessageIsAccessible() {
        val error = KurrencyError.InvalidCurrencyCode("TEST")
        
        assertNotNull(error.errorMessage)
        assertTrue(error.errorMessage.contains("TEST"))
    }
    
    @Test
    fun testErrorWithEmptyValues() {
        val error1 = KurrencyError.InvalidCurrencyCode("")
        assertEquals("", error1.code)
        assertTrue(error1.errorMessage.contains("Invalid currency code"))
        
        val error2 = KurrencyError.InvalidAmount("")
        assertEquals("", error2.amount)
        assertTrue(error2.errorMessage.contains("Invalid amount"))
    }
    
    @Test
    fun testErrorMessagesAreDifferent() {
        val error1 = KurrencyError.InvalidCurrencyCode("USD")
        val error2 = KurrencyError.InvalidAmount("100")
        val error3 = KurrencyError.FormattingFailure("EUR", "200", RuntimeException())
        val error4 = KurrencyError.FractionDigitsFailure("GBP", RuntimeException())
        
        val messages = setOf(
            error1.errorMessage,
            error2.errorMessage,
            error3.errorMessage,
            error4.errorMessage
        )
        
        assertEquals(4, messages.size)
    }
}

