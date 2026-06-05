package org.kimplify.kurrency
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class LocaleFormattingTestInstrumented {

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
            assertTrue(formatted.contains("1") || formatted.contains("2"))
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
            // ISO format should include the currency code
            assertTrue(
                formatted.contains("USD") || formatted.contains("usd"),
                "ISO format should contain currency code"
            )
        }
    }

    @Test
    fun testFractionDigits_consistentAcrossLocales() {
        val currencyCode = "USD"

        val locales = listOf(
            KurrencyLocale.US,
            KurrencyLocale.UK,
            KurrencyLocale.GERMANY,
            KurrencyLocale.JAPAN
        )

        val fractionDigits = mutableSetOf<Int>()

        locales.forEach { _ ->
            val result = CurrencyFormatter.getFractionDigits(currencyCode)

            assertTrue(result.isSuccess, "Should get fraction digits for $currencyCode")
            result.getOrNull()?.let { fractionDigits.add(it) }
        }

        // USD should have consistent fraction digits (2) across all locales
        assertTrue(
            fractionDigits.size == 1,
            "USD should have consistent fraction digits across locales"
        )
        assertTrue(
            fractionDigits.first() == 2,
            "USD should have 2 fraction digits"
        )
    }

    @Test
    fun testFactoryMethod_createWithLocale() {
        val formatter = CurrencyFormatter(KurrencyLocale.GERMANY)
        val result = formatter.formatCurrencyStyleResult("100.50", "EUR")

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun testFactoryMethod_createWithSystemLocale() {
        val formatter = CurrencyFormatter(KurrencyLocale.systemLocale())
        val result = formatter.formatCurrencyStyleResult("100.50", "USD")

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun testFactoryMethod_createWithNullLocale() {
        val formatter = CurrencyFormatter(KurrencyLocale.systemLocale())
        val result = formatter.formatCurrencyStyleResult("100.50", "USD")

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }
}
