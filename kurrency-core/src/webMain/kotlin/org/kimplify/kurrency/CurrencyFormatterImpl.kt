package org.kimplify.kurrency

import org.kimplify.kurrency.extensions.normalizeAmount

internal expect fun webGetMaxFractionDigits(cur: String, loc: String?): Int
internal expect fun webGetResolvedCurrency(cur: String, loc: String?): String
internal expect fun webFormatSymbol(amt: String, cur: String, loc: String?): String
internal expect fun webFormatIso(amt: String, cur: String, loc: String?): String
internal expect fun webIsSupportedCurrency(cur: String): Boolean?
internal expect fun webCanCreateCurrencyFormatter(cur: String): Boolean
internal expect fun webFormatCompact(amt: String, cur: String, loc: String?): String

actual class CurrencyFormatterImpl actual constructor(
    kurrencyLocale: KurrencyLocale,
) : CurrencyFormat {

    private val locale: String = kurrencyLocale.languageTag

    actual override fun getFractionDigitsOrDefault(currencyCode: String, default: Int): Int {
        return runCatching {
            val upperCode = currencyCode.uppercase()
            val resolvedCurrency = webGetResolvedCurrency(upperCode, locale)
            require(resolvedCurrency == upperCode) {
                "Invalid currency code: $currencyCode (resolved to: $resolvedCurrency)"
            }
            val fractionDigits = webGetMaxFractionDigits(upperCode, locale)
            if (fractionDigits >= 0) fractionDigits else default
        }.getOrElse { throwable ->
            KurrencyLog.w { "Failed to get fraction digits for $currencyCode: ${throwable.message}" }
            default
        }
    }

    actual override fun formatCurrencyStyle(amount: String, currencyCode: String): String {
        return runCatching {
            val normalizedAmount = amount.normalizeAmount().trim()
            if (normalizedAmount.isEmpty()) return amount

            val doubleValue = normalizedAmount.toDouble()
            require(doubleValue.isFinite()) { "Amount must be a finite number" }
            webFormatSymbol(normalizedAmount, currencyCode, locale)
        }.getOrElse { throwable ->
            KurrencyLog.w { "Formatting failed for $currencyCode with amount $amount: ${throwable.message}" }
            amount
        }
    }

    actual override fun formatIsoCurrencyStyle(amount: String, currencyCode: String): String {
        return runCatching {
            val normalizedAmount = amount.normalizeAmount().trim()
            if (normalizedAmount.isEmpty()) return amount

            val doubleValue = normalizedAmount.toDouble()
            require(doubleValue.isFinite()) { "Amount must be a finite number" }
            webFormatIso(normalizedAmount, currencyCode, locale)
        }.getOrElse { throwable ->
            KurrencyLog.w { "Formatting failed for $currencyCode with amount $amount: ${throwable.message}" }
            amount
        }
    }

    actual override fun formatCompactStyle(amount: String, currencyCode: String): String {
        return runCatching {
            val normalizedAmount = amount.normalizeAmount().trim()
            if (normalizedAmount.isEmpty()) return amount

            val doubleValue = normalizedAmount.toDouble()
            require(doubleValue.isFinite()) { "Amount must be a finite number" }
            webFormatCompact(normalizedAmount, currencyCode, locale)
        }.getOrElse { throwable ->
            KurrencyLog.w { "Compact formatting failed for $currencyCode with amount $amount: ${throwable.message}" }
            amount
        }
    }

    actual override fun parseCurrencyAmount(formattedText: String, currencyCode: String): Double? {
        return runCatching {
            val cleaned = formattedText
                .replace(Regex("[^0-9.,\\-]"), "")
                .normalizeAmount()
            cleaned.toDoubleOrNull()
        }.getOrNull()
    }
}

actual fun isValidCurrency(currencyCode: String): Boolean =
    runCatching {
        val upperCode = currencyCode.uppercase()

        val isSupported = webIsSupportedCurrency(upperCode)
        if (isSupported != null) {
            return isSupported
        }

        if (!webCanCreateCurrencyFormatter(upperCode)) {
            return false
        }

        val resolvedCurrency = webGetResolvedCurrency(upperCode, null)
        resolvedCurrency == upperCode
    }.getOrDefault(false)
