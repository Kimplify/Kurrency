package org.kimplify.kurrency.serialization

import kotlinx.serialization.SerializationException
import org.kimplify.kurrency.CurrencyAmount
import org.kimplify.kurrency.Kurrency
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CurrencyAmountSerializerTest {

    private val json = KurrencyJson

    @Test
    fun roundTripUsdAmount() {
        val original = CurrencyAmount.of(12345L, Kurrency.USD)
        val serialized = json.encodeToString(CurrencyAmountSerializer, original)
        val deserialized = json.decodeFromString(CurrencyAmountSerializer, serialized)
        assertEquals(original.minorUnits, deserialized.minorUnits)
        assertEquals(original.currency, deserialized.currency)
    }

    @Test
    fun serializesMinorUnitsAsLongNotFloat() {
        val amount = CurrencyAmount.of(12345L, Kurrency.USD)
        val serialized = json.encodeToString(CurrencyAmountSerializer, amount)
        // Should contain 12345 as an integer, not 12345.0 or a float
        assertTrue(serialized.contains("12345"))
        assertTrue(!serialized.contains("12345.0"))
    }

    @Test
    fun serializationFormat() {
        val amount = CurrencyAmount.of(500L, Kurrency.JPY)
        val serialized = json.encodeToString(CurrencyAmountSerializer, amount)
        assertTrue(serialized.contains("\"minorUnits\":500"))
        assertTrue(serialized.contains("\"currency\":\"JPY\""))
    }

    @Test
    fun deserializeInvalidCurrencyThrows() {
        val invalidJson = """{"minorUnits":100,"currency":"INVALID"}"""
        assertFailsWith<SerializationException> {
            json.decodeFromString(CurrencyAmountSerializer, invalidJson)
        }
    }
}
