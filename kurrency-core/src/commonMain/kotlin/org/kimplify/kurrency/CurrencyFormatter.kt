package org.kimplify.kurrency

import org.kimplify.kurrency.extensions.normalizeAmount

expect class CurrencyFormatterImpl(kurrencyLocale: KurrencyLocale = KurrencyLocale.systemLocale()) : CurrencyFormat {
    override fun getFractionDigitsOrDefault(currencyCode: String, default: Int): Int
    override fun formatCurrencyStyle(amount: String, currencyCode: String): String
    override fun formatIsoCurrencyStyle(amount: String, currencyCode: String): String
    override fun formatCompactStyle(amount: String, currencyCode: String): String
    override fun parseCurrencyAmount(formattedText: String, currencyCode: String): Double?
}

expect fun isValidCurrency(currencyCode: String): Boolean

/**
 * CurrencyFormatter provides currency formatting functionality with locale support.
 *
 * Create instances with a specific locale for formatting operations:
 * ```
 * val formatter = CurrencyFormatter(KurrencyLocale.US)
 * formatter.formatCurrencyStyleResult("100.50", "USD")
 * ```
 *
 * Use companion object methods for locale-independent operations:
 * ```
 * CurrencyFormatter.getFractionDigits("USD") // Returns 2
 * ```
 *
 * ## Thread Safety
 *
 * CurrencyFormatter instances are thread-safe for concurrent read operations after initialization.
 * The underlying platform formatter is lazily initialized on first use.
 *
 * ### Safe Usage Patterns
 *
 * **Shared formatter (recommended):**
 * ```
 * val sharedFormatter = CurrencyFormatter(KurrencyLocale.US)
 *
 * launch { sharedFormatter.formatCurrencyStyleResult("100", "USD") }
 * launch { sharedFormatter.formatCurrencyStyleResult("200", "EUR") }
 * ```
 *
 * **Per-thread formatters:**
 * ```
 * launch { CurrencyFormatter(KurrencyLocale.US).formatCurrencyStyleResult("100", "USD") }
 * ```
 *
 * Both patterns are safe. Formatter instances use platform-specific implementations that are
 * inherently thread-safe (ICU on Android, NSNumberFormatter on iOS, NumberFormat on JVM).
 */
class CurrencyFormatter(private val locale: KurrencyLocale = KurrencyLocale.systemLocale()) : CurrencyFormat {

    private val impl: CurrencyFormat by lazy {
        KurrencyLog.d { "Initializing CurrencyFormatter with locale: ${locale.languageTag}" }
        CurrencyFormatterImpl(locale)
    }

    override fun getFractionDigitsOrDefault(currencyCode: String, default: Int): Int =
        impl.getFractionDigitsOrDefault(currencyCode, default)

    override fun formatCurrencyStyle(amount: String, currencyCode: String): String =
        formatCurrencyStyleResult(amount, currencyCode).getOrElse { amount }

    override fun formatIsoCurrencyStyle(amount: String, currencyCode: String): String =
        formatIsoCurrencyStyleResult(amount, currencyCode).getOrElse { amount }

    override fun formatCompactStyle(amount: String, currencyCode: String): String =
        formatCompactStyleResult(amount, currencyCode).getOrElse { amount }

    fun formatCompactStyleResult(amount: String, currencyCode: String): Result<String> {
        return formatWithValidation(amount, currencyCode) {
            Result.success(impl.formatCompactStyle(it, currencyCode))
        }
    }

    fun parseCurrencyAmountResult(formattedText: String, currencyCode: String): Result<Double> {
        if (!isValidCurrencyCode(currencyCode)) {
            return Result.failure(KurrencyError.InvalidCurrencyCode(currencyCode))
        }
        val parsed = impl.parseCurrencyAmount(formattedText, currencyCode)
            ?: return Result.failure(KurrencyError.InvalidAmount(formattedText))
        return Result.success(parsed)
    }

    fun formatMinorUnitsResult(minorUnits: Long, currencyCode: String): Result<String> {
        if (!isValidCurrencyCode(currencyCode)) {
            return Result.failure(KurrencyError.InvalidCurrencyCode(currencyCode))
        }
        return Result.success(impl.formatMinorUnits(minorUnits, currencyCode))
    }

