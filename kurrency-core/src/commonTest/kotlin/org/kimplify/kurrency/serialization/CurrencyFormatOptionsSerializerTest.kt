package org.kimplify.kurrency.serialization

import kotlinx.serialization.json.Json
import org.kimplify.kurrency.CurrencyFormatOptions
import org.kimplify.kurrency.NegativeStyle
import org.kimplify.kurrency.SymbolDisplay
import org.kimplify.kurrency.SymbolPosition
import org.kimplify.kurrency.ZeroDisplay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CurrencyFormatOptionsSerializerTest {

    private val json = KurrencyJson

    @Test
    fun defaultOptionsSerializesToMinimalJson() {
        val options = CurrencyFormatOptions()
        val serialized = json.encodeToString(CurrencyFormatOptions.serializer(), options)
        // encodeDefaults = false, so default values are omitted
        assertEquals("{}", serialized)
    }

    @Test
    fun customOptionsRoundTrip() {
        val original = CurrencyFormatOptions(
            symbolPosition = SymbolPosition.TRAILING,
            grouping = false,
            minFractionDigits = 0,
            maxFractionDigits = 4,
            negativeStyle = NegativeStyle.PARENTHESES,
            symbolDisplay = SymbolDisplay.ISO_CODE,
            zeroDisplay = ZeroDisplay.DASH,
        )
        val serialized = json.encodeToString(CurrencyFormatOptions.serializer(), original)
        val deserialized = json.decodeFromString(CurrencyFormatOptions.serializer(), serialized)
        assertEquals(original, deserialized)
    }

    @Test
    fun partialJsonUsesDefaults() {
        val partial = """{"symbolDisplay":"NONE"}"""
        val deserialized = json.decodeFromString(CurrencyFormatOptions.serializer(), partial)
        assertEquals(SymbolDisplay.NONE, deserialized.symbolDisplay)
        // All other fields should retain defaults
        assertEquals(SymbolPosition.LOCALE_DEFAULT, deserialized.symbolPosition)
        assertEquals(true, deserialized.grouping)
        assertEquals(null, deserialized.minFractionDigits)
        assertEquals(null, deserialized.maxFractionDigits)
        assertEquals(NegativeStyle.MINUS_SIGN, deserialized.negativeStyle)
        assertEquals(ZeroDisplay.SHOW, deserialized.zeroDisplay)
    }

    @Test
    fun unknownFieldsAreIgnored() {
        val jsonWithExtra = """{"unknownField":"value","symbolDisplay":"SYMBOL"}"""
        val deserialized = json.decodeFromString(CurrencyFormatOptions.serializer(), jsonWithExtra)
        assertEquals(SymbolDisplay.SYMBOL, deserialized.symbolDisplay)
    }

    @Test
    fun onlyNonDefaultFieldsSerialized() {
        val options = CurrencyFormatOptions(negativeStyle = NegativeStyle.PARENTHESES)
        val serialized = json.encodeToString(CurrencyFormatOptions.serializer(), options)
        assertTrue(serialized.contains("PARENTHESES"))
        // Default fields should not appear
        assertTrue(!serialized.contains("symbolPosition"))
        assertTrue(!serialized.contains("grouping"))
        assertTrue(!serialized.contains("zeroDisplay"))
    }
}
