@file:OptIn(ExperimentalWasmJsInterop::class)

package org.kimplify.kurrency

import org.kimplify.kurrency.extensions.replaceCommaWithDot

@JsFun("function(cur, loc) { return new Intl.NumberFormat(loc || undefined, {style:'currency', currency:cur}).resolvedOptions().maximumFractionDigits; }")
private external fun jsGetMaxFractionDigits(cur: String, loc: String?): Int

@JsFun("function(cur, loc) { return new Intl.NumberFormat(loc || undefined, {style:'currency', currency:cur}).resolvedOptions().currency; }")
private external fun jsGetResolvedCurrency(cur: String, loc: String?): String

@JsFun("function(amt, cur, loc) { return new Intl.NumberFormat(loc || undefined, {style:'currency', currency:cur}).format(+amt); }")
private external fun jsFormatSymbol(amt: String, cur: String, loc: String?): String

@JsFun("function(amt, cur, loc) { return new Intl.NumberFormat(loc || undefined, {style:'currency', currency:cur, currencyDisplay:'code'}).format(+amt); }")
private external fun jsFormatIso(amt: String, cur: String, loc: String?): String

@JsFun("function(cur) { try { if (typeof Intl.supportedValuesOf === 'function') { return Intl.supportedValuesOf('currency').includes(cur); } return null; } catch(e) { return null; } }")
private external fun jsIsSupportedCurrency(cur: String): Boolean?

@JsFun("function(cur) { try { new Intl.NumberFormat(undefined, {style:'currency', currency:cur}); return true; } catch(e) { return false; } }")
private external fun jsCanCreateCurrencyFormatter(cur: String): Boolean

actual class CurrencyFormatterImpl actual constructor(kurrencyLocale: KurrencyLocale) :
    CurrencyFormat {

    private val locale: String = kurrencyLocale.languageTag

    actual override fun getFractionDigitsOrDefault(currencyCode: String, default: Int): Int {
        return runCatching {
            val upperCode = currencyCode.uppercase()
            val resolvedCurrency = jsGetResolvedCurrency(upperCode, locale)
            require(resolvedCurrency == upperCode) {
                "Invalid currency code: $currencyCode (resolved to: $resolvedCurrency)"
            }
            val fractionDigits = jsGetMaxFractionDigits(upperCode, locale)
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
        return runCatching {
            val normalizedAmount = amount.replaceCommaWithDot().trim()
            if (normalizedAmount.isEmpty()) return amount

            val doubleValue = normalizedAmount.toDouble()
            require(doubleValue.isFinite()) { "Amount must be a finite number" }
            jsFormatSymbol(normalizedAmount, currencyCode, locale)
        }.getOrElse { throwable ->
            KurrencyLog.w { "Formatting failed for $currencyCode with amount $amount: ${throwable.message}" }
            amount
        }
    }

    actual override fun formatIsoCurrencyStyle(
        amount: String,
        currencyCode: String
    ): String {
        return runCatching {
            val normalizedAmount = amount.replaceCommaWithDot().trim()
            if (normalizedAmount.isEmpty()) return amount

            val doubleValue = normalizedAmount.toDouble()
            require(doubleValue.isFinite()) { "Amount must be a finite number" }
            jsFormatIso(normalizedAmount, currencyCode, locale)
        }.getOrElse { throwable ->
            KurrencyLog.w { "Formatting failed for $currencyCode with amount $amount: ${throwable.message}" }
            amount
        }
    }
}

actual fun isValidCurrency(currencyCode: String): Boolean =
    runCatching {
        val upperCode = currencyCode.uppercase()

        val isSupported = jsIsSupportedCurrency(upperCode)
        if (isSupported != null) {
            return isSupported
        }

        if (!jsCanCreateCurrencyFormatter(upperCode)) {
            return false
        }

        val resolvedCurrency = jsGetResolvedCurrency(upperCode, null)
        resolvedCurrency == upperCode
    }.getOrDefault(false)
