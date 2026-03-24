package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LocaleFormattingTest {

    @Test
    fun testFormatting_withDifferentLocales() {
        val amount = "1234.56"
        val currencyCode = "USD"

        val locales = listOf(
            KurrencyLocale.US,
            KurrencyLocale.UK,
            KurrencyLocale.GERMANY,
            KurrencyLocale.FRANCE,
            KurrencyLocale.JAPAN
        )

        locales.forEach { locale ->
            val formatter = CurrencyFormatter(locale)
            val result = formatter.formatCurrencyStyleResult(amount, currencyCode)

            assertTrue(
                result.isSuccess,
                "Formatting should succeed for locale ${locale.languageTag}"
            )

            val formatted = result.getOrNull()
            assertNotNull(formatted, "Formatted value should not be null for ${locale.languageTag}")
            assertTrue(formatted.isNotBlank(), "Formatted value should not be blank")
            assertTrue(
                formatted.any { it.isDigit() },
                "Formatted value should contain digits for ${locale.languageTag}: $formatted"
            )
            assertTrue(
                formatted.contains("1") && formatted.contains("2") && formatted.contains("3"),
                "Formatted value should contain digits from 123x for ${locale.languageTag}: $formatted"
            )
        }
    }

    @Test
    fun testFormatting_euroWithDifferentLocales() {
        val amount = "1234.56"
        val currencyCode = "EUR"

        val locales = listOf(
            KurrencyLocale.GERMANY,
            KurrencyLocale.FRANCE,
            KurrencyLocale.ITALY,
            KurrencyLocale.SPAIN
        )

        locales.forEach { locale ->
            val formatter = CurrencyFormatter(locale)
            val result = formatter.formatCurrencyStyleResult(amount, currencyCode)

            assertTrue(
                result.isSuccess,
                "EUR formatting should succeed for locale ${locale.languageTag}"
            )

            val formatted = result.getOrNull()
            assertNotNull(formatted)
            assertTrue(
                formatted.contains("1") && formatted.contains("2") && formatted.contains("3"),
                "Formatted EUR value should contain digits from 123x for ${locale.languageTag}: $formatted"
            )
        }
    }

    @Test
    fun testIsoFormatting_withDifferentLocales() {
        val amount = "1234.56"
        val currencyCode = "USD"

        val locales = listOf(
            KurrencyLocale.US,
            KurrencyLocale.JAPAN,
            KurrencyLocale.GERMANY
        )

        locales.forEach { locale ->
            val formatter = CurrencyFormatter(locale)
            val result = formatter.formatIsoCurrencyStyleResult(amount, currencyCode)

            assertTrue(
                result.isSuccess,
                "ISO formatting should succeed for locale ${locale.languageTag}"
            )

            val formatted = result.getOrNull()
            assertNotNull(formatted)
            assertTrue(
                formatted.contains("USD"),
                "ISO format should contain currency code USD for ${locale.languageTag}: $formatted"
            )
        }
    }

    @Test
    fun testFractionDigits_consistentAcrossLocales() {
        val currencyCode = "USD"

        val result = CurrencyFormatter.getFractionDigits(currencyCode)

        assertTrue(result.isSuccess, "Should get fraction digits for $currencyCode")
        assertTrue(
            result.getOrNull() == 2,
            "USD should have 2 fraction digits"
        )
    }

    @Test
    fun testConstructor_createWithLocale() {
        val formatter = CurrencyFormatter(KurrencyLocale.GERMANY)
        val result = formatter.formatCurrencyStyleResult("100.50", "EUR")

        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.any { it.isDigit() })
    }

    @Test
    fun testConstructor_createWithSystemLocale() {
        val formatter = CurrencyFormatter()
        val result = formatter.formatCurrencyStyleResult("100.50", "USD")

        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.any { it.isDigit() })
    }

    @Test
    fun testConstructor_createWithExplicitSystemLocale() {
        val formatter = CurrencyFormatter(KurrencyLocale.systemLocale())
        val result = formatter.formatCurrencyStyleResult("100.50", "USD")

        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.any { it.isDigit() })
    }

    @Test
    fun testIsoFormattingContainsCurrencyCodeForAllLocales() {
        val locales = listOf(KurrencyLocale.US, KurrencyLocale.GERMANY, KurrencyLocale.JAPAN)
        locales.forEach { locale ->
            val formatter = CurrencyFormatter(locale)
            val result = formatter.formatIsoCurrencyStyle("1234.56", "EUR")
            assertTrue(
                result.contains("EUR"),
                "ISO format for ${locale.languageTag} should contain EUR: $result"
            )
        }
    }
}
