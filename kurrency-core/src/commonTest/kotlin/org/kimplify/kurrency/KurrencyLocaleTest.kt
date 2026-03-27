package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class KurrencyLocaleTest {

    @Test
    fun testFromLanguageTag_validTags() {
        val validTags = listOf(
            "en-US",
            "en-GB",
            "fr-FR",
            "de-DE",
            "ja-JP",
            "zh-CN",
            "ar-SA",
            "pt-BR",
            "es-ES"
        )

        validTags.forEach { tag ->
            val result = KurrencyLocale.fromLanguageTag(tag)
            assertTrue(result.isSuccess, "Expected $tag to be valid")
            assertEquals(tag, result.getOrNull()?.languageTag?.replace("_", "-"))
        }
    }

    @Test
    fun testFromLanguageTag_invalidTags() {
        val invalidTags = listOf(
            "",
            " ",
            "invalid",
            "123",
            "en_",
            "_US"
        )

        invalidTags.forEach { tag ->
            val result = KurrencyLocale.fromLanguageTag(tag)
            assertTrue(result.isFailure, "Expected '$tag' to be invalid")
        }
    }

    @Test
    fun testSystemLocale() {
        val systemLocale = KurrencyLocale.systemLocale()
        assertNotNull(systemLocale)
        val tag = systemLocale.languageTag
        assertTrue(tag.isNotBlank())
        assertTrue(
            tag.contains("-") || tag.length >= 2,
            "System locale tag should match BCP47 pattern: $tag"
        )
    }

    @Test
    fun testPredefinedLocales() {
        assertEquals("en-US", KurrencyLocale.US.languageTag)
        assertEquals("en-GB", KurrencyLocale.UK.languageTag)
        assertEquals("en-CA", KurrencyLocale.CANADA.languageTag)
        assertEquals("fr-CA", KurrencyLocale.CANADA_FRENCH.languageTag)
        assertEquals("de-DE", KurrencyLocale.GERMANY.languageTag)
        assertEquals("fr-FR", KurrencyLocale.FRANCE.languageTag)
        assertEquals("it-IT", KurrencyLocale.ITALY.languageTag)
        assertEquals("es-ES", KurrencyLocale.SPAIN.languageTag)
        assertEquals("ja-JP", KurrencyLocale.JAPAN.languageTag)
        assertEquals("zh-CN", KurrencyLocale.CHINA.languageTag)
        assertEquals("ko-KR", KurrencyLocale.KOREA.languageTag)
        assertEquals("pt-BR", KurrencyLocale.BRAZIL.languageTag)
        assertEquals("ru-RU", KurrencyLocale.RUSSIA.languageTag)
        assertEquals("ar-SA", KurrencyLocale.SAUDI_ARABIA.languageTag)
        assertEquals("hi-IN", KurrencyLocale.INDIA.languageTag)
    }

    @Test
    fun testLocaleEquality() {
        val locale1 = KurrencyLocale.fromLanguageTag("en-US").getOrThrow()
        val locale2 = KurrencyLocale.fromLanguageTag("en-US").getOrThrow()
        val locale3 = KurrencyLocale.fromLanguageTag("en-GB").getOrThrow()

        assertTrue(locale1 == locale2)
        assertFalse(locale1 == locale3)
    }

    @Test
    fun testLocaleHashCode() {
        val locale1 = KurrencyLocale.fromLanguageTag("en-US").getOrThrow()
        val locale2 = KurrencyLocale.fromLanguageTag("en-US").getOrThrow()

        assertEquals(locale1.hashCode(), locale2.hashCode())
    }

    @Test
    fun testLocaleToString() {
        val locale = KurrencyLocale.US
        val string = locale.toString()
        assertTrue(string.contains("KurrencyLocale"))
        assertTrue(string.contains(locale.languageTag.replace("_", "-")))
    }
}
