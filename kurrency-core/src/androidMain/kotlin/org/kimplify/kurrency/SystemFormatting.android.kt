package org.kimplify.kurrency

import java.text.DecimalFormatSymbols
import java.util.Locale

class SystemFormatting(val locale: Locale): SystemFormattingProvider {
    override val decimalSeparator: String
        get() = DecimalFormatSymbols.getInstance(locale).decimalSeparator.toString()

    override val groupingSeparator: String
        get() = DecimalFormatSymbols.getInstance(locale).groupingSeparator.toString()
}
