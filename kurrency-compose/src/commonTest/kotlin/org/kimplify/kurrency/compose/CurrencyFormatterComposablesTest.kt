package org.kimplify.kurrency.compose

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import org.kimplify.kurrency.CurrencyFormatter
import org.kimplify.kurrency.KurrencyLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class CurrencyFormatterComposablesTest {

    @Test
    fun rememberCurrencyFormatter_withUSLocale_createsFormatterWithUSLocale() = runComposeUiTest {
        var capturedFormatter: CurrencyFormatter? = null

        setContent {
            val formatter = rememberCurrencyFormatter(KurrencyLocale.US)
            capturedFormatter = formatter as? CurrencyFormatter
        }

        assertNotNull(capturedFormatter)
        val result = capturedFormatter!!.formatCurrencyStyleResult("1234.56", "USD")
        assertTrue(result.isSuccess)
        assertEquals("$1,234.56", result.getOrNull())
    }

    @Test
    fun rememberCurrencyFormatter_withGermanyLocale_createsFormatterWithGermanyLocale() = runComposeUiTest {
        var capturedFormatter: CurrencyFormatter? = null

        setContent {
            val formatter = rememberCurrencyFormatter(KurrencyLocale.GERMANY)
            capturedFormatter = formatter as? CurrencyFormatter
        }

        assertNotNull(capturedFormatter)
        val result = capturedFormatter!!.formatCurrencyStyleResult("1234.56", "EUR")
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("1.234,56") || formatted.contains("1 234,56"))
    }

    @Test
    fun rememberCurrencyFormatter_whenLocaleChanges_recreatesFormatter() = runComposeUiTest {
        var locale by mutableStateOf(KurrencyLocale.US)
        var formatterCreationCount = 0
        var lastFormattedValue = ""

        setContent {
            val formatter = rememberCurrencyFormatter(locale)
            formatterCreationCount++

            val result = formatter.formatCurrencyStyleResult("1000.00", "USD")
            lastFormattedValue = result.getOrNull() ?: ""
        }

        waitForIdle()
        assertEquals("$1,000.00", lastFormattedValue)
        val initialCount = formatterCreationCount

        locale = KurrencyLocale.GERMANY

        waitForIdle()
        assertTrue(formatterCreationCount > initialCount)
        assertTrue(lastFormattedValue.contains("1.000,00") || lastFormattedValue.contains("1 000,00"))
    }

    @Test
    fun rememberSystemCurrencyFormatter_createsFormatter() = runComposeUiTest {
        var capturedFormatter: CurrencyFormatter? = null

        setContent {
            val formatter = rememberSystemCurrencyFormatter()
            capturedFormatter = formatter as? CurrencyFormatter
        }

        assertNotNull(capturedFormatter)
        val result = capturedFormatter!!.formatCurrencyStyleResult("100.00", "USD")
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun localCurrencyFormatter_providesDefaultFormatter() = runComposeUiTest {
        var capturedFormatter: CurrencyFormatter? = null

        setContent {
            capturedFormatter = LocalCurrencyFormatter.current as? CurrencyFormatter
        }

        assertNotNull(capturedFormatter)
    }

    @Test
    fun provideCurrencyFormatter_providesFormatterToChildren() = runComposeUiTest {
        var childFormatter: CurrencyFormatter? = null

        setContent {
            ProvideCurrencyFormatter(locale = KurrencyLocale.JAPAN) {
                childFormatter = LocalCurrencyFormatter.current as? CurrencyFormatter
            }
        }

        assertNotNull(childFormatter)
        val result = childFormatter!!.formatCurrencyStyleResult("1234", "JPY")
        assertTrue(result.isSuccess)
        val formatted = result.getOrNull()
        assertNotNull(formatted)
        assertTrue(formatted.contains("1,234") || formatted.contains("¥"))
    }

    @Test
    fun provideCurrencyFormatter_whenLocaleChanges_updatesProvidedFormatter() = runComposeUiTest {
        var locale by mutableStateOf(KurrencyLocale.US)
        var lastFormattedValue = ""

        setContent {
            ProvideCurrencyFormatter(locale = locale) {
                val formatter = LocalCurrencyFormatter.current
                val result = formatter.formatCurrencyStyleResult("500.50", "USD")
                lastFormattedValue = result.getOrNull() ?: ""
            }
        }

        waitForIdle()
        assertEquals("$500.50", lastFormattedValue)

        locale = KurrencyLocale.UK

        waitForIdle()
        assertTrue(lastFormattedValue.contains("500.50") || lastFormattedValue.contains("US$"))
    }

    @Test
    fun provideSystemCurrencyFormatter_providesSystemLocaleFormatter() = runComposeUiTest {
        var childFormatter: CurrencyFormatter? = null

        setContent {
            ProvideSystemCurrencyFormatter {
                childFormatter = LocalCurrencyFormatter.current as? CurrencyFormatter
            }
        }

        assertNotNull(childFormatter)
        val result = childFormatter!!.formatCurrencyStyleResult("99.99", "EUR")
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    @Test
    fun provideCurrencyFormatter_canBeNested_innerOverridesOuter() = runComposeUiTest {
        var outerFormattedValue = ""
        var innerFormattedValue = ""

        setContent {
            ProvideCurrencyFormatter(locale = KurrencyLocale.US) {
                val outerFormatter = LocalCurrencyFormatter.current
                outerFormattedValue = outerFormatter.formatCurrencyStyleResult("100.00", "USD").getOrNull() ?: ""

                ProvideCurrencyFormatter(locale = KurrencyLocale.JAPAN) {
                    val innerFormatter = LocalCurrencyFormatter.current
                    innerFormattedValue = innerFormatter.formatCurrencyStyleResult("100", "JPY").getOrNull() ?: ""
                }
            }
        }

        waitForIdle()
        assertTrue(outerFormattedValue.contains("$"))
        assertTrue(innerFormattedValue.contains("100") || innerFormattedValue.contains("¥"))
    }

    @Test
    fun rememberCurrencyFormatter_withMultipleLocales_formatsCorrectly() = runComposeUiTest {
        var usFormatted = ""
        var deFormatted = ""
        var jpFormatted = ""

        setContent {
            val usFormatter = rememberCurrencyFormatter(KurrencyLocale.US)
            val deFormatter = rememberCurrencyFormatter(KurrencyLocale.GERMANY)
            val jpFormatter = rememberCurrencyFormatter(KurrencyLocale.JAPAN)

            usFormatted = usFormatter.formatCurrencyStyleResult("1234.56", "USD").getOrNull() ?: ""
            deFormatted = deFormatter.formatCurrencyStyleResult("1234.56", "EUR").getOrNull() ?: ""
            jpFormatted = jpFormatter.formatCurrencyStyleResult("1234", "JPY").getOrNull() ?: ""
        }

        waitForIdle()
        assertEquals("$1,234.56", usFormatted)
        assertTrue(deFormatted.contains("1.234,56") || deFormatted.contains("1 234,56"))
        assertTrue(jpFormatted.contains("1,234") || jpFormatted.contains("¥"))
    }
}
