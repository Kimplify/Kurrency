package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The options path resolved its symbol from [CurrencyMetadata], which holds one generic symbol per
 * currency, so every dollar currency rendered as a bare `$`. Migrating from the locale-aware
 * formatters to the options API silently lost the disambiguation that keeps AUD and USD apart.
 */
class LocaleAwareSymbolTest {

    private val us = CurrencyFormatter(KurrencyLocale.US)

    @Test
    fun audToAUsReaderKeepsItsDisambiguatingSymbol() {
        assertEquals(
            "A$100",
            us.formatMinorUnitsWithOptions(
                10_000,
                "AUD",
                CurrencyFormatOptions { hideZeroFractionDigits = true },
            ).getOrThrow(),
        )
    }

    @Test
    fun usdToAUsReaderStaysAPlainDollarSign() {
        assertEquals(
            "$100",
            us.formatMinorUnitsWithOptions(
                10_000,
                "USD",
                CurrencyFormatOptions { hideZeroFractionDigits = true },
            ).getOrThrow(),
        )
    }

    @Test
    fun theOptionsPathAgreesWithTheLocaleAwarePathOnTheSymbol() {
        val viaPlatform = us.formatMinorUnits(10_000, "AUD")
        val viaOptions = us.formatMinorUnitsWithOptions(
            10_000,
            "AUD",
            CurrencyFormatOptions.STANDARD,
        ).getOrThrow()
        assertEquals(viaPlatform, viaOptions)
    }

    @Test
    fun theSameCurrencyCarriesADifferentSymbolForADifferentReader() {
        val au = CurrencyFormatter(KurrencyLocale.fromLanguageTag("en-AU").getOrThrow())
        assertEquals(
            "A$100.00",
            us.formatMinorUnitsWithOptions(10_000, "AUD", CurrencyFormatOptions.STANDARD).getOrThrow(),
        )
        assertEquals(
            "$100.00",
            au.formatMinorUnitsWithOptions(10_000, "AUD", CurrencyFormatOptions.STANDARD).getOrThrow(),
        )
    }

    @Test
    fun anIsoCodeIsUnaffectedByLocaleAwareSymbolResolution() {
        assertEquals(
            "AUD 100.00",
            us.formatMinorUnitsWithOptions(10_000, "AUD", CurrencyFormatOptions.ISO).getOrThrow(),
        )
    }
}
