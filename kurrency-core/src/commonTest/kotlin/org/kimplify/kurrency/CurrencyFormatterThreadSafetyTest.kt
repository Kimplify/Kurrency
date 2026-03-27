package org.kimplify.kurrency

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CurrencyFormatterThreadSafetyTest {

    @Test
    fun formatCurrencyStyle_concurrentAccess_producesConsistentResults() = runTest {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val iterations = 100

        val expected = formatter.formatCurrencyStyleResult("1234.56", "USD").getOrThrow()

        val results = (1..iterations).map {
            async(Dispatchers.Default) {
                formatter.formatCurrencyStyleResult("1234.56", "USD")
            }
        }.awaitAll()

        results.forEach { result ->
            assertTrue(result.isSuccess)
            val formatted = result.getOrNull()
            assertNotNull(formatted)
            assertEquals(expected, formatted)
        }
    }

    @Test
    fun formatCurrencyStyle_multipleFormattersCreatedConcurrently_allWorkCorrectly() = runTest {
        val iterations = 50

        val referenceFormatter = CurrencyFormatter(KurrencyLocale.US)
        val expected = referenceFormatter.formatCurrencyStyleResult("500.25", "USD").getOrThrow()

        val results = (1..iterations).map {
            async(Dispatchers.Default) {
                val formatter = CurrencyFormatter(KurrencyLocale.US)
                formatter.formatCurrencyStyleResult("500.25", "USD")
            }
        }.awaitAll()

        results.forEach { result ->
            assertTrue(result.isSuccess)
            val formatted = result.getOrNull()
            assertNotNull(formatted)
            assertEquals(expected, formatted)
        }
    }

    @Test
    fun formatCurrencyStyle_concurrentAccessWithDifferentCurrencies_producesCorrectResults() = runTest {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val currencies = listOf("USD", "EUR", "GBP", "JPY", "CAD")

        val results = currencies.flatMap { currency ->
            (1..20).map {
                async(Dispatchers.Default) {
                    formatter.formatCurrencyStyleResult("100.00", currency) to currency
                }
            }
        }.awaitAll()

        results.forEach { (result, _) ->
            assertTrue(result.isSuccess)
            val formatted = result.getOrNull()
            assertNotNull(formatted)
            assertTrue(formatted.any { it.isDigit() })
        }
    }

    @Test
    fun getFractionDigits_concurrentAccess_producesConsistentResults() = runTest {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val iterations = 100

        val results = (1..iterations).map {
            async(Dispatchers.Default) {
                formatter.getFractionDigitsOrDefault("USD")
            }
        }.awaitAll()

        results.forEach { result ->
            assertEquals(2, result)
        }
    }

    @Test
    fun formatCurrencyStyle_concurrentAccessWithMultipleLocales_producesCorrectResults() = runTest {
        val locales = listOf(
            KurrencyLocale.US,
            KurrencyLocale.GERMANY,
            KurrencyLocale.JAPAN,
            KurrencyLocale.UK,
            KurrencyLocale.FRANCE
        )

        val results = locales.flatMap { locale ->
            (1..20).map {
                async(Dispatchers.Default) {
                    val formatter = CurrencyFormatter(locale)
                    formatter.formatCurrencyStyleResult("999.99", "USD")
                }
            }
        }.awaitAll()

        results.forEach { result ->
            assertTrue(result.isSuccess)
        }
    }

    @Test
    fun formatIsoCurrencyStyle_concurrentAccess_producesConsistentResults() = runTest {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val iterations = 100

        val results = (1..iterations).map {
            async(Dispatchers.Default) {
                formatter.formatIsoCurrencyStyleResult("750.00", "EUR")
            }
        }.awaitAll()

        results.forEach { result ->
            assertTrue(result.isSuccess)
            val formatted = result.getOrNull()
            assertTrue(formatted?.contains("EUR") == true)
            assertTrue(formatted?.contains("750.00") == true)
        }
    }

    @Test
    fun currencyFromCode_concurrentAccess_producesConsistentResults() = runTest {
        val iterations = 100

        val results = (1..iterations).map {
            async(Dispatchers.Default) {
                Kurrency.fromCode("USD")
            }
        }.awaitAll()

        results.forEach { result ->
            assertTrue(result.isSuccess)
            val currency = result.getOrNull()
            assertEquals("USD", currency?.code)
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    @Test
    fun formatCurrencyStyle_mixedReadWriteOperations_threadsafe() = runTest {
        val formatters = mutableListOf<CurrencyFormatter>()
        val lock = SynchronizedObject()
        val iterations = 50

        val writeJobs = (1..iterations).map {
            async(Dispatchers.Default) {
                val formatter = CurrencyFormatter(KurrencyLocale.US)
                synchronized(lock) {
                    formatters.add(formatter)
                }
            }
        }

        val readJobs = (1..iterations).map {
            async(Dispatchers.Default) {
                val formatter = synchronized(lock) {
                    formatters.randomOrNull()
                }
                formatter?.formatCurrencyStyleResult("100.00", "USD")
            }
        }

        writeJobs.awaitAll()
        readJobs.awaitAll()

        assertEquals(iterations, formatters.size)
    }

    @Test
    fun formatCurrencyStyle_heavyConcurrentLoad_handlesGracefully() = runTest {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val threadCount = 100
        val operationsPerThread = 10

        val results = (1..threadCount).flatMap { threadId ->
            (1..operationsPerThread).map { opId ->
                async(Dispatchers.Default) {
                    val amount = "${threadId}.${opId.toString().padStart(2, '0')}"
                    formatter.formatCurrencyStyleResult(amount, "USD")
                }
            }
        }.awaitAll()

        assertEquals(threadCount * operationsPerThread, results.size)
        results.forEach { result ->
            assertTrue(result.isSuccess)
        }
    }

    @Test
    fun isValidCurrency_concurrentAccess_producesConsistentResults() = runTest {
        val iterations = 100

        val results = (1..iterations).map {
            async(Dispatchers.Default) {
                isValidCurrency("USD")
            }
        }.awaitAll()

        results.forEach { result ->
            assertTrue(result)
        }
    }

    @Test
    fun formatCurrencyStyle_sameFormatterMultipleThreads_noDataCorruption() = runTest {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val amounts = listOf("1000.00", "2000.00", "3000.00", "4000.00", "5000.00")

        val expectedByAmount = amounts.associateWith { amount ->
            formatter.formatCurrencyStyleResult(amount, "USD").getOrThrow()
        }

        val results = amounts.flatMap { amount ->
            (1..20).map {
                async(Dispatchers.Default) {
                    val result = formatter.formatCurrencyStyleResult(amount, "USD")
                    result.getOrNull() to amount
                }
            }
        }.awaitAll()

        results.forEach { pair: Pair<String?, String> ->
            assertNotNull(pair.first)
            assertEquals(expectedByAmount[pair.second], pair.first)
        }
    }

    @Test
    fun currencyAmountFromMajorUnits_concurrentAccess_producesConsistentResults() = runTest {
        val iterations = 100

        val results = (1..iterations).map {
            async(Dispatchers.Default) {
                CurrencyAmount.fromMajorUnits("123.45", Kurrency.USD)
            }
        }.awaitAll()

        results.forEach { result ->
            assertTrue(result.isSuccess, "CurrencyAmount.fromMajorUnits should succeed")
            val amount = result.getOrNull()
            assertNotNull(amount)
            assertEquals(12345L, amount.minorUnits, "Minor units should be consistent")
            assertEquals(Kurrency.USD, amount.currency, "Currency should be consistent")
        }
    }

    @Test
    fun currencyFormatterCreation_concurrentWithDifferentLocales_allWorkCorrectly() = runTest {
        val locales = listOf(
            KurrencyLocale.US,
            KurrencyLocale.GERMANY,
            KurrencyLocale.JAPAN,
            KurrencyLocale.UK,
            KurrencyLocale.FRANCE,
            KurrencyLocale.ITALY,
            KurrencyLocale.SPAIN,
            KurrencyLocale.BRAZIL,
            KurrencyLocale.CANADA,
            KurrencyLocale.CHINA,
        )

        val results = locales.flatMap { locale ->
            (1..10).map {
                async(Dispatchers.Default) {
                    val formatter = CurrencyFormatter(locale)
                    val result = formatter.formatCurrencyStyleResult("1000.00", "USD")
                    Triple(locale, result, formatter)
                }
            }
        }.awaitAll()

        // All formatters should succeed
        results.forEach { (locale, result, _) ->
            assertTrue(result.isSuccess, "Formatting with locale ${locale.languageTag} should succeed")
            val formatted = result.getOrNull()
            assertNotNull(formatted, "Formatted result for ${locale.languageTag} should not be null")
            assertTrue(formatted.any { it.isDigit() }, "Result should contain digits: $formatted")
        }

        // Verify consistency: same locale should produce same output
        val groupedByLocale = results.groupBy { it.first.languageTag }
        groupedByLocale.forEach { (tag, group) ->
            val firstResult = group.first().second.getOrThrow()
            group.forEach { (_, result, _) ->
                assertEquals(
                    firstResult,
                    result.getOrThrow(),
                    "Same locale ($tag) should produce same output"
                )
            }
        }
    }
}
