package org.kimplify.kurrency

import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleDecimalSeparator
import platform.Foundation.NSLocaleGroupingSeparator
import platform.Foundation.currentLocale

object SystemFormatting : SystemFormattingProvider {
    override val decimalSeparator: String
        get() = (NSLocale.currentLocale.objectForKey(NSLocaleDecimalSeparator) as? String)
            ?.firstOrNull()?.toString() ?: "."

    override val groupingSeparator: String
        get() = (NSLocale.currentLocale.objectForKey(NSLocaleGroupingSeparator) as? String)
            ?.firstOrNull()?.toString() ?: ","
}
