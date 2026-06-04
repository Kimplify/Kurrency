package org.kimplify.kurrency
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class CurrencyMetadataTestInstrumented {

    @Test
    fun testParseValidCurrencyCodeUppercase() {
        val result = CurrencyMetadata.parse("USD")
        
        assertTrue(result.isSuccess)
        val metadata = result.getOrNull()
        assertNotNull(metadata)
        assertEquals("USD", metadata.code)
        assertEquals("US Dollar", metadata.displayName)
        assertEquals("$", metadata.symbol)
        assertEquals("US", metadata.countryIso)
        assertEquals(2, metadata.fractionDigits)
    }

    @Test
    fun testParseValidCurrencyCodeLowercase() {
        val result = CurrencyMetadata.parse("usd")
        
        assertTrue(result.isSuccess)
        val metadata = result.getOrNull()
        assertNotNull(metadata)
        assertEquals("USD", metadata.code)
    }

    @Test
    fun testParseValidCurrencyCodeMixedCase() {
        val result = CurrencyMetadata.parse("EuR")
        
        assertTrue(result.isSuccess)
        val metadata = result.getOrNull()
        assertNotNull(metadata)
        assertEquals("EUR", metadata.code)
        assertEquals("Euro", metadata.displayName)
    }

    @Test
    fun testParseInvalidCurrencyCode() {
        val result = CurrencyMetadata.parse("XXX")
        
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is KurrencyError.InvalidCurrencyCode)
        assertEquals("XXX", (exception as KurrencyError.InvalidCurrencyCode).code)
    }

    @Test
    fun testParseEmptyString() {
        val result = CurrencyMetadata.parse("")
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is KurrencyError.InvalidCurrencyCode)
    }

    @Test
    fun testParseBlankString() {
        val result = CurrencyMetadata.parse("   ")
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is KurrencyError.InvalidCurrencyCode)
    }

    @Test
    fun testParseShortCode() {
        val result = CurrencyMetadata.parse("US")
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is KurrencyError.InvalidCurrencyCode)
    }

    @Test
    fun testParseLongCode() {
        val result = CurrencyMetadata.parse("USDD")
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is KurrencyError.InvalidCurrencyCode)
    }

    @Test
    fun testGetAllReturnsAllCurrencies() {
        val allCurrencies = CurrencyMetadata.getAll()
        
        assertEquals(50, allCurrencies.size)
        assertTrue(allCurrencies.contains(CurrencyMetadata.USD))
        assertTrue(allCurrencies.contains(CurrencyMetadata.EUR))
        assertTrue(allCurrencies.contains(CurrencyMetadata.GBP))
        assertTrue(allCurrencies.contains(CurrencyMetadata.JPY))
        assertTrue(allCurrencies.contains(CurrencyMetadata.KRW))
        assertTrue(allCurrencies.contains(CurrencyMetadata.TWD))
        assertTrue(allCurrencies.contains(CurrencyMetadata.KWD))
    }

    @Test
    fun testAllCurrenciesHaveValidData() {
        val allCurrencies = CurrencyMetadata.getAll()
        
        allCurrencies.forEach { currency ->
            assertEquals(3, currency.code.length)
            assertTrue(currency.code.all { it.isLetter() })
            assertFalse(currency.displayName.isBlank())
            assertFalse(currency.symbol.isBlank())
            assertEquals(2, currency.countryIso.length)
            assertTrue(currency.fractionDigits >= 0)
            assertFalse(currency.flag.isBlank())
        }
    }

    @Test
    fun testUsdMetadata() {
        assertEquals("USD", CurrencyMetadata.USD.code)
        assertEquals("US Dollar", CurrencyMetadata.USD.displayName)
        assertEquals("$", CurrencyMetadata.USD.symbol)
        assertEquals("US", CurrencyMetadata.USD.countryIso)
        assertEquals("🇺🇸", CurrencyMetadata.USD.flag)
        assertEquals(2, CurrencyMetadata.USD.fractionDigits)
    }

    @Test
    fun testEurMetadata() {
        assertEquals("EUR", CurrencyMetadata.EUR.code)
        assertEquals("Euro", CurrencyMetadata.EUR.displayName)
        assertEquals("€", CurrencyMetadata.EUR.symbol)
        assertEquals("EU", CurrencyMetadata.EUR.countryIso)
        assertEquals("🇪🇺", CurrencyMetadata.EUR.flag)
        assertEquals(2, CurrencyMetadata.EUR.fractionDigits)
    }

    @Test
    fun testJpyMetadata() {
        assertEquals("JPY", CurrencyMetadata.JPY.code)
        assertEquals("Japanese Yen", CurrencyMetadata.JPY.displayName)
        assertEquals("¥", CurrencyMetadata.JPY.symbol)
        assertEquals("JP", CurrencyMetadata.JPY.countryIso)
        assertEquals("🇯🇵", CurrencyMetadata.JPY.flag)
        assertEquals(0, CurrencyMetadata.JPY.fractionDigits)
    }

    @Test
    fun testClpMetadata() {
        assertEquals("CLP", CurrencyMetadata.CLP.code)
        assertEquals(0, CurrencyMetadata.CLP.fractionDigits)
    }

    @Test
    fun testParseWithWhitespace() {
        val result = CurrencyMetadata.parse("  USD  ")
        
        assertTrue(result.isSuccess)
        val metadata = result.getOrNull()
        assertNotNull(metadata)
        assertEquals("USD", metadata.code)
    }

    @Test
    fun testAllCurrenciesHaveUniqueCodes() {
        val allCurrencies = CurrencyMetadata.getAll()
        val codes = allCurrencies.map { it.code }
        val uniqueCodes = codes.toSet()
        
        assertEquals(codes.size, uniqueCodes.size)
    }

    @Test
    fun testParseMultipleCurrencies() {
        val testCodes = listOf("USD", "EUR", "GBP", "JPY", "CNY", "AUD", "CAD", "CHF")
        
        testCodes.forEach { code ->
            val result = CurrencyMetadata.parse(code)
            assertTrue(result.isSuccess, "Failed to parse $code")
            val metadata = result.getOrNull()
            assertNotNull(metadata)
            assertEquals(code.uppercase(), metadata.code)
        }
    }

    @Test
    fun testKrwMetadata() {
        assertEquals("KRW", CurrencyMetadata.KRW.code)
        assertEquals("South Korean Won", CurrencyMetadata.KRW.displayName)
        assertEquals("₩", CurrencyMetadata.KRW.symbol)
        assertEquals("KR", CurrencyMetadata.KRW.countryIso)
        assertEquals("🇰🇷", CurrencyMetadata.KRW.flag)
        assertEquals(0, CurrencyMetadata.KRW.fractionDigits)
    }

    @Test
    fun testKwdMetadata() {
        assertEquals("KWD", CurrencyMetadata.KWD.code)
        assertEquals("Kuwaiti Dinar", CurrencyMetadata.KWD.displayName)
        assertEquals("د.ك", CurrencyMetadata.KWD.symbol)
        assertEquals("KW", CurrencyMetadata.KWD.countryIso)
        assertEquals("🇰🇼", CurrencyMetadata.KWD.flag)
        assertEquals(3, CurrencyMetadata.KWD.fractionDigits)
    }

    @Test
    fun testVndMetadata() {
        assertEquals("VND", CurrencyMetadata.VND.code)
        assertEquals("Vietnamese Dong", CurrencyMetadata.VND.displayName)
        assertEquals("₫", CurrencyMetadata.VND.symbol)
        assertEquals(0, CurrencyMetadata.VND.fractionDigits)
    }

    @Test
    fun testParseNewCurrencies() {
        val newCurrencies = listOf("KRW", "TWD", "VND", "ARS", "COP", "UAH", "PKR", "NGN", "KES", "QAR", "KWD", "OMR")
        
        newCurrencies.forEach { code ->
            val result = CurrencyMetadata.parse(code)
            assertTrue(result.isSuccess, "Failed to parse $code")
            val metadata = result.getOrNull()
            assertNotNull(metadata)
            assertEquals(code.uppercase(), metadata.code)
        }
    }
}

