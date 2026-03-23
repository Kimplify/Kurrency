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
 * iOS implementation of [CurrencyFormat].
 *
 * ## iOS Locale vs Formatting Locale
 *
 * On iOS, the "locale" and "formatting locale" are fundamentally different concepts:
 *
 * - **Locale (language/region):** Determines language, region, and default formatting rules.
 *   This is what [kurrencyLocale] represents (e.g., "en-US", "de-DE").
 *
 * - **Formatting locale ([NSLocale.currentLocale]):** Reflects the user's **custom** formatting
 *   preferences configured in iOS Settings > General > Language & Region. Users can override
 *   decimal separators, grouping separators, and other formatting details independently of
 *   their chosen language/region. For example, a user with "en-US" locale can set comma as
 *   their decimal separator.
 *
 * This implementation intentionally uses [NSLocale.currentLocale] for all formatting operations
 * to respect the user's custom preferences. The provided [kurrencyLocale] is **not** used for
 * formatting — it determines the currency code context only. This matches Apple's recommended
 * behavior: always format numbers and currencies using [NSLocale.currentLocale] so the output
 * aligns with what the user expects to see based on their personal settings.
 *
 * This differs from Android/JVM/Web where the provided locale directly controls formatting,
 * because those platforms do not have a separate user-customizable formatting layer.
 */
actual class CurrencyFormatterImpl actual constructor(private val kurrencyLocale: KurrencyLocale) : CurrencyFormat {

    /**
     * The formatting locale used for all number/currency formatting on iOS.
     *
     * Always returns [NSLocale.currentLocale] to respect the user's custom formatting
     * preferences (decimal separator, grouping separator, etc.) configured in iOS Settings.
     *
     * Note: This intentionally ignores [kurrencyLocale]. See class-level documentation
     * for the rationale on iOS locale vs formatting locale.
     */
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
