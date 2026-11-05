package org.kimplify.kurrency.compose

import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.kimplify.kurrency.CurrencyFormatter
import org.kimplify.kurrency.KurrencyLocale

class CurrencyOutputTransformation(
    private val currencyCode: String,
    private val locale: KurrencyLocale = KurrencyLocale.systemLocale(),
    private val fractionDigits: Int = CurrencyFormatter.getFractionDigitsOrDefault(currencyCode).coerceAtLeast(0),
) : OutputTransformation {

    init {
        require(fractionDigits >= 0) { "fractionDigits must be nonnegative" }
    }

    private val formatter = CurrencyFormatter(locale)
    private val formattedZero by lazy {
        formatter.formatCurrencyStyleResult(buildAmount(""), currencyCode)
    }
    private val displayFractionDigits by lazy {
        formattedZero.getOrNull()?.count { it.isDigit() }?.minus(1)?.takeIf { it >= 0 }
    }

    override fun TextFieldBuffer.transformOutput() {
        val raw = asCharSequence().toString()
        val digits = raw.filter { it.isDigit() }
        val amount = buildAmount(digits)
        val result = if (digits.isEmpty()) formattedZero else formatter.formatCurrencyStyleResult(amount, currencyCode)
        val formatted = result.getOrDefault(amount)
        val outputScale = if (result.isSuccess) displayFractionDigits else fractionDigits

        if (outputScale == null) {
            replaceOutputRange(0, length, formatted)
        } else {
            applyCurrencyFormatting(raw, formatted, fractionDigits, outputScale)
        }
    }

    private fun buildAmount(digits: String): String {
        if (fractionDigits == 0) return digits.ifEmpty { "0" }
        val integerLength = digits.length - fractionDigits
        return if (integerLength > 0) {
            digits.take(integerLength) + "." + digits.drop(integerLength)
        } else {
            "0." + "0".repeat(-integerLength) + digits
        }
    }
}

private fun TextFieldBuffer.applyCurrencyFormatting(
    raw: String,
    formatted: String,
    inputScale: Int,
    outputScale: Int,
) {
    val sourceDigits = raw.indices.filter { raw[it].isDigit() }
    val outputDigits = formatted.indices.filter { formatted[it].isDigit() }
    val sourceExponent = sourceDigits.lastIndex - inputScale
    val outputExponent = outputDigits.lastIndex - outputScale
    val firstExponent = maxOf(-inputScale, -outputScale)
    val lastExponent = minOf(sourceExponent, outputExponent)
    var sourceEnd = raw.length
    var outputEnd = formatted.length

    for (exponent in firstExponent..lastExponent) {
        val sourceIndex = sourceDigits[sourceExponent - exponent]
        val outputIndex = outputDigits[outputExponent - exponent]
        replaceOutputRange(sourceIndex + 1, sourceEnd, formatted.substring(outputIndex + 1, outputEnd))
        replaceOutputRange(sourceIndex, sourceIndex + 1, formatted[outputIndex].toString())
        sourceEnd = sourceIndex
        outputEnd = outputIndex
    }
    replaceOutputRange(0, sourceEnd, formatted.substring(0, outputEnd))
}

private fun TextFieldBuffer.replaceOutputRange(start: Int, end: Int, replacement: String) {
    if (asCharSequence().subSequence(start, end).toString() != replacement) {
        replace(start, end, replacement)
    }
}

@Composable
fun rememberCurrencyOutputTransformation(
    currencyCode: String,
    locale: KurrencyLocale = KurrencyLocale.current(),
    fractionDigits: Int = CurrencyFormatter.getFractionDigitsOrDefault(currencyCode).coerceAtLeast(0),
): CurrencyOutputTransformation = remember(currencyCode, locale, fractionDigits) {
    CurrencyOutputTransformation(currencyCode, locale, fractionDigits)
}
