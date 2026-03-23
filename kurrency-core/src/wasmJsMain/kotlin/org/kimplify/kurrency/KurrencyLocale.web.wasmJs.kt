package org.kimplify.kurrency

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("function() { return navigator.language || 'en-US'; }")
private external fun getBrowserLocaleInternal(): String

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("function(locale) { const formatted = new Intl.NumberFormat(locale).format(1.1); return formatted.replace(/[0-9]/g, '')[0] || '.'; }")
private external fun getDecimalSeparatorForLocaleInternal(locale: String): String

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("function(locale) { const formatted = new Intl.NumberFormat(locale).format(1111); return formatted.replace(/[0-9]/g, '')[0] || ','; }")
private external fun getGroupingSeparatorForLocaleInternal(locale: String): String

internal actual fun getBrowserLocale(): String {
    return getBrowserLocaleInternal()
}

internal actual fun getDecimalSeparatorForLocale(locale: String): String {
    return getDecimalSeparatorForLocaleInternal(locale)
}

internal actual fun getGroupingSeparatorForLocale(locale: String): String {
    return getGroupingSeparatorForLocaleInternal(locale)
}
