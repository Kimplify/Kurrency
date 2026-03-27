package org.kimplify.kurrency


class SystemFormatting(val locale: KurrencyLocale) : SystemFormattingProvider {
    override val decimalSeparator: String
        get() = getDecimalSeparatorForLocale(locale.languageTag).firstOrNull()?.toString() ?: "."

    override val groupingSeparator: String
        get() = getGroupingSeparatorForLocale(locale.languageTag).firstOrNull()?.toString() ?: ""
}