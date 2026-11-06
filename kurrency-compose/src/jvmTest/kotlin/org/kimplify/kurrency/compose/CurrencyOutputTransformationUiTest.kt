package org.kimplify.kurrency.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextRange
import org.kimplify.kurrency.KurrencyLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

@OptIn(ExperimentalTestApi::class)
class CurrencyOutputTransformationUiTest {

    @Test
    fun formatting_displaysMinorUnitsWithoutEditingState() = runComposeUiTest {
        val state = TextFieldState("123456", TextRange(2, 5))

        setContent {
            BasicTextField(
                state = state,
                modifier = Modifier.testTag("amount"),
                outputTransformation = rememberCurrencyOutputTransformation("USD", KurrencyLocale.US)
            )
        }

        onNodeWithTag("amount").assertTextEquals("$1,234.56")
        runOnIdle {
            assertEquals("123456", state.text.toString())
            assertEquals(TextRange(2, 5), state.selection)
        }
    }

    @Test
    fun emptyState_displaysZeroWithoutInsertingDigits() = runComposeUiTest {
        val state = TextFieldState()

        setContent {
            BasicTextField(
                state = state,
                modifier = Modifier.testTag("amount"),
                outputTransformation = rememberCurrencyOutputTransformation("USD", KurrencyLocale.US)
            )
        }

        onNodeWithTag("amount").assertTextEquals("$0.00")
        runOnIdle {
            assertEquals("", state.text.toString())
            assertEquals(TextRange.Zero, state.selection)
        }
    }

    @Test
    fun insertionAtDisplayedInteriorOffset_editsCorrespondingRawDigit() = runComposeUiTest {
        val state = TextFieldState("123456")

        setContent {
            BasicTextField(
                state = state,
                modifier = Modifier.testTag("amount"),
                outputTransformation = rememberCurrencyOutputTransformation("USD", KurrencyLocale.US)
            )
        }

        val field = onNodeWithTag("amount")
        field.assertTextEquals("$1,234.56")
        field.performTextInputSelection(TextRange(5), relativeToOriginalText = false)
        runOnIdle { assertEquals(TextRange(3), state.selection) }
        field.performTextInput("9")

        field.assertTextEquals("$12,394.56")
        runOnIdle {
            assertEquals("1239456", state.text.toString())
            assertEquals(TextRange(4), state.selection)
        }
    }

    @Test
    fun eitherSideOfDisplayedDecimal_mapsToSameRawBoundary() = runComposeUiTest {
        val beforeDecimal = TextFieldState("123456")
        val afterDecimal = TextFieldState("123456")

        setContent {
            val transformation = rememberCurrencyOutputTransformation("USD", KurrencyLocale.US)
            Column {
                BasicTextField(
                    state = beforeDecimal,
                    modifier = Modifier.testTag("before"),
                    outputTransformation = transformation
                )
                BasicTextField(
                    state = afterDecimal,
                    modifier = Modifier.testTag("after"),
                    outputTransformation = transformation
                )
            }
        }

        val before = onNodeWithTag("before")
        val after = onNodeWithTag("after")
        before.performTextInputSelection(TextRange(6), relativeToOriginalText = false)
        runOnIdle { assertEquals(TextRange(4), beforeDecimal.selection) }
        before.performTextInput("9")
        after.performTextInputSelection(TextRange(7), relativeToOriginalText = false)
        runOnIdle { assertEquals(TextRange(4), afterDecimal.selection) }
        after.performTextInput("9")

        before.assertTextEquals("$12,349.56")
        after.assertTextEquals("$12,349.56")
        runOnIdle {
            assertEquals("1234956", beforeDecimal.text.toString())
            assertEquals("1234956", afterDecimal.text.toString())
        }
    }

    @Test
    fun replacingDisplayedRangeAcrossSeparators_replacesOnlySelectedRawDigits() = runComposeUiTest {
        val state = TextFieldState("123456")

        setContent {
            BasicTextField(
                state = state,
                modifier = Modifier.testTag("amount"),
                outputTransformation = rememberCurrencyOutputTransformation("USD", KurrencyLocale.US)
            )
        }

        val field = onNodeWithTag("amount")
        field.performTextInputSelection(TextRange(2, 8), relativeToOriginalText = false)
        runOnIdle { assertEquals(TextRange(1, 5), state.selection) }
        field.performTextInput("9")

        field.assertTextEquals("$1.96")
        runOnIdle {
            assertEquals("196", state.text.toString())
            assertEquals(TextRange(2), state.selection)
        }
    }

    @Test
    fun backspaceAfterDisplayedDecimal_deletesPreviousRawDigit() = runComposeUiTest {
        val state = TextFieldState("123456")

        setContent {
            BasicTextField(
                state = state,
                modifier = Modifier.testTag("amount"),
                outputTransformation = rememberCurrencyOutputTransformation("USD", KurrencyLocale.US)
            )
        }

        val field = onNodeWithTag("amount")
        field.performTextInputSelection(TextRange(7), relativeToOriginalText = false)
        field.performKeyInput { pressKey(Key.Backspace) }

        field.assertTextEquals("$123.56")
        runOnIdle {
            assertEquals("12356", state.text.toString())
            assertEquals(TextRange(3), state.selection)
        }
    }

    @Test
    fun deleteAfterCrossingDisplayedDecimal_deletesNextRawDigit() = runComposeUiTest {
        val state = TextFieldState("123456")

        setContent {
            BasicTextField(
                state = state,
                modifier = Modifier.testTag("amount"),
                outputTransformation = rememberCurrencyOutputTransformation("USD", KurrencyLocale.US)
            )
        }

        val field = onNodeWithTag("amount")
        field.performTextInputSelection(TextRange(6), relativeToOriginalText = false)
        field.performKeyInput {
            pressKey(Key.DirectionRight)
            pressKey(Key.Delete)
        }

        field.assertTextEquals("$123.46")
        runOnIdle {
            assertEquals("12346", state.text.toString())
            assertEquals(TextRange(4), state.selection)
        }
    }

    @Test
    fun deleteAtDisplayedDigitBoundary_deletesNextRawDigit() = runComposeUiTest {
        val state = TextFieldState("123456")

        setContent {
            BasicTextField(
                state = state,
                modifier = Modifier.testTag("amount"),
                outputTransformation = rememberCurrencyOutputTransformation("USD", KurrencyLocale.US),
            )
        }

        val field = onNodeWithTag("amount")
        field.performTextInputSelection(TextRange(5), relativeToOriginalText = false)
        field.performKeyInput { pressKey(Key.Delete) }

        field.assertTextEquals("$123.56")
        runOnIdle {
            assertEquals("12356", state.text.toString())
            assertEquals(TextRange(3), state.selection)
        }
    }

    @Test
    fun shortRawAmount_displayPaddingDoesNotBecomeInput() = runComposeUiTest {
        val state = TextFieldState("5")

        setContent {
            BasicTextField(
                state = state,
                modifier = Modifier.testTag("amount"),
                outputTransformation = rememberCurrencyOutputTransformation("USD", KurrencyLocale.US)
            )
        }

        val field = onNodeWithTag("amount")
        field.assertTextEquals("$0.05")
        runOnIdle { assertEquals("5", state.text.toString()) }
        field.performTextInputSelection(TextRange(4), relativeToOriginalText = false)
        runOnIdle { assertEquals(TextRange.Zero, state.selection) }
        field.performTextInput("9")

        field.assertTextEquals("$0.95")
        runOnIdle { assertEquals("95", state.text.toString()) }
    }

