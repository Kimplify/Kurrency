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
import kotlin.test.assertTrue

class CurrencyFormatterThreadSafetyTest {

    @Test
    fun formatCurrencyStyle_concurrentAccess_producesConsistentResults() = runTest {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val iterations = 100

        val results = (1..iterations).map {
            async(Dispatchers.Default) {
                formatter.formatCurrencyStyleResult("1234.56", "USD")
            }
        }.awaitAll()

        results.forEach { result ->
            assertTrue(result.isSuccess)
            assertEquals("$1,234.56", result.getOrNull())
        }
    }

    @Test
    fun formatCurrencyStyle_multipleFormattersCreatedConcurrently_allWorkCorrectly() = runTest {
        val iterations = 50

        val results = (1..iterations).map {
            async(Dispatchers.Default) {
                val formatter = CurrencyFormatter(KurrencyLocale.US)
                formatter.formatCurrencyStyleResult("500.25", "USD")
            }
        }.awaitAll()

        results.forEach { result ->
            assertTrue(result.isSuccess)
            assertEquals("$500.25", result.getOrNull())
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
        val testData = mapOf(
            "1000.00" to "$1,000.00",
            "2000.00" to "$2,000.00",
            "3000.00" to "$3,000.00",
            "4000.00" to "$4,000.00",
            "5000.00" to "$5,000.00"
        )

        val results = testData.flatMap { (amount, expected) ->
            (1..20).map {
                async(Dispatchers.Default) {
                    val result = formatter.formatCurrencyStyleResult(amount, "USD")
                    result.getOrNull() to expected
                }
            }
        }.awaitAll()

        results.forEach { (actual, expected) ->
            assertEquals(expected, actual)
        }
    }
}
