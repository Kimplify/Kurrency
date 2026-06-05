package org.kimplify.kurrency.deci

import org.kimplify.deci.Deci
import org.kimplify.kurrency.CurrencyFormat
import org.kimplify.kurrency.CurrencyFormatOptions
import org.kimplify.kurrency.CurrencyFormatter
import org.kimplify.kurrency.Kurrency

/**
 * Formats a [Deci] amount in currency style, returning the original value on error.
 *
 * @param amount The [Deci] amount to format
 * @param currencyCode The ISO 4217 currency code (e.g., "USD", "EUR")
 * @return The formatted string, or original amount if formatting fails
 */
fun CurrencyFormat.formatCurrencyStyle(amount: Deci, currencyCode: String): String =
    formatCurrencyStyle(amount.toString(), currencyCode)

/**
 * Formats a [Deci] amount using a [Kurrency] instance, returning the original value on error.
 *
 * @param amount The [Deci] amount to format
 * @param currency The [Kurrency] whose ISO code should be used
 * @return The formatted string, or original amount if formatting fails
 */
fun CurrencyFormat.formatCurrencyStyle(amount: Deci, currency: Kurrency): String =
    formatCurrencyStyle(amount.toString(), currency.code)

/**
 * Formats a [Deci] amount in ISO currency style, returning the original value on error.
 *
 * @param amount The [Deci] amount to format
 * @param currencyCode The ISO 4217 currency code (e.g., "USD", "EUR")
 * @return The formatted string with ISO code, or original amount if formatting fails
 */
fun CurrencyFormat.formatIsoCurrencyStyle(amount: Deci, currencyCode: String): String =
    formatIsoCurrencyStyle(amount.toString(), currencyCode)

/**
 * Formats a [Deci] amount in ISO style using a [Kurrency] instance, returning the original value on error.
 *
 * @param amount The [Deci] amount to format
 * @param currency The [Kurrency] whose ISO code should be used
 * @return The formatted string with ISO code, or original amount if formatting fails
 */
fun CurrencyFormat.formatIsoCurrencyStyle(amount: Deci, currency: Kurrency): String =
    formatIsoCurrencyStyle(amount.toString(), currency.code)

/**
 * Formats a [Deci] amount with fine-grained [CurrencyFormatOptions] using this formatter's locale.
 *
 * Formatting is delegated to the `Double`-based engine, so values beyond `Double` precision
 * may be rounded; this does not guarantee exact decimal formatting of the [Deci].
 *
 * @param amount The [Deci] amount to format
 * @param currencyCode The ISO 4217 currency code (e.g., "USD", "EUR")
 * @param options The formatting options to apply
 * @return Result containing the formatted string, or failure if validation fails
 */
fun CurrencyFormatter.formatWithOptions(
    amount: Deci,
    currencyCode: String,
    options: CurrencyFormatOptions,
): Result<String> = formatWithOptions(amount.toString(), currencyCode, options)

/**
 * Formats a [Deci] amount with fine-grained [CurrencyFormatOptions] using this formatter's locale.
 *
 * Formatting is delegated to the `Double`-based engine, so values beyond `Double` precision
 * may be rounded; this does not guarantee exact decimal formatting of the [Deci].
 *
 * @param amount The [Deci] amount to format
 * @param currency The [Kurrency] whose ISO code should be used
 * @param options The formatting options to apply
 * @return Result containing the formatted string, or failure if validation fails
 */
fun CurrencyFormatter.formatWithOptions(
    amount: Deci,
    currency: Kurrency,
    options: CurrencyFormatOptions,
): Result<String> = formatWithOptions(amount.toString(), currency.code, options)
