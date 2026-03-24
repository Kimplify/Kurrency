package org.kimplify.kurrency.compose

import androidx.compose.ui.text.intl.Locale
import org.kimplify.kurrency.KurrencyLocale

actual fun KurrencyLocale.Companion.fromComposeLocale(composeLocale: Locale): KurrencyLocale {
    val tag = composeLocale.toLanguageTag()
    return KurrencyLocale.fromLanguageTag(tag).getOrElse { KurrencyLocale.systemLocale() }
}
