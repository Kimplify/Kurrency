package org.kimplify.kurrency

import org.kimplify.kurrency.extensions.normalizeAmount
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

actual class CurrencyFormatterImpl actual constructor(
    kurrencyLocale: KurrencyLocale
) : CurrencyFormat {

    private val locale: Locale = kurrencyLocale.locale

    actual override fun getFractionDigitsOrDefault(currencyCode: String, default: Int): Int {
        return runCatching {
            val currency = Currency.getInstance(currencyCode.uppercase())
            requireNotNull(currency) { "Currency instance is null for code: $currencyCode" }
            currency.defaultFractionDigits
        }.getOrElse { throwable ->
            KurrencyLog.w { "Failed to get fraction digits for $currencyCode: ${throwable.message}" }
            default
        }
    }

    override fun getCurrencySymbolOrDefault(currencyCode: String, default: String): String {
        return runCatching {
            Currency.getInstance(currencyCode.uppercase()).getSymbol(locale)
        }.getOrElse { throwable ->
            KurrencyLog.w { "Failed to get symbol for $currencyCode: ${throwable.message}" }
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
    ): String = format(amount, currencyCode, useIsoCode = style == PlatformFormatStyle.ISO_CODE)

    private fun format(
        amount: String,
        currencyCode: String,
        useIsoCode: Boolean
    ): String {
        return runCatching {
            val normalizedAmount = amount.normalizeAmount().trim()
            if (normalizedAmount.isEmpty()) return amount

            val value = normalizedAmount.toDouble()
            require(value.isFinite()) { "Amount must be a finite number" }

            val currency = Currency.getInstance(currencyCode.uppercase())
            requireNotNull(currency) { "Currency instance is null for code: $currencyCode" }

            if (useIsoCode) {
                val numberFormat = createNumberFormat(locale, currencyCode)
                if (numberFormat is DecimalFormat) {
                    val symbols = numberFormat.decimalFormatSymbols
                    symbols.currencySymbol = currencyCode
                    numberFormat.decimalFormatSymbols = symbols
                }
                numberFormat.format(value)
            } else {
                val numberFormat = createNumberFormat(locale, currencyCode)
                numberFormat.format(value) ?: ""
            }
        }.onFailure { throwable ->
            KurrencyLog.w { "Formatting failed for $currencyCode with amount $amount: ${throwable.message}" }
        }.getOrThrow()
    }

    actual override fun parseCurrencyAmount(formattedText: String, currencyCode: String): Double? {
        return runCatching {
            val numberFormat = createNumberFormat(locale, currencyCode)
            numberFormat.parse(formattedText)?.toDouble()
        }.getOrNull()
    }

    private fun createNumberFormat(
        locale: Locale,
        currencyCode: String
    ): NumberFormat = NumberFormat.getCurrencyInstance(locale).apply {
        currency = Currency.getInstance(currencyCode.uppercase())
        val digits = currency.defaultFractionDigits.coerceAtLeast(0)
        minimumFractionDigits = digits
        maximumFractionDigits = digits
    }
}

actual fun isValidCurrency(currencyCode: String): Boolean =
    runCatching {
        val currency = Currency.getInstance(currencyCode.uppercase())
        currency != null
    }.getOrDefault(false)
