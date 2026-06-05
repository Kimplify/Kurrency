package org.kimplify.kurrency
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class KurrencyLocaleTestInstrumented {

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
        assertTrue(systemLocale.languageTag.isNotBlank())
    }

    @Test
    fun testPredefinedLocales() {
        // Test that all predefined locales are valid
        val locales = listOf(
            KurrencyLocale.US,
            KurrencyLocale.UK,
            KurrencyLocale.CANADA,
            KurrencyLocale.CANADA_FRENCH,
            KurrencyLocale.GERMANY,
            KurrencyLocale.FRANCE,
            KurrencyLocale.ITALY,
            KurrencyLocale.SPAIN,
            KurrencyLocale.JAPAN,
            KurrencyLocale.CHINA,
            KurrencyLocale.KOREA,
            KurrencyLocale.BRAZIL,
            KurrencyLocale.RUSSIA,
            KurrencyLocale.SAUDI_ARABIA,
            KurrencyLocale.INDIA
        )

        locales.forEach { locale ->
            assertNotNull(locale)
            assertTrue(locale.languageTag.isNotBlank())
        }
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
