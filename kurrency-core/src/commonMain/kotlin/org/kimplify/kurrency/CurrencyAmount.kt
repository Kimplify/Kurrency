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
    }
}
