@file:OptIn(ExperimentalKurrency::class)

package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CurrencyStateTest {
    
    @Test
    fun testCurrencyStateCreation() {
        val state = CurrencyState("USD", "100.00")
        
        assertEquals("USD", state.currency.code)
        assertEquals("100.00", state.amount)
    }
    
    @Test
    fun testCurrencyStateDefaultAmount() {
        val state = CurrencyState("USD")
        
        assertEquals("USD", state.currency.code)
        assertEquals("0.00", state.amount)
    }
    
    @Test
    fun testFormattedAmount() {
        val state = CurrencyState("USD", "1234.56")
        val formatted = state.formattedAmount
        
        assertNotNull(formatted)
        assertTrue(formatted.isNotEmpty())
    }
    
    @Test
    fun testFormattedAmountIso() {
        val state = CurrencyState("USD", "1234.56")
        val formatted = state.formattedAmountIso
        
        assertNotNull(formatted)
        assertTrue(formatted.isNotEmpty())
    }
    
    @Test
    fun testFormattedAmountResult() {
        val state = CurrencyState("USD", "1234.56")
        val result = state.formattedAmountResult
        
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }
    
    @Test
    fun testFormattedAmountIsoResult() {
        val state = CurrencyState("USD", "1234.56")
        val result = state.formattedAmountIsoResult
        
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }
    
    @Test
    fun testUpdateCurrency() {
        val state = CurrencyState("USD", "100.00")
        val originalCurrency = state.currency
        
        state.updateCurrency("EUR")
        
        assertEquals("EUR", state.currency.code)
        assertNotEquals(originalCurrency, state.currency)
        assertEquals("100.00", state.amount)
    }
    
    @Test
    fun testUpdateAmount() {
        val state = CurrencyState("USD", "100.00")
        
        state.updateAmount("250.50")
        
        assertEquals("USD", state.currency.code)
        assertEquals("250.50", state.amount)
    }
    
    @Test
    fun testUpdateCurrencyAndAmount() {
        val state = CurrencyState("USD", "100.00")
        
        state.updateCurrencyAndAmount("EUR", "500.00")
        
        assertEquals("EUR", state.currency.code)
        assertEquals("500.00", state.amount)
    }
    
    @Test
    fun testFormattedAmountAfterUpdate() {
        val state = CurrencyState("USD", "100.00")
        val beforeUpdate = state.formattedAmount
        
        state.updateAmount("200.00")
        val afterUpdate = state.formattedAmount
        
        assertNotEquals(beforeUpdate, afterUpdate)
    }
    
    @Test
    fun testFormattedAmountAfterCurrencyUpdate() {
        val state = CurrencyState("USD", "100.00")
        val beforeUpdate = state.formattedAmount
        
        state.updateCurrency("EUR")
        val afterUpdate = state.formattedAmount
        
        assertNotEquals(beforeUpdate, afterUpdate)
    }
    
    @Test
    fun testStateWithInvalidAmount() {
        val state = CurrencyState("USD", "invalid")
        val formatted = state.formattedAmount
        
        assertEquals("", formatted)
    }
    
    @Test
    fun testStateWithInvalidAmountResult() {
        val state = CurrencyState("USD", "invalid")
        val result = state.formattedAmountResult
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is KurrencyError.InvalidAmount)
    }
    
    @Test
    fun testStateWithInvalidCurrency() {
        val state = CurrencyState("INVALID", "100.00")
        val formatted = state.formattedAmount
        
        assertEquals("USD", state.currency.code)
        assertTrue(formatted.isNotEmpty())
    }
    
    @Test
    fun testStateWithZeroAmount() {
        val state = CurrencyState("USD", "0.00")
        val formatted = state.formattedAmount
        
        assertNotNull(formatted)
        assertTrue(formatted.isNotEmpty())
    }
    
    @Test
    fun testStateWithNegativeAmount() {
        val state = CurrencyState("USD", "-100.50")
        val formatted = state.formattedAmount
        
        assertNotNull(formatted)
        assertTrue(formatted.isNotEmpty())
    }
    
    @Test
    fun testMultipleSequentialUpdates() {
        val state = CurrencyState("USD", "100.00")
        
        state.updateAmount("200.00")
        assertEquals("200.00", state.amount)
        
        state.updateAmount("300.00")
        assertEquals("300.00", state.amount)
        
        state.updateCurrency("EUR")
        assertEquals("EUR", state.currency.code)
    }
    
    @Test
    fun testFormattedAmountDelegateCreation() {
        val state = CurrencyState("USD", "100.00")
        val delegate = state.formattedAmount()
        
        assertNotNull(delegate)
    }
    
    @Test
    fun testFormattedAmountDelegateWithStyle() {
        val state = CurrencyState("USD", "100.00")
        val delegate = state.formattedAmount(CurrencyStyle.Iso)
        
        assertNotNull(delegate)
    }
    
    @Test
    fun testStateWithDifferentCurrencies() {
        val currencies = listOf("USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD")
        
        currencies.forEach { code ->
            val state = CurrencyState(code, "100.00")
            val formatted = state.formattedAmount
            assertNotNull(formatted, "Failed for currency: $code")
        }
    }
    
    @Test
    fun testStateImmutabilityAfterCreation() {
        val state1 = CurrencyState("USD", "100.00")
        val state2 = CurrencyState("USD", "100.00")
        
        assertEquals(state1.currency.code, state2.currency.code)
        assertEquals(state1.amount, state2.amount)
    }
}
