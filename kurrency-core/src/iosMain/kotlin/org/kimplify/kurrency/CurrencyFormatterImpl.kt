package org.kimplify.kurrency

import org.kimplify.kurrency.extensions.replaceCommaWithDot
import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyISOCodeStyle
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.NSNumberFormatterStyle
import platform.Foundation.commonISOCurrencyCodes
import platform.Foundation.currentLocale

actual class CurrencyFormatterImpl actual constructor(private val kurrencyLocale: KurrencyLocale) : CurrencyFormat {

    /**
     * On iOS, we use NSLocale.currentLocale for formatting to respect user's
     * custom formatting preferences (decimal/grouping separators).
     *
     * The kurrencyLocale is kept for potential language/region context validation.
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
            val normalizedAmount = amount.replaceCommaWithDot().trim()
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
