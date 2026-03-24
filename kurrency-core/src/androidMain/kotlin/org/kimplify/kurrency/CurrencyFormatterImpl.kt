package org.kimplify.kurrency

import android.icu.text.CompactDecimalFormat
import android.icu.util.Currency
import org.kimplify.kurrency.extensions.normalizeAmount
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

actual class CurrencyFormatterImpl actual constructor(kurrencyLocale: KurrencyLocale) : CurrencyFormat {

    private val platformLocale: Locale = kurrencyLocale.locale

    actual override fun getFractionDigitsOrDefault(currencyCode: String, default: Int): Int {
        return runCatching {
            val currency = java.util.Currency.getInstance(currencyCode.uppercase())
            requireNotNull(currency) { "Currency instance is null for code: $currencyCode" }
            currency.defaultFractionDigits
        }.getOrElse { throwable ->
            KurrencyLog.w { "Failed to get fraction digits for $currencyCode: ${throwable.message}" }
            default
        }
    }

    actual override fun formatCurrencyStyle(
        amount: String,
        currencyCode: String
    ): String {
        return formatCurrencyOrOriginal(amount, currencyCode, useIsoCode = false)
    }

    actual override fun formatIsoCurrencyStyle(
        amount: String,
        currencyCode: String
    ): String {
        return formatCurrencyOrOriginal(amount, currencyCode, useIsoCode = true)
    }

    actual override fun formatCompactStyle(amount: String, currencyCode: String): String {
        return runCatching {
            val currency = Currency.getInstance(currencyCode.uppercase())
            val normalized = amount.normalizeAmount().trim()
            if (normalized.isEmpty()) return amount

            val value = BigDecimal(normalized)
            require(value.toDouble().isFinite()) { "Amount must be a finite number" }

            val compactFormat = CompactDecimalFormat.getInstance(
                platformLocale,
                CompactDecimalFormat.CompactStyle.SHORT,
            )
            compactFormat.currency = currency
            compactFormat.format(value.toDouble())
        }.getOrElse { throwable ->
            KurrencyLog.w { "Compact formatting failed for $currencyCode with amount $amount: ${throwable.message}" }
            formatCurrencyStyle(amount, currencyCode)
        }
    }

    private fun formatCurrencyOrOriginal(
        amount: String,
        currencyCode: String,
        useIsoCode: Boolean
    ): String {
        return runCatching {
            val normalizedAmount = amount.normalizeAmount().trim()
            if (normalizedAmount.isEmpty()) return amount

            val value = normalizedAmount.toDouble()
            require(value.isFinite()) { "Amount must be a finite number" }

            val currency = java.util.Currency.getInstance(currencyCode.uppercase())
            requireNotNull(currency) { "Currency instance is null for code: $currencyCode" }

            val numberFormat = createNumberFormat(platformLocale, currencyCode)
            if (useIsoCode && numberFormat is DecimalFormat) {
                val symbols = numberFormat.decimalFormatSymbols
                symbols.currencySymbol = currencyCode
                numberFormat.decimalFormatSymbols = symbols
            }
            numberFormat.format(value)
        }.getOrElse { throwable ->
            KurrencyLog.w { "Formatting failed for $currencyCode with amount $amount: ${throwable.message}" }
            amount
        }
    }

    actual override fun parseCurrencyAmount(formattedText: String, currencyCode: String): Double? {
        return runCatching {
            val numberFormat = createNumberFormat(platformLocale, currencyCode)
            numberFormat.parse(formattedText)?.toDouble()
        }.getOrNull()
    }

    private fun createNumberFormat(
        locale: Locale,
        currencyCode: String
    ): NumberFormat = NumberFormat.getCurrencyInstance(locale).apply {
        currency = java.util.Currency.getInstance(currencyCode)
    }
}

actual fun isValidCurrency(currencyCode: String): Boolean =
    runCatching {
        java.util.Currency.getInstance(currencyCode.uppercase()) != null
    }.getOrDefault(false)
