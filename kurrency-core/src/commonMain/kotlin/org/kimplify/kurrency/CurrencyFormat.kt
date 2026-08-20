package org.kimplify.kurrency

interface CurrencyFormat {
    /**
     * Gets the number of fraction digits for a currency code, returning a default value on error.
     * This is the recommended method for UI use cases.
     *
     * @param currencyCode The ISO 4217 currency code (e.g., "USD", "EUR")
     * @param default The default value to return on error (defaults to 2)
     * @return The number of fraction digits, or default if there's an error
     */
    fun getFractionDigitsOrDefault(currencyCode: String, default: Int = 2): Int

    /**
     * Gets the locale-aware currency symbol for a currency code, returning [default] on error or
     * where the platform has no locale-specific data.
     *
     * The symbol depends on the reader's locale, not only on the currency: `AUD` is `"A$"` to a
     * US reader and `"$"` to an Australian one, which is what keeps currencies sharing the `$`
     * glyph apart. A static table cannot express that, so this delegates to the platform.
     *
     * @param currencyCode The ISO 4217 currency code (e.g., "USD", "AUD")
     * @param default The symbol to fall back to when the platform cannot supply one
     * @return The locale-aware symbol, or [default]
     */
    fun getCurrencySymbolOrDefault(currencyCode: String, default: String): String = default

    /**
     * Formats an amount in currency style, returning original value on error.
     * This is the recommended method for UI display.
     *
     * @param amount The amount to format
     * @param currencyCode The ISO 4217 currency code (e.g., "USD", "EUR")
     * @return The formatted string, or original amount if formatting fails
     */
    fun formatCurrencyStyle(amount: String, currencyCode: String): String

    /**
     * Formats an amount using a [Kurrency] instance, returning original value on error.
     *
     * @param amount The amount to format
     * @param currency The [Kurrency] whose ISO code should be used
     * @return The formatted string, or original amount if formatting fails
     */
    fun formatCurrencyStyle(amount: String, currency: Kurrency): String =
        formatCurrencyStyle(amount, currency.code)

    /**
     * Formats an amount in ISO currency style, returning original value on error.
     * This is the recommended method for UI display.
     *
     * @param amount The amount to format
     * @param currencyCode The ISO 4217 currency code (e.g., "USD", "EUR")
     * @return The formatted string with ISO code, or original amount if formatting fails
     */
    fun formatIsoCurrencyStyle(amount: String, currencyCode: String): String

    /**
     * Formats an amount in ISO style using a [Kurrency] instance, returning original value on error.
     *
     * @param amount The amount to format
     * @param currency The [Kurrency] whose ISO code should be used
     * @return The formatted string with ISO code, or original amount if formatting fails
     */
    fun formatIsoCurrencyStyle(amount: String, currency: Kurrency): String =
        formatIsoCurrencyStyle(amount, currency.code)

    fun formatCompactStyle(amount: String, currencyCode: String): String =
        formatCurrencyStyle(amount, currencyCode)

    fun formatCompactStyle(amount: String, currency: Kurrency): String =
        formatCompactStyle(amount, currency.code)

    fun parseCurrencyAmount(formattedText: String, currencyCode: String): Double? = null

    /**
     * Parses a formatted currency string into minor units (e.g., cents for USD).
     * Uses string-based arithmetic to avoid floating-point precision issues.
     *
     * @param formattedText The formatted currency text (e.g., "$1,234.56")
     * @param currencyCode The ISO 4217 currency code (e.g., "USD")
     * @return Result containing minor units as Long, or failure with KurrencyError
     */
    fun parseToMinorUnitsResult(formattedText: String, currencyCode: String): Result<Long> {
        return Result.failure(KurrencyError.InvalidAmount(formattedText))
    }

    /**
     * Parses a formatted currency string into a [CurrencyAmount].
     *
     * @param formattedText The formatted currency text (e.g., "$1,234.56")
     * @param currency The [Kurrency] instance for the currency
     * @return Result containing CurrencyAmount, or failure with KurrencyError
     */
    fun parseToCurrencyAmountResult(formattedText: String, currency: Kurrency): Result<CurrencyAmount> {
        return parseToMinorUnitsResult(formattedText, currency.code).map { minorUnits ->
            CurrencyAmount(minorUnits, currency)
        }
    }

    fun minorUnitsToPlainString(minorUnits: Long, currencyCode: String): String {
        val fractionDigits = getFractionDigitsOrDefault(currencyCode)
        if (fractionDigits <= 0) return minorUnits.toString()
        val sign = if (minorUnits < 0) "-" else ""
        // kotlin.math.abs(Long.MIN_VALUE) overflows back to Long.MIN_VALUE; strip the
        // leading '-' from its decimal representation instead.
        val absString = if (minorUnits == Long.MIN_VALUE) {
            minorUnits.toString().substring(1)
        } else {
            kotlin.math.abs(minorUnits).toString()
        }
        val str = absString.padStart(fractionDigits + 1, '0')
        val whole = str.dropLast(fractionDigits)
        val fraction = str.takeLast(fractionDigits)
        return "$sign$whole.$fraction"
    }

    fun formatMinorUnits(minorUnits: Long, currencyCode: String): String {
        val plainAmount = minorUnitsToPlainString(minorUnits, currencyCode)
        return formatCurrencyStyle(plainAmount, currencyCode)
    }

    fun formatMinorUnitsIsoStyle(minorUnits: Long, currencyCode: String): String {
        val plainAmount = minorUnitsToPlainString(minorUnits, currencyCode)
        return formatIsoCurrencyStyle(plainAmount, currencyCode)
    }

    fun formatMinorUnitsCompactStyle(minorUnits: Long, currencyCode: String): String {
        val plainAmount = minorUnitsToPlainString(minorUnits, currencyCode)
        return formatCompactStyle(plainAmount, currencyCode)
    }
}
