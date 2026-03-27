
package org.kimplify.kurrency

import org.kimplify.cedar.logging.Cedar
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class Kurrency private constructor(val code: String) {
    init {
        Cedar.tag("Kurrency").d("Currency created: code=$code")
    }

    val fractionDigits: Result<Int>
        get() = CurrencyFormatter.getFractionDigits(code)

    val fractionDigitsOrDefault: Int
        get() = CurrencyFormatter.getFractionDigitsOrDefault(code)

    companion object Companion {
        fun fromCode(code: String): Result<Kurrency> {
            return if (isValid(code)) {
                Result.success(Kurrency(code))
            } else {
                Result.failure(KurrencyError.InvalidCurrencyCode(code))
            }
        }

        fun isValid(code: String): Boolean {
            if (code.length != 3 || !code.all { it.isLetter() }) {
                return false
            }
            return isValidCurrency(code)
        }

        val USD: Kurrency by lazy { Kurrency("USD") }
        val EUR: Kurrency by lazy { Kurrency("EUR") }
        val GBP: Kurrency by lazy { Kurrency("GBP") }
        val JPY: Kurrency by lazy { Kurrency("JPY") }
        val AUD: Kurrency by lazy { Kurrency("AUD") }
        val CAD: Kurrency by lazy { Kurrency("CAD") }
        val CHF: Kurrency by lazy { Kurrency("CHF") }
        val CNY: Kurrency by lazy { Kurrency("CNY") }
        val INR: Kurrency by lazy { Kurrency("INR") }
        val KRW: Kurrency by lazy { Kurrency("KRW") }
        val MXN: Kurrency by lazy { Kurrency("MXN") }
        val BRL: Kurrency by lazy { Kurrency("BRL") }
        val ZAR: Kurrency by lazy { Kurrency("ZAR") }
        val NZD: Kurrency by lazy { Kurrency("NZD") }
        val SGD: Kurrency by lazy { Kurrency("SGD") }
        val HKD: Kurrency by lazy { Kurrency("HKD") }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Kurrency) return false
        return code == other.code
    }

    override fun hashCode(): Int = code.hashCode()

    override fun toString(): String = "Currency(code=$code)"
    
    fun formatAmount(
        amount: String,
        style: CurrencyStyle = CurrencyStyle.Standard,
        locale: KurrencyLocale = KurrencyLocale.systemLocale()
    ): Result<String> {
        Cedar.tag("Kurrency").d("Formatting amount: amount=$amount, currency=$code, style=$style")
        val formatter = CurrencyFormatter(locale)
        return when (style) {
            CurrencyStyle.Standard -> formatter.formatCurrencyStyleResult(amount, code)
            CurrencyStyle.Iso -> formatter.formatIsoCurrencyStyleResult(amount, code)
            CurrencyStyle.Accounting -> formatter.formatCurrencyStyleResult(amount, code).map { formatted ->
                if (formatted.contains("-") || amount.trimStart().startsWith("-")) {
                    val withoutMinus = formatted.replace("-", "").replace("\u2212", "").trim()
                    "($withoutMinus)"
                } else {
                    formatted
                }
            }
        }
    }
    
    fun formatAmount(
        amount: Double,
        style: CurrencyStyle = CurrencyStyle.Standard,
        locale: KurrencyLocale = KurrencyLocale.systemLocale()
    ): Result<String> = formatAmount(amount.toString(), style, locale)

    fun formatAmountOrEmpty(
        amount: String,
        style: CurrencyStyle = CurrencyStyle.Standard,
        locale: KurrencyLocale = KurrencyLocale.systemLocale()
    ): String = formatAmount(amount, style, locale).getOrDefault("")

    fun formatAmountOrEmpty(
        amount: Double,
        style: CurrencyStyle = CurrencyStyle.Standard,
        locale: KurrencyLocale = KurrencyLocale.systemLocale()
    ): String = formatAmount(amount, style, locale).getOrDefault("")

    /**
     * Formats an amount using fine-grained [CurrencyFormatOptions].
     *
     * ```kotlin
     * val opts = CurrencyFormatOptions {
     *     symbolDisplay = SymbolDisplay.ISO_CODE
     *     negativeStyle = NegativeStyle.PARENTHESES
     * }
     * Kurrency.USD.formatAmountWithOptions("1234.56", opts)
     * ```
     *
     * @param amount The amount to format
     * @param options The formatting options to apply
     * @param locale The locale for number formatting (defaults to system locale)
     * @return Result containing the formatted string, or failure if validation fails
     */
    fun formatAmountWithOptions(
        amount: String,
        options: CurrencyFormatOptions,
        locale: KurrencyLocale = KurrencyLocale.systemLocale(),
    ): Result<String> {
        val formatter = CurrencyFormatter(locale)
        return formatter.formatWithOptions(amount, code, options)
    }

    /**
     * Formats a Double amount using fine-grained [CurrencyFormatOptions].
     *
     * @param amount The amount to format
     * @param options The formatting options to apply
     * @param locale The locale for number formatting (defaults to system locale)
     * @return Result containing the formatted string, or failure if validation fails
     */
    fun formatAmountWithOptions(
        amount: Double,
        options: CurrencyFormatOptions,
        locale: KurrencyLocale = KurrencyLocale.systemLocale(),
    ): Result<String> = formatAmountWithOptions(amount.toString(), options, locale)

    fun formatAmountCompact(
        amount: String,
        locale: KurrencyLocale = KurrencyLocale.systemLocale(),
    ): Result<String> {
        val formatter = CurrencyFormatter(locale)
        return formatter.formatCompactStyleResult(amount, code)
    }

    fun formatAmountCompact(
        amount: Double,
        locale: KurrencyLocale = KurrencyLocale.systemLocale(),
    ): Result<String> = formatAmountCompact(amount.toString(), locale)

    fun formatMinorUnits(
        minorUnits: Long,
        locale: KurrencyLocale = KurrencyLocale.systemLocale(),
    ): Result<String> {
        val formatter = CurrencyFormatter(locale)
        return formatter.formatMinorUnitsResult(minorUnits, code)
    }

    fun format(
        amount: String,
        style: CurrencyStyle = CurrencyStyle.Standard,
        locale: KurrencyLocale = KurrencyLocale.systemLocale()
    ): FormattedCurrencyDelegate = FormattedCurrencyDelegate(this, amount, style, locale)

    fun format(
        amount: Double,
        style: CurrencyStyle = CurrencyStyle.Standard,
        locale: KurrencyLocale = KurrencyLocale.systemLocale()
    ): FormattedCurrencyDelegate = FormattedCurrencyDelegate(this, amount.toString(), style, locale)
}

class FormattedCurrencyDelegate(
    private val currency: Kurrency,
    private val amount: String,
    private val style: CurrencyStyle,
    private val locale: KurrencyLocale
) : ReadOnlyProperty<Any?, String> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return currency.formatAmount(amount, style, locale).getOrDefault("")
    }
}

enum class CurrencyStyle {
    Standard,
    Iso,
    Accounting,
}