    @Test
    fun leadingRawZeros_remainInStateAndKeepVisibleDigitsEditable() = runComposeUiTest {
        val state = TextFieldState("001234")

        setContent {
            BasicTextField(
                state = state,
                modifier = Modifier.testTag("amount"),
                outputTransformation = rememberCurrencyOutputTransformation("USD", KurrencyLocale.US)
            )
        }

        val field = onNodeWithTag("amount")
        field.assertTextEquals("$12.34")
        runOnIdle { assertEquals("001234", state.text.toString()) }
        field.performTextInputSelection(TextRange(2), relativeToOriginalText = false)
        runOnIdle { assertEquals(TextRange(3), state.selection) }
        field.performTextInput("9")

        field.assertTextEquals("$192.34")
        runOnIdle { assertEquals("0019234", state.text.toString()) }
    }

    @Test
    fun zeroScale_keepsIntegerZerosMappedBeforeDisplayedFraction() = runComposeUiTest {
        val state = TextFieldState("100")

        setContent {
            BasicTextField(
                state = state,
                modifier = Modifier.testTag("amount"),
                outputTransformation = rememberCurrencyOutputTransformation(
                    "USD", KurrencyLocale.US, fractionDigits = 0
                )
            )
        }

        val field = onNodeWithTag("amount")
        field.assertTextEquals("$100.00")
        field.performTextInputSelection(TextRange(3), relativeToOriginalText = false)
        runOnIdle { assertEquals(TextRange(2), state.selection) }
        field.performTextInput("9")

        field.assertTextEquals("$1,090.00")
        runOnIdle { assertEquals("1090", state.text.toString()) }
    }

    @Test
    fun sharedTransformation_andRecompositionKeepFieldStatesIndependent() = runComposeUiTest {
        val firstState = TextFieldState("1234")
        val secondState = TextFieldState("5678", TextRange(2))
        var recomposition by mutableStateOf(0)
        var observedRecomposition = -1

        setContent {
            observedRecomposition = recomposition
            val transformation = rememberCurrencyOutputTransformation("USD", KurrencyLocale.US)
            Column {
                BasicTextField(
                    state = firstState,
                    modifier = Modifier.testTag("first"),
                    outputTransformation = transformation
                )
                BasicTextField(
                    state = secondState,
                    modifier = Modifier.testTag("second"),
                    outputTransformation = transformation
                )
            }
        }

        val first = onNodeWithTag("first")
        val second = onNodeWithTag("second")
        first.assertTextEquals("$12.34")
        second.assertTextEquals("$56.78")
        first.performTextInputSelection(TextRange(2), relativeToOriginalText = false)
        first.performTextInput("9")
        runOnIdle { recomposition++ }

        first.assertTextEquals("$192.34")
        second.assertTextEquals("$56.78")
        runOnIdle {
            assertEquals(1, observedRecomposition)
            assertEquals("19234", firstState.text.toString())
            assertEquals(TextRange(2), firstState.selection)
            assertEquals("5678", secondState.text.toString())
            assertEquals(TextRange(2), secondState.selection)
        }
    }

    @Test
    fun rememberHelper_reusesEqualKeysAndRecreatesForEachChangedKey() = runComposeUiTest {
        var currencyCode by mutableStateOf("USD")
        var locale by mutableStateOf(KurrencyLocale.US)
        var fractionDigits by mutableStateOf(2)
        var recomposition by mutableStateOf(0)
        var observedRecomposition = -1
        var remembered: CurrencyOutputTransformation? = null

        setContent {
            observedRecomposition = recomposition
            remembered = rememberCurrencyOutputTransformation(currencyCode, locale, fractionDigits)
        }

        waitForIdle()
        val initial = remembered
        runOnIdle { recomposition++ }
        waitForIdle()
        assertEquals(1, observedRecomposition)
        assertSame(initial, remembered)

        runOnIdle { currencyCode = "EUR" }
        waitForIdle()
        val changedCurrency = remembered
        assertNotSame(initial, changedCurrency)

        runOnIdle { locale = KurrencyLocale.GERMANY }
        waitForIdle()
        val changedLocale = remembered
        assertNotSame(changedCurrency, changedLocale)

        runOnIdle { fractionDigits = 3 }
        waitForIdle()
        assertNotSame(changedLocale, remembered)
    }
}
