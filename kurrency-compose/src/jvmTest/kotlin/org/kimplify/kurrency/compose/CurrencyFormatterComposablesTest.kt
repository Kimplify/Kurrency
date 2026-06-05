package org.kimplify.kurrency.compose

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
    fun rememberCurrencyFormatter_withUSLocale_createsFormatter() = runComposeUiTest {
        var formatted = ""

        setContent {
            val formatter = rememberCurrencyFormatter(KurrencyLocale.US)
            formatted = formatter.formatCurrencyStyle("1234.56", "USD")
        }

        waitForIdle()
        assertEquals("$1,234.56", formatted)
    }

    @Test
    fun rememberCurrencyFormatter_withGermanyLocale_formatsCorrectly() = runComposeUiTest {
        var formatted = ""

        setContent {
            val formatter = rememberCurrencyFormatter(KurrencyLocale.GERMANY)
            formatted = formatter.formatCurrencyStyle("1234.56", "EUR")
        }

        waitForIdle()
        assertTrue(formatted.contains("1.234,56") || formatted.contains("1 234,56"))
    }

    @Test
    fun rememberCurrencyFormatter_whenLocaleChanges_recreatesFormatter() = runComposeUiTest {
        var locale by mutableStateOf(KurrencyLocale.US)
        var lastFormattedValue = ""

        setContent {
            val formatter = rememberCurrencyFormatter(locale)
            lastFormattedValue = formatter.formatCurrencyStyle("1000.00", "USD")
        }

        waitForIdle()
        assertEquals("$1,000.00", lastFormattedValue)

        locale = KurrencyLocale.GERMANY
        waitForIdle()
        assertTrue(lastFormattedValue.contains("1.000,00") || lastFormattedValue.contains("1 000,00"))
    }

    @Test
    fun rememberSystemCurrencyFormatter_createsFormatter() = runComposeUiTest {
        var formatted = ""

        setContent {
            val formatter = rememberSystemCurrencyFormatter()
            formatted = formatter.formatCurrencyStyle("100.00", "USD")
        }

        waitForIdle()
        assertTrue(formatted.isNotBlank())
    }

    @Test
    fun localCurrencyFormatter_providesDefaultFormatter() = runComposeUiTest {
        var formatted = ""

        setContent {
            val formatter = LocalCurrencyFormatter.current
            formatted = formatter.formatCurrencyStyle("100.00", "USD")
        }

        waitForIdle()
        assertTrue(formatted.isNotBlank())
    }

    @Test
    fun provideCurrencyFormatter_providesFormatterToChildren() = runComposeUiTest {
        var childFormatted = ""

        setContent {
            ProvideCurrencyFormatter(locale = KurrencyLocale.JAPAN) {
                val formatter = LocalCurrencyFormatter.current
                childFormatted = formatter.formatCurrencyStyle("1234", "JPY")
            }
        }

        waitForIdle()
        assertTrue(childFormatted.contains("1,234") || childFormatted.contains("¥"))
    }

    @Test
    fun provideCurrencyFormatter_canBeNested_innerOverridesOuter() = runComposeUiTest {
        var outerFormatted = ""
        var innerFormatted = ""

        setContent {
            ProvideCurrencyFormatter(locale = KurrencyLocale.US) {
                outerFormatted = LocalCurrencyFormatter.current.formatCurrencyStyle("100.00", "USD")

                ProvideCurrencyFormatter(locale = KurrencyLocale.JAPAN) {
                    innerFormatted = LocalCurrencyFormatter.current.formatCurrencyStyle("100", "JPY")
                }
            }
        }

        waitForIdle()
        assertTrue(outerFormatted.contains("$"))
        assertTrue(innerFormatted.contains("100") || innerFormatted.contains("¥"))
    }
}
