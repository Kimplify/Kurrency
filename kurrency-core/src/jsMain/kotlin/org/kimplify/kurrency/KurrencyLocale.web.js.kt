package org.kimplify.kurrency

external object Intl {
    class NumberFormat(locales: String? = definedExternally, options: dynamic = definedExternally) {
        fun format(number: Number): String
    }
}

internal actual fun getBrowserLocale(): String {
    return js("navigator.language || 'en-US'") as String
}

internal actual fun getDecimalSeparatorForLocale(locale: String): String {
    val formatter = Intl.NumberFormat(locale)
    val formatted = formatter.format(1.1)
    return formatted.replace(Regex("[0-9]"), "").firstOrNull()?.toString() ?: "."
}

internal actual fun getGroupingSeparatorForLocale(locale: String): String {
    val formatter = Intl.NumberFormat(locale)
    val formatted = formatter.format(1111)
    return formatted.replace(Regex("[0-9]"), "").firstOrNull()?.toString() ?: ","
}
