package org.kimplify.kurrency

import org.kimplify.kurrency.extensions.normalizeAmount
import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyISOCodeStyle
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.NSNumberFormatterStyle
import platform.Foundation.commonISOCurrencyCodes
import platform.Foundation.currentLocale

/**
 * iOS currency formatter implementation.
 *
 * On iOS, the locale provided via [kurrencyLocale] is **not** used for formatting output.
 * Instead, [formattingLocale] always returns [NSLocale.currentLocale], which reflects the
 * user's actual formatting preferences from Settings > General > Language & Region.
 *
 * This is intentional: iOS separates language/region locale from formatting preferences.
 * Users can customize decimal separators, grouping separators, and currency symbol placement
 * independently of their chosen language. Using `NSLocale.currentLocale` respects these
 * customizations, whereas constructing an `NSLocale` from a language tag would only use
 * the locale's defaults and ignore user overrides.
 *
 * See also: [KurrencyLocale] class-level documentation for cross-platform locale behavior.
 */
actual class CurrencyFormatterImpl actual constructor(private val kurrencyLocale: KurrencyLocale) : CurrencyFormat {

    /** Uses the system's current locale to respect user formatting preferences (see class KDoc). */
    private val formattingLocale: NSLocale
        get() = NSLocale.currentLocale

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

    actual override fun formatCurrencyStyle(
        amount: String,
        currencyCode: String
    ): String {
        return formatCurrencyOrOriginal(amount, currencyCode, NSNumberFormatterCurrencyStyle)
    }

    actual override fun formatCompactStyle(amount: String, currencyCode: String): String {
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
        }.getOrElse { throwable ->
            KurrencyLog.w { "Compact formatting failed for $currencyCode with amount $amount: ${throwable.message}" }
            amount
        }
    }

    actual override fun formatIsoCurrencyStyle(
        amount: String,
        currencyCode: String
    ): String {
        return formatCurrencyOrOriginal(amount, currencyCode, NSNumberFormatterCurrencyISOCodeStyle)
    }

    private fun formatCurrencyOrOriginal(
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
        }.getOrElse { throwable ->
            KurrencyLog.w { "Formatting failed for $currencyCode with amount $amount: ${throwable.message}" }
            amount
        }
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
