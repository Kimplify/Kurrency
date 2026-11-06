package org.kimplify.sample

import android.graphics.Bitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.text.TextRange
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runner.RunWith

@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class CurrencyInputTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val testName = TestName()

    @Before
    fun focusCurrencyInput() {
        field().performScrollTo().performClick()
    }

    @After
    fun captureScreen() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val directory = File(requireNotNull(
            InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
        ))
        directory.mkdirs()
        val screenshot = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
        try {
            File(directory, "${testName.methodName}.png").outputStream().use {
                check(screenshot.compress(Bitmap.CompressFormat.PNG, 100, it))
            }
        } finally {
            screenshot.recycle()
        }
    }

    @Test
    fun initialAmountDisplaysCurrencyAndKeepsMinorUnits() {
        assertAmount("$12.34", "1234")
    }

    @Test
    fun enteringAndClearingDigitsKeepsZeroDisplaySeparateFromStoredText() {
        field().performTextClearance()
        assertAmount("$0.00", "")

        field().performTextInput("123456")
        assertAmount("$1,234.56", "123456")

        field().performTextClearance()
        assertAmount("$0.00", "")

        field().performTextInput("0")
        assertAmount("$0.00", "0")
    }

    @Test
    fun insertionAtDisplayedInteriorOffsetEditsCorrespondingRawDigit() {
        enterGroupedAmount()

        field().performTextInputSelection(TextRange(5), relativeToOriginalText = false)
        field().performTextInput("9")

        assertAmount("$12,394.56", "1239456")
    }

    @Test
    fun replacingDisplayedSelectionAcrossSeparatorsReplacesSelectedRawDigits() {
        enterGroupedAmount()

        field().performTextInputSelection(TextRange(2, 8), relativeToOriginalText = false)
        field().performTextInput("9")

        assertAmount("$1.96", "196")
    }

    @Test
    fun backspaceAfterDisplayedDecimalDeletesPreviousRawDigit() {
        enterGroupedAmount()

        field().performTextInputSelection(TextRange(7), relativeToOriginalText = false)
        field().performKeyInput { pressKey(Key.Backspace) }

        assertAmount("$123.56", "12356")
    }

    @Test
    fun deleteAtDisplayedDigitBoundaryDeletesNextRawDigit() {
        enterGroupedAmount()

        field().performTextInputSelection(TextRange(5), relativeToOriginalText = false)
        field().performKeyInput { pressKey(Key.Delete) }

        assertAmount("$123.56", "12356")
    }

    private fun enterGroupedAmount() {
        field().performTextClearance()
        field().performTextInput("123456")
        assertAmount("$1,234.56", "123456")
    }

    private fun field(): SemanticsNodeInteraction = composeRule.onNodeWithTag("currencyInput")

    private fun assertAmount(displayedAmount: String, rawAmount: String) {
        field().assertTextEquals(displayedAmount)
        composeRule.onNodeWithTag("currencyRawAmount")
            .assertTextEquals("Stored minor units: $rawAmount")
    }
}
