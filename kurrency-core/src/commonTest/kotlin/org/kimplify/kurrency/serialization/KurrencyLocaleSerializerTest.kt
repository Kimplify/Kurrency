package org.kimplify.kurrency.serialization

import kotlinx.serialization.SerializationException
import org.kimplify.kurrency.KurrencyLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class KurrencyLocaleSerializerTest {

    private val json = KurrencyJson

    @Test
    fun roundTripUsLocale() {
        val original = KurrencyLocale.US
        val serialized = json.encodeToString(KurrencyLocaleSerializer, original)
        assertEquals("\"en-US\"", serialized)
        val deserialized = json.decodeFromString(KurrencyLocaleSerializer, serialized)
        assertEquals(original.languageTag, deserialized.languageTag)
    }

    @Test
    fun roundTripGermanyLocale() {
        val original = KurrencyLocale.GERMANY
        val serialized = json.encodeToString(KurrencyLocaleSerializer, original)
        assertEquals("\"de-DE\"", serialized)
        val deserialized = json.decodeFromString(KurrencyLocaleSerializer, serialized)
        assertEquals(original.languageTag, deserialized.languageTag)
    }

    @Test
    fun deserializeInvalidLocaleThrows() {
        assertFailsWith<SerializationException> {
            json.decodeFromString(KurrencyLocaleSerializer, "\"\"")
        }
    }
}
