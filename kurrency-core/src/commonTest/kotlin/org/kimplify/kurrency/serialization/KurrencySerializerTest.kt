package org.kimplify.kurrency.serialization

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.kimplify.kurrency.Kurrency
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class KurrencySerializerTest {

    private val json = KurrencyJson

    @Test
    fun serializeUsd() {
        val serialized = json.encodeToString(KurrencySerializer, Kurrency.USD)
        assertEquals("\"USD\"", serialized)
    }

    @Test
    fun deserializeUsd() {
        val deserialized = json.decodeFromString(KurrencySerializer, "\"USD\"")
        assertEquals(Kurrency.USD, deserialized)
    }

    @Test
    fun roundTripUsd() {
        val original = Kurrency.USD
        val serialized = json.encodeToString(KurrencySerializer, original)
        val deserialized = json.decodeFromString(KurrencySerializer, serialized)
        assertEquals(original, deserialized)
    }

    @Test
    fun roundTripJpy() {
        val original = Kurrency.JPY
        val serialized = json.encodeToString(KurrencySerializer, original)
        val deserialized = json.decodeFromString(KurrencySerializer, serialized)
        assertEquals(original, deserialized)
        assertEquals("\"JPY\"", serialized)
    }

    @Test
    fun deserializeInvalidCodeThrows() {
        assertFailsWith<SerializationException> {
            json.decodeFromString(KurrencySerializer, "\"INVALID\"")
        }
    }
}
