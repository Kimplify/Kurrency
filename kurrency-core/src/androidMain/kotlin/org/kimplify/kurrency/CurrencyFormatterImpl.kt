package org.kimplify.kurrency

import android.icu.text.NumberFormat
import android.icu.util.Currency
import org.kimplify.kurrency.extensions.replaceCommaWithDot
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

    private fun formatOrOriginal(
        amount: String,
        currencyCode: String,
        style: Int
    ): String {
        return runCatching {
            val currency = Currency.getInstance(currencyCode.uppercase())

            val normalized = amount.replaceCommaWithDot().trim()
            if (normalized.isEmpty()) return amount

            val value = BigDecimal(normalized)

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
