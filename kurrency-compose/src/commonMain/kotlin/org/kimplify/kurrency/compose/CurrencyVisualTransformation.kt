package org.kimplify.kurrency.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import org.kimplify.kurrency.CurrencyFormatter
import org.kimplify.kurrency.KurrencyLocale

class CurrencyVisualTransformation(
    private val currencyCode: String,
    private val locale: KurrencyLocale = KurrencyLocale.systemLocale(),
    private val fractionDigits: Int = CurrencyFormatter.getFractionDigitsOrDefault(currencyCode),
) : VisualTransformation {

    private val formatter = CurrencyFormatter(locale)

    override fun filter(text: AnnotatedString): TransformedText {
        val digitsOnly = text.text.filter { it.isDigit() }

        if (digitsOnly.isEmpty()) {
            val zeroAmount = buildZeroAmount()
            val formatted = formatter.formatCurrencyStyle(zeroAmount, currencyCode)
            return TransformedText(
                AnnotatedString(formatted),
                ZeroOffsetMapping(formatted.length),
            )
        }

        val amount = insertDecimalPoint(digitsOnly)
        val formatted = formatter.formatCurrencyStyle(amount, currencyCode)
        val digitPositionsInFormatted = buildDigitPositionMap(formatted)

        return TransformedText(
            AnnotatedString(formatted),
            CurrencyOffsetMapping(digitsOnly.length, formatted.length, digitPositionsInFormatted),
        )
    }

    private fun insertDecimalPoint(digits: String): String {
        if (fractionDigits == 0) return digits

        val padded = digits.padStart(fractionDigits + 1, '0')
        val integerPart = padded.substring(0, padded.length - fractionDigits)
        val decimalPart = padded.substring(padded.length - fractionDigits)
        return "$integerPart.$decimalPart"
    }

    private fun buildZeroAmount(): String {
        return if (fractionDigits == 0) "0"
        else "0." + "0".repeat(fractionDigits)
    }

    private fun buildDigitPositionMap(formatted: String): List<Int> {
        val positions = mutableListOf<Int>()
        for (i in formatted.indices) {
            if (formatted[i].isDigit()) {
                positions.add(i)
            }
        }
        return positions
    }
}

/**
 * Returns true if the character is a Unicode bidirectional mark.
 * These invisible characters appear in RTL-formatted currency strings
 * and should be skipped during offset mapping.
 */
private fun isBidiMark(c: Char): Boolean =
    c == '\u200F' || c == '\u200E' || c == '\u061C' || c == '\u200B'

private class ZeroOffsetMapping(private val formattedLength: Int) : OffsetMapping {
    override fun originalToTransformed(offset: Int): Int = formattedLength
    override fun transformedToOriginal(offset: Int): Int = 0
}

private class CurrencyOffsetMapping(
    private val originalLength: Int,
    private val formattedLength: Int,
    private val digitPositions: List<Int>,
) : OffsetMapping {

    override fun originalToTransformed(offset: Int): Int {
        if (digitPositions.isEmpty()) return 0
        val clampedOffset = offset.coerceIn(0, originalLength)
        if (clampedOffset == 0) return digitPositions[0].coerceIn(0, formattedLength)
        if (clampedOffset >= digitPositions.size) return formattedLength
        // For offset N (> 0), place the cursor after the Nth digit
        val posAfterDigit = digitPositions[clampedOffset - 1] + 1
        return posAfterDigit.coerceIn(0, formattedLength)
    }

    override fun transformedToOriginal(offset: Int): Int {
        if (digitPositions.isEmpty()) return 0
        var count = 0
        for (pos in digitPositions) {
            if (pos >= offset) return count
            count++
        }
        return originalLength
    }
}

@Composable
fun rememberCurrencyVisualTransformation(
    currencyCode: String,
    locale: KurrencyLocale = KurrencyLocale.current(),
): CurrencyVisualTransformation {
    return remember(currencyCode, locale) {
        CurrencyVisualTransformation(currencyCode, locale)
    }
}
