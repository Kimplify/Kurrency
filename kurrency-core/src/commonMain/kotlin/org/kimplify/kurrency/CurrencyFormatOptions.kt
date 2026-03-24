package org.kimplify.kurrency

import kotlinx.serialization.Serializable

/**
 * Fine-grained formatting options for currency display.
 *
 * Use [CurrencyFormatOptions] to customise symbol placement, grouping, fraction digits,
 * negative-value presentation, and zero-value display without switching between the
 * coarser [CurrencyStyle] presets.
 *
 * ## Quick Start
 *
 * ```kotlin
 * // DSL style
 * val opts = CurrencyFormatOptions {
 *     symbolDisplay = SymbolDisplay.ISO_CODE
 *     negativeStyle = NegativeStyle.PARENTHESES
 * }
 * Kurrency.USD.formatAmountWithOptions("1234.56", opts)
 * ```
 *
 * ```kotlin
 * // Builder style (Java-friendly)
 * val opts = CurrencyFormatOptions.builder()
 *     .grouping(false)
 *     .symbolDisplay(SymbolDisplay.NONE)
 *     .build()
 * ```
 *
 * Pre-built constants [STANDARD], [ISO], and [ACCOUNTING] mirror the existing
 * [CurrencyStyle] presets.
 *
 * @property symbolPosition Where the currency symbol should appear relative to the number.
 * @property grouping Whether to include grouping (thousands) separators.
 * @property minFractionDigits Minimum number of decimal places. `null` means use the currency default.
 * @property maxFractionDigits Maximum number of decimal places. `null` means use the currency default.
 * @property negativeStyle How negative amounts should be rendered.
 * @property symbolDisplay What form the currency indicator takes (symbol, ISO code, name, or none).
 * @property zeroDisplay How zero amounts should be rendered.
 */
@Serializable
data class CurrencyFormatOptions(
    val symbolPosition: SymbolPosition = SymbolPosition.LOCALE_DEFAULT,
    val grouping: Boolean = true,
    val minFractionDigits: Int? = null,
    val maxFractionDigits: Int? = null,
    val negativeStyle: NegativeStyle = NegativeStyle.MINUS_SIGN,
    val symbolDisplay: SymbolDisplay = SymbolDisplay.SYMBOL,
    val zeroDisplay: ZeroDisplay = ZeroDisplay.SHOW,
) {
    init {
        if (minFractionDigits != null && maxFractionDigits != null) {
            require(minFractionDigits <= maxFractionDigits) {
                "minFractionDigits ($minFractionDigits) must be <= maxFractionDigits ($maxFractionDigits)"
            }
        }
        if (minFractionDigits != null) {
            require(minFractionDigits >= 0) {
                "minFractionDigits ($minFractionDigits) must be >= 0"
            }
        }
        if (maxFractionDigits != null) {
            require(maxFractionDigits >= 0) {
                "maxFractionDigits ($maxFractionDigits) must be >= 0"
            }
        }
    }

    companion object {
        /** Default formatting — equivalent to [CurrencyStyle.Standard]. */
        val STANDARD = CurrencyFormatOptions()

        /** ISO code formatting — equivalent to [CurrencyStyle.Iso]. */
        val ISO = CurrencyFormatOptions(symbolDisplay = SymbolDisplay.ISO_CODE)

        /** Accounting formatting — equivalent to [CurrencyStyle.Accounting]. */
        val ACCOUNTING = CurrencyFormatOptions(negativeStyle = NegativeStyle.PARENTHESES)

        /** Creates a new [Builder] for step-by-step construction. */
        fun builder() = Builder()

        /**
         * DSL constructor — configure options inside the lambda.
         *
         * ```kotlin
         * val opts = CurrencyFormatOptions {
         *     grouping = false
         *     symbolDisplay = SymbolDisplay.NONE
         * }
         * ```
         */
        inline operator fun invoke(block: Builder.() -> Unit): CurrencyFormatOptions =
            Builder().apply(block).build()
    }

    /**
     * Mutable builder for [CurrencyFormatOptions].
     *
     * Supports both property-assignment (Kotlin DSL) and fluent-chaining (Java) styles.
     */
    class Builder {
        var symbolPosition: SymbolPosition = SymbolPosition.LOCALE_DEFAULT
        var grouping: Boolean = true
        var minFractionDigits: Int? = null
        var maxFractionDigits: Int? = null
        var negativeStyle: NegativeStyle = NegativeStyle.MINUS_SIGN
        var symbolDisplay: SymbolDisplay = SymbolDisplay.SYMBOL
        var zeroDisplay: ZeroDisplay = ZeroDisplay.SHOW

        fun symbolPosition(value: SymbolPosition) = apply { symbolPosition = value }
        fun grouping(value: Boolean) = apply { grouping = value }
        fun minFractionDigits(value: Int?) = apply { minFractionDigits = value }
        fun maxFractionDigits(value: Int?) = apply { maxFractionDigits = value }
        fun negativeStyle(value: NegativeStyle) = apply { negativeStyle = value }
        fun symbolDisplay(value: SymbolDisplay) = apply { symbolDisplay = value }
        fun zeroDisplay(value: ZeroDisplay) = apply { zeroDisplay = value }

        /** Builds an immutable [CurrencyFormatOptions] from the current builder state. */
        fun build() = CurrencyFormatOptions(
            symbolPosition, grouping, minFractionDigits, maxFractionDigits,
            negativeStyle, symbolDisplay, zeroDisplay,
        )
    }
}

/**
 * Controls where the currency symbol/code appears relative to the numeric value.
 */
@Serializable
enum class SymbolPosition {
    /** Place the symbol before the number (e.g., "$100.00"). */
    LEADING,

    /** Place the symbol after the number (e.g., "100.00 $"). */
    TRAILING,

    /** Use the locale's default placement. */
    LOCALE_DEFAULT,
}

/**
 * Controls how negative amounts are displayed.
 */
@Serializable
enum class NegativeStyle {
    /** Prefix with a minus sign (e.g., "-$100.00"). */
    MINUS_SIGN,

    /** Wrap in parentheses (e.g., "($100.00)"). */
    PARENTHESES,

    /** Use whatever the locale uses by default. */
    LOCALE_DEFAULT,
}

/**
 * Controls what form the currency indicator takes.
 */
@Serializable
enum class SymbolDisplay {
    /** Use the currency symbol (e.g., "$", "EUR"). */
    SYMBOL,

    /** Use the ISO 4217 code (e.g., "USD", "EUR"). */
    ISO_CODE,

    /** Use the full currency name (e.g., "US Dollars"). */
    NAME,

    /** Omit the currency indicator entirely. */
    NONE,
}

/**
 * Controls how zero amounts are displayed.
 */
@Serializable
enum class ZeroDisplay {
    /** Display the formatted zero (e.g., "$0.00"). */
    SHOW,

    /** Display an em-dash character instead of zero. */
    DASH,

    /** Return an empty string for zero amounts. */
    EMPTY,
}
