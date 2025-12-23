package org.kimplify.kurrency

import org.kimplify.kurrency.extensions.replaceCommaWithDot

@JsName("Intl")
private external object IntlCurrency {
    class NumberFormat(locales: String? = definedExternally, options: dynamic = definedExternally) {
        fun format(number: Number): String
        fun resolvedOptions(): dynamic
    }

    fun supportedValuesOf(key: String): Array<String>
}

private fun jsGetMaxFractionDigits(cur: String, loc: String?): Int {
    val options = js("({ style: 'currency', currency: cur })")
    val formatter = IntlCurrency.NumberFormat(loc, options)
    val resolved = formatter.resolvedOptions()
    return resolved.maximumFractionDigits as Int
}

private fun jsGetResolvedCurrency(cur: String, loc: String?): String {
    val options = js("({ style: 'currency', currency: cur })")
    val formatter = IntlCurrency.NumberFormat(loc, options)
    val resolved = formatter.resolvedOptions()
    return resolved.currency as String
}

private fun jsFormatSymbol(amt: String, cur: String, loc: String?): String {
    val options = js("({ style: 'currency', currency: cur })")
    val formatter = IntlCurrency.NumberFormat(loc, options)
    return formatter.format(amt.toDouble())
}

private fun jsFormatIso(amt: String, cur: String, loc: String?): String {
    val options = js("({ style: 'currency', currency: cur, currencyDisplay: 'code' })")
    val formatter = IntlCurrency.NumberFormat(loc, options)
    return formatter.format(amt.toDouble())
}

private fun jsIsSupportedCurrency(cur: String): Boolean? {
    return try {
        val supportedValuesOf: dynamic = js("Intl.supportedValuesOf")
        if (js("typeof supportedValuesOf === 'function'") as Boolean) {
            val currencies = IntlCurrency.supportedValuesOf("currency")
            return currencies.contains(cur)
        }
        null
    } catch (e: Throwable) {
        null
    }
}

private fun jsCanCreateCurrencyFormatter(cur: String): Boolean {
    return try {
        val options = js("({ style: 'currency', currency: cur })")
        IntlCurrency.NumberFormat(null, options)
        true
    } catch (e: Throwable) {
        false
    }
}

actual class CurrencyFormatterImpl actual constructor(
    kurrencyLocale: KurrencyLocale
) : CurrencyFormat {

    private val locale: String? = kurrencyLocale?.languageTag

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
