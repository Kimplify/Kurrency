package org.kimplify.kurrency

/**
 * Which platform formatting path [CurrencyFormatterImpl.formatOrThrow] should take. Internal: the
 * public surface expresses the same choice through the separate [CurrencyFormat] methods.
 */
internal enum class PlatformFormatStyle {
    SYMBOL,
    ISO_CODE,
    COMPACT,
}

/**
 * Applies the lenient contract the [CurrencyFormat] methods document — the original amount is
 * returned when the platform cannot format it — in one place, so the platform implementations do not
 * each decide it for themselves. The failure still reaches callers who ask for a `Result`, because
 * [CurrencyFormatter] takes the throwing path instead of this one.
 */
internal inline fun formatLeniently(
    amount: String,
    currencyCode: String,
    format: () -> String,
): String = runCatching { format() }.getOrElse { throwable ->
    KurrencyLog.w { "Formatting failed for $currencyCode with amount $amount: ${throwable.message}" }
    amount
}
