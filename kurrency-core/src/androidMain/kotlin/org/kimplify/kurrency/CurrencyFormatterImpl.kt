package org.kimplify.kurrency

import android.icu.text.CompactDecimalFormat
import android.icu.text.NumberFormat
import android.icu.util.Currency
import org.kimplify.kurrency.extensions.normalizeAmount
import java.math.BigDecimal
import java.util.Locale

actual class CurrencyFormatterImpl actual constructor(kurrencyLocale: KurrencyLocale) : CurrencyFormat {

    private val platformLocale: Locale = kurrencyLocale.locale

    actual override fun getFractionDigitsOrDefault(currencyCode: String, default: Int): Int {
        return runCatching {
            val currency = Currency.getInstance(currencyCode.uppercase())
            val fractionDigits = currency.defaultFractionDigits
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
        return formatOrOriginal(amount, currencyCode, NumberFormat.CURRENCYSTYLE)
    }

    actual override fun formatIsoCurrencyStyle(
        amount: String,
        currencyCode: String
    ): String {
        return formatOrOriginal(amount, currencyCode, NumberFormat.ISOCURRENCYSTYLE)
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
            amount
        }
    }

    private fun formatOrOriginal(
        amount: String,
        currencyCode: String,
        style: Int
    ): String {
        return runCatching {
            val currency = Currency.getInstance(currencyCode.uppercase())

            val normalized = amount.normalizeAmount().trim()
            if (normalized.isEmpty()) return amount

            val value = BigDecimal(normalized)
            require(value.toDouble().isFinite()) { "Amount must be a finite number" }

            val numberFormat = NumberFormat.getInstance(platformLocale, style).apply {
                this.currency = currency
                val fractionDigits = currency.defaultFractionDigits
                if (fractionDigits >= 0) {
                    minimumFractionDigits = fractionDigits
                    maximumFractionDigits = fractionDigits
                }
            }

            numberFormat.format(value)
        }.getOrElse { throwable ->
            KurrencyLog.w { "Formatting failed for $currencyCode with amount $amount: ${throwable.message}" }
            amount
        }
    }

    actual override fun parseCurrencyAmount(formattedText: String, currencyCode: String): Double? {
        return runCatching {
            val currency = Currency.getInstance(currencyCode.uppercase())
            val numberFormat = NumberFormat.getInstance(platformLocale, NumberFormat.CURRENCYSTYLE).apply {
                this.currency = currency
            }
            numberFormat.parse(formattedText)?.toDouble()
        }.getOrNull()
    }
}

actual fun isValidCurrency(currencyCode: String): Boolean {
    if (currencyCode.length != 3 || !currencyCode.all { it.isLetter() }) {
        return false
    }

    val upperCode = currencyCode.uppercase()
    return runCatching {
        val availableCurrencies = Currency.getAvailableCurrencies()
        availableCurrencies.any { it.currencyCode == upperCode }
    }.getOrDefault(false)
}
