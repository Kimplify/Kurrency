package org.kimplify.kurrency

@JsName("Intl")
private external object IntlCurrency {
    class NumberFormat(locales: String? = definedExternally, options: dynamic = definedExternally) {
        fun format(number: Number): String
        fun resolvedOptions(): dynamic
    }

    fun supportedValuesOf(key: String): Array<String>
}

internal actual fun webGetMaxFractionDigits(cur: String, loc: String?): Int {
    val options = js("({ style: 'currency', currency: cur })")
    val formatter = IntlCurrency.NumberFormat(loc, options)
    val resolved = formatter.resolvedOptions()
    return resolved.maximumFractionDigits as Int
}

internal actual fun webGetResolvedCurrency(cur: String, loc: String?): String {
    val options = js("({ style: 'currency', currency: cur })")
    val formatter = IntlCurrency.NumberFormat(loc, options)
    val resolved = formatter.resolvedOptions()
    return resolved.currency as String
}

internal actual fun webFormatSymbol(amt: String, cur: String, loc: String?): String {
    val options = js("({ style: 'currency', currency: cur })")
    val formatter = IntlCurrency.NumberFormat(loc, options)
    return formatter.format(amt.toDouble())
}

internal actual fun webFormatIso(amt: String, cur: String, loc: String?): String {
    val options = js("({ style: 'currency', currency: cur, currencyDisplay: 'code' })")
    val formatter = IntlCurrency.NumberFormat(loc, options)
    return formatter.format(amt.toDouble())
}

internal actual fun webIsSupportedCurrency(cur: String): Boolean? {
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

internal actual fun webCanCreateCurrencyFormatter(cur: String): Boolean {
    return try {
        val options = js("({ style: 'currency', currency: cur })")
        IntlCurrency.NumberFormat(null, options)
        true
    } catch (e: Throwable) {
        false
    }
}

internal actual fun webFormatCompact(amt: String, cur: String, loc: String?): String {
    val options = js("({ style: 'currency', currency: cur, notation: 'compact' })")
    val formatter = IntlCurrency.NumberFormat(loc, options)
    return formatter.format(amt.toDouble())
}
