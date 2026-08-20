package org.kimplify.kurrency

import android.icu.text.CompactDecimalFormat
import android.icu.text.DecimalFormat
import android.icu.text.NumberFormat
import android.icu.util.Currency
import org.kimplify.kurrency.extensions.normalizeAmount
import java.math.BigDecimal
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

    override fun getCurrencySymbolOrDefault(currencyCode: String, default: String): String {
        return runCatching {
            Currency.getInstance(currencyCode.uppercase()).getName(
                platformLocale,
                Currency.SYMBOL_NAME,
                booleanArrayOf(false),
            )
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
    ): String = when (style) {
        PlatformFormatStyle.SYMBOL -> format(amount, currencyCode, useIsoCode = false)
        PlatformFormatStyle.ISO_CODE -> format(amount, currencyCode, useIsoCode = true)
        PlatformFormatStyle.COMPACT -> formatCompact(amount, currencyCode)
    }

    private fun formatCompact(amount: String, currencyCode: String): String {
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
            KurrencyLog.w {
                "Compact formatting failed for $currencyCode with amount $amount, " +
                    "falling back to standard: ${throwable.message}"
            }
            format(amount, currencyCode, useIsoCode = false)
        }
    }

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

            val currency = java.util.Currency.getInstance(currencyCode.uppercase())
            requireNotNull(currency) { "Currency instance is null for code: $currencyCode" }

            val numberFormat = createNumberFormat(platformLocale, currencyCode)
            if (useIsoCode && numberFormat is DecimalFormat) {
                val symbols = numberFormat.decimalFormatSymbols
                symbols.currencySymbol = currencyCode
                numberFormat.decimalFormatSymbols = symbols
            }
            numberFormat.format(value)
        }.onFailure { throwable ->
            KurrencyLog.w { "Formatting failed for $currencyCode with amount $amount: ${throwable.message}" }
        }.getOrThrow()
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
        currency = Currency.getInstance(currencyCode.uppercase())
    }
}

private val availableCurrencyCodes: Set<String> by lazy {
    java.util.Currency.getAvailableCurrencies().mapTo(HashSet()) { it.currencyCode }
}

actual fun isValidCurrency(currencyCode: String): Boolean =
    currencyCode.uppercase() in availableCurrencyCodes