    /**
     * Formats an amount in currency style using this formatter's locale.
     *
     * @param amount The amount to format
     * @param currencyCode The ISO 4217 currency code (e.g., "USD", "EUR")
     * @return Result containing the formatted string, or failure if validation fails
     */
    fun formatCurrencyStyleResult(amount: String, currencyCode: String): Result<String> {
        return formatWithValidation(amount, currencyCode) {
            Result.success(impl.formatCurrencyStyle(it, currencyCode))
        }
    }

    /**
     * Formats an amount in ISO currency style using this formatter's locale.
     *
     * @param amount The amount to format
     * @param currencyCode The ISO 4217 currency code (e.g., "USD", "EUR")
     * @return Result containing the formatted string with ISO code, or failure if validation fails
     */
    fun formatIsoCurrencyStyleResult(amount: String, currencyCode: String): Result<String> {
        return formatWithValidation(amount, currencyCode) {
            Result.success(impl.formatIsoCurrencyStyle(it, currencyCode))
        }
    }

    private fun formatWithValidation(
        amount: String,
        currencyCode: String,
        format: (String) -> Result<String>
    ): Result<String> {
        if (!isValidCurrencyCode(currencyCode)) {
            val error = KurrencyError.InvalidCurrencyCode(currencyCode)
            KurrencyLog.w { error.errorMessage }
            return Result.failure(error)
        }

        if (!isValidAmount(amount)) {
            val error = KurrencyError.InvalidAmount(amount)
            KurrencyLog.w { error.errorMessage }
            return Result.failure(error)
        }

        KurrencyLog.d { "Formatting: amount=$amount, currency=$currencyCode" }
        return format(amount)
            .onFailure { throwable ->
                val error = KurrencyError.FormattingFailure(currencyCode, amount, throwable)
                KurrencyLog.e(throwable) { error.errorMessage }
            }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CurrencyFormatter) return false
        return locale.languageTag == other.locale.languageTag
    }

    override fun hashCode(): Int = locale.languageTag.hashCode()

    companion object Companion {
        private const val DEFAULT_FRACTION_DIGITS = 2
        private val defaultFormatter: CurrencyFormat by lazy {
            KurrencyLog.d { "Initializing default CurrencyFormatter" }
            CurrencyFormatterImpl()
        }


        /**
         * Gets the fraction digits for a currency code.
         * Fraction digits are defined by ISO 4217 and do not vary by locale.
         *
         * @param currencyCode The ISO 4217 currency code (e.g., "USD", "EUR")
         * @return Result containing the number of fraction digits, or failure if currency is invalid
         */
        fun getFractionDigits(currencyCode: String): Result<Int> {
            val kurrency = Kurrency.fromCode(currencyCode).getOrElse { throwable ->
                return Result.failure(throwable)
            }

            CurrencyMetadata.parse(kurrency.code).getOrNull()?.let {
                return Result.success(it.fractionDigits)
            }

            return runCatching {
                val normalizedCode = kurrency.code.uppercase()
                defaultFormatter.getFractionDigitsOrDefault(normalizedCode, DEFAULT_FRACTION_DIGITS)
            }.fold(
                onSuccess = { Result.success(it) },
                onFailure = { throwable ->
                    val error = KurrencyError.FractionDigitsFailure(currencyCode, throwable)
                    KurrencyLog.e(throwable) { error.errorMessage }
                    Result.failure(error)
                }
            )
        }

        /**
         * Gets the fraction digits for a currency code, or returns default value.
         *
         * @param currencyCode The ISO 4217 currency code
         * @return The number of fraction digits, or DEFAULT_FRACTION_DIGITS if there's an error
         */
        fun getFractionDigitsOrDefault(currencyCode: String): Int =
            getFractionDigits(currencyCode).getOrDefault(DEFAULT_FRACTION_DIGITS)

        private fun isValidCurrencyCode(code: String): Boolean =
            code.length == 3 && code.all { it.isLetter() } && isValidCurrency(code)

        private fun isValidAmount(amount: String): Boolean {
            if (amount.isBlank()) return false
            val normalized = amount.normalizeAmount()
            val doubleValue = normalized.toDoubleOrNull() ?: return false
            return doubleValue.isFinite()
        }
    }
}
