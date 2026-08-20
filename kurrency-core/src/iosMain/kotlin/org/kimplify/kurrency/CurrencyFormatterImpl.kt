package org.kimplify.kurrency

import org.kimplify.kurrency.extensions.normalizeAmount
import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyISOCodeStyle
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.NSNumberFormatterStyle
import platform.Foundation.commonISOCurrencyCodes

/**
 * iOS currency formatter implementation.
 *
 * Uses the locale provided via [kurrencyLocale] for formatting output,
 * consistent with JVM, Android, and Web platform behavior.
 */
actual class CurrencyFormatterImpl actual constructor(private val kurrencyLocale: KurrencyLocale) : CurrencyFormat {

    private val formattingLocale: NSLocale = kurrencyLocale.nsLocale

    override fun getCurrencySymbolOrDefault(currencyCode: String, default: String): String {
        return runCatching {
            val formatter = NSNumberFormatter().apply {
                this.locale = formattingLocale
                this.currencyCode = currencyCode.uppercase()
                this.numberStyle = NSNumberFormatterCurrencyStyle
            }
            formatter.currencySymbol ?: default
        }.getOrElse { throwable ->
            KurrencyLog.w { "Failed to get symbol for $currencyCode: ${throwable.message}" }
            default
        }
    }

    actual override fun getFractionDigitsOrDefault(currencyCode: String, default: Int): Int {
        return runCatching {
            val formatter = NSNumberFormatter().apply {
                this.currencyCode = currencyCode
                this.numberStyle = NSNumberFormatterCurrencyStyle
            }
            val fractionDigits = formatter.maximumFractionDigits.toInt()
            if (fractionDigits >= 0) fractionDigits else default
        }.getOrElse { throwable ->
            KurrencyLog.w { "Failed to get fraction digits for $currencyCode: ${throwable.message}" }
            default
        }
    }

    actual override fun formatCurrencyStyle(amount: String, currencyCode: String): String =
        formatLeniently(amount, currencyCode) {
            formatOrThrow(amount, currencyCode, PlatformFormatStyle.SYMBOL)
        }

    actual override fun formatIsoCurrencyStyle(amount: String, currencyCode: String): String =
        formatLeniently(amount, currencyCode) {
            formatOrThrow(amount, currencyCode, PlatformFormatStyle.ISO_CODE)
        }

    actual override fun formatCompactStyle(amount: String, currencyCode: String): String =
        formatLeniently(amount, currencyCode) {
            formatOrThrow(amount, currencyCode, PlatformFormatStyle.COMPACT)
        }

    internal actual fun formatOrThrow(
        amount: String,
        currencyCode: String,
        style: PlatformFormatStyle,
    ): String = when (style) {
        PlatformFormatStyle.SYMBOL -> format(amount, currencyCode, NSNumberFormatterCurrencyStyle)
        PlatformFormatStyle.ISO_CODE ->
            format(amount, currencyCode, NSNumberFormatterCurrencyISOCodeStyle)
        PlatformFormatStyle.COMPACT -> formatCompact(amount, currencyCode)
    }

    private fun formatCompact(amount: String, currencyCode: String): String {
        return runCatching {
            val normalizedAmount = amount.normalizeAmount().trim()
            if (normalizedAmount.isEmpty()) return amount

            val doubleValue = normalizedAmount.toDouble()
            require(doubleValue.isFinite()) { "Amount must be a finite number" }

            val absValue = kotlin.math.abs(doubleValue)
            val (divisor, suffix) = when {
                absValue >= 1_000_000_000 -> 1_000_000_000.0 to "B"
                absValue >= 1_000_000 -> 1_000_000.0 to "M"
                absValue >= 1_000 -> 1_000.0 to "K"
                else -> 1.0 to ""
            }

            val scaledValue = doubleValue / divisor
            val value = NSNumber(scaledValue)
            val numberFormatter = createNumberFormatter(currencyCode, NSNumberFormatterCurrencyStyle)
            val formatted = numberFormatter.stringFromNumber(value) ?: return amount
            if (suffix.isEmpty()) return formatted

            // Insert suffix after the last digit to keep it adjacent to the number,
            // preserving correct placement in locales with trailing currency symbols (e.g., "1,23K €")
            val lastDigitIndex = formatted.indexOfLast { it.isDigit() }
            if (lastDigitIndex < 0) return "$formatted$suffix"
            formatted.substring(0, lastDigitIndex + 1) + suffix + formatted.substring(lastDigitIndex + 1)
        }.onFailure { throwable ->
            KurrencyLog.w { "Compact formatting failed for $currencyCode with amount $amount: ${throwable.message}" }
        }.getOrThrow()
    }

    private fun format(
        amount: String,
        currencyCode: String,
        style: NSNumberFormatterStyle
    ): String {
        return runCatching {
            val normalizedAmount = amount.normalizeAmount().trim()
            if (normalizedAmount.isEmpty()) return amount

            val doubleValue = normalizedAmount.toDouble()
            require(doubleValue.isFinite()) { "Amount must be a finite number" }

            val value = NSNumber(doubleValue)
            val numberFormatter = createNumberFormatter(currencyCode, style)
            numberFormatter.stringFromNumber(value) ?: ""
        }.onFailure { throwable ->
            KurrencyLog.w { "Formatting failed for $currencyCode with amount $amount: ${throwable.message}" }
        }.getOrThrow()
    }

    actual override fun parseCurrencyAmount(formattedText: String, currencyCode: String): Double? {
        return runCatching {
            val formatter = createNumberFormatter(currencyCode, NSNumberFormatterCurrencyStyle)
            formatter.numberFromString(formattedText)?.doubleValue
        }.getOrNull()
    }

    private fun createNumberFormatter(
        currencyCode: String,
        style: NSNumberFormatterStyle
    ): NSNumberFormatter = NSNumberFormatter().apply {
        this.numberStyle = style
        this.locale = formattingLocale
        this.currencyCode = currencyCode
    }
}

actual fun isValidCurrency(currencyCode: String): Boolean {
    val upperCode = currencyCode.uppercase()
    return NSLocale.commonISOCurrencyCodes.contains(upperCode)
}
