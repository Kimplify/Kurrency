package org.kimplify.kurrency.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.kimplify.kurrency.KurrencyLocale
import org.kimplify.kurrency.SystemFormatting

@Composable
actual fun rememberSystemFormatting(locale: KurrencyLocale): SystemFormattingSnapshot {
    return remember(locale) {
        val systemFormatting = SystemFormatting(locale.locale)
        SystemFormattingSnapshot(
            decimalSeparator = systemFormatting.decimalSeparator,
            groupingSeparator = systemFormatting.groupingSeparator,
        )
    }
}
