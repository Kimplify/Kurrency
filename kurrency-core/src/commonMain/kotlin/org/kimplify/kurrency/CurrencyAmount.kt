package org.kimplify.kurrency

import org.kimplify.kurrency.extensions.normalizeAmount
import kotlin.math.pow
import kotlin.math.roundToLong

data class CurrencyAmount(
    val minorUnits: Long,
    val currency: Kurrency,
) {
    fun format(
        style: CurrencyStyle = CurrencyStyle.Standard,
        locale: KurrencyLocale = KurrencyLocale.systemLocale(),
    ): Result<String> {
        val fractionDigits = currency.fractionDigitsOrDefault
        val divisor = 10.0.pow(fractionDigits)
        val majorAmount = minorUnits / divisor
        return currency.formatAmount(majorAmount.toString(), style, locale)
    }

    fun formatOrEmpty(
        style: CurrencyStyle = CurrencyStyle.Standard,
        locale: KurrencyLocale = KurrencyLocale.systemLocale(),
    ): String = format(style, locale).getOrDefault("")

    /**
     * Formats this amount using fine-grained [CurrencyFormatOptions].
     *
     * @param options The formatting options to apply
     * @param locale The locale for number formatting (defaults to system locale)
     * @return Result containing the formatted string, or failure if validation fails
     */
    fun format(
        options: CurrencyFormatOptions,
        locale: KurrencyLocale = KurrencyLocale.systemLocale(),
    ): Result<String> {
        val fractionDigits = currency.fractionDigitsOrDefault
        val divisor = 10.0.pow(fractionDigits)
        val majorAmount = minorUnits / divisor
        return currency.formatAmountWithOptions(majorAmount.toString(), options, locale)
    }

    /**
     * Formats this amount using [CurrencyFormatOptions], returning empty string on failure.
     *
     * @param options The formatting options to apply
     * @param locale The locale for number formatting (defaults to system locale)
     * @return The formatted string, or empty string if formatting fails
     */
    fun formatOrEmpty(
        options: CurrencyFormatOptions,
        locale: KurrencyLocale = KurrencyLocale.systemLocale(),
    ): String = format(options, locale).getOrDefault("")

    companion object {
        fun of(minorUnits: Long, currency: Kurrency) = CurrencyAmount(minorUnits, currency)

        fun fromMajorUnits(amount: String, currency: Kurrency): Result<CurrencyAmount> {
            val normalizedAmount = amount.normalizeAmount()
            val fractionDigits = currency.fractionDigitsOrDefault
            val multiplier = 10.0.pow(fractionDigits)
            val doubleValue = normalizedAmount.toDoubleOrNull()
                ?: return Result.failure(KurrencyError.InvalidAmount(amount))
            if (!doubleValue.isFinite()) {
                return Result.failure(KurrencyError.InvalidAmount(amount))
            }
            val minorUnits = (doubleValue * multiplier).roundToLong()
            return Result.success(CurrencyAmount(minorUnits, currency))
        }

        fun fromMajorUnits(amount: Double, currency: Kurrency): Result<CurrencyAmount> =
            fromMajorUnits(amount.toString(), currency)

        /**
         * Parses a formatted currency string into a [CurrencyAmount].
         *
         * @param formattedText The formatted currency text (e.g., "$1,234.56")
         * @param currency The [Kurrency] instance for the currency
         * @param locale The locale used for formatting (determines separator conventions)
         * @return Result containing CurrencyAmount, or failure with KurrencyError
         */
        fun parse(
            formattedText: String,
            currency: Kurrency,
            locale: KurrencyLocale,
        ): Result<CurrencyAmount> = runCatching {
            val formatter = CurrencyFormatter(locale)
            formatter.parseToCurrencyAmountResult(formattedText, currency).getOrThrow()
        }
    }
}
