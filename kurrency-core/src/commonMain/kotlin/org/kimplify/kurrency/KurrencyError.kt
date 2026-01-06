package org.kimplify.kurrency

/**
 * Sealed hierarchy of errors that can occur during currency operations.
 *
 * All formatting and validation methods in the Kurrency library return `Result<T>` types,
 * which can contain instances of this error class on failure.
 *
 * ## Error Handling Patterns
 *
 * ### Using getOrNull()
 * ```kotlin
 * val formatted = formatter.formatCurrencyStyleResult("1234.56", "USD").getOrNull()
 * if (formatted == null) {
 *     // Handle error
 * }
 * ```
 *
 * ### Using getOrElse()
 * ```kotlin
 * val formatted = formatter.formatCurrencyStyleResult("1234.56", "USD")
 *     .getOrElse { "N/A" }
 * ```
 *
 * ### Using fold() for type-specific handling
 * ```kotlin
 * formatter.formatCurrencyStyleResult("1234.56", "USD").fold(
 *     onSuccess = { formatted -> displayPrice(formatted) },
 *     onFailure = { error ->
 *         when (error) {
 *             is KurrencyError.InvalidCurrencyCode -> showError("Invalid currency: ${error.code}")
 *             is KurrencyError.InvalidAmount -> showError("Invalid amount: ${error.amount}")
 *             is KurrencyError.FormattingFailure -> showError("Formatting error")
 *             is KurrencyError.FractionDigitsFailure -> showError("Currency not supported")
 *             else -> showError("Unknown error")
 *         }
 *     }
 * )
 * ```
 *
 * @property errorMessage Human-readable error description
 */
sealed class KurrencyError(
    val errorMessage: String,
    cause: Throwable? = null
) : Exception(errorMessage, cause) {

    /**
     * Currency code is invalid or not recognized.
     *
     * Occurs when:
     * - Code is not exactly 3 letters
     * - Code contains non-alphabetic characters
     * - Code is not a valid ISO 4217 currency code
     *
     * @property code The invalid currency code that was provided
     */
    class InvalidCurrencyCode(
        val code: String
    ) : KurrencyError("Invalid currency code: $code")

    /**
     * Amount value is invalid or cannot be parsed.
     *
     * Occurs when:
     * - Amount is blank or empty
     * - Amount contains invalid characters
     * - Amount cannot be parsed as a number
     * - Amount is infinity or NaN
     *
     * @property amount The invalid amount string that was provided
     */
    class InvalidAmount(
        val amount: String
    ) : KurrencyError("Invalid amount: $amount")

    /**
     * Platform-specific formatting operation failed.
     *
     * Occurs when the underlying platform formatter (ICU, NSNumberFormatter, etc.)
     * encounters an error during the formatting process.
     *
     * @property currencyCode The currency code being formatted
     * @property amount The amount being formatted
     */
    class FormattingFailure(
        val currencyCode: String,
        val amount: String,
        cause: Throwable
    ) : KurrencyError("Formatting failed for $currencyCode: $amount", cause)

    /**
     * Failed to retrieve fraction digits for the currency.
     *
     * Occurs when the platform cannot determine the standard number of decimal places
     * for a currency code (e.g., 2 for USD, 0 for JPY).
     *
     * @property currencyCode The currency code for which fraction digits were requested
     */
    class FractionDigitsFailure(
        val currencyCode: String,
        cause: Throwable
    ) : KurrencyError("Failed to get fraction digits for $currencyCode", cause)
}

