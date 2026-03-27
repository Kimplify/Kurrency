package org.kimplify.kurrency

import org.kimplify.kurrency.extensions.normalizeAmount
import kotlin.math.abs

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
        val cleanDecimalString = cleanFormattedText(formattedText, currencyCode)
            ?: return Result.failure(KurrencyError.InvalidAmount(formattedText))
        val parsed = cleanDecimalString.toDoubleOrNull()
            ?: return Result.failure(KurrencyError.InvalidAmount(formattedText))
        if (!parsed.isFinite()) {
            return Result.failure(KurrencyError.InvalidAmount(formattedText))
        }
        return Result.success(parsed)
    }

    override fun parseToMinorUnitsResult(formattedText: String, currencyCode: String): Result<Long> {
        if (!isValidCurrencyCode(currencyCode)) {
            return Result.failure(KurrencyError.InvalidCurrencyCode(currencyCode))
        }
        val fractionDigits = CurrencyMetadata.parse(currencyCode).getOrNull()?.fractionDigits
            ?: getFractionDigitsOrDefault(currencyCode)

        val cleanDecimalString = cleanFormattedText(formattedText, currencyCode)
            ?: return Result.failure(KurrencyError.InvalidAmount(formattedText))

        // String-based arithmetic to avoid floating-point precision issues
        val isNegative = cleanDecimalString.startsWith("-")
        val absString = if (isNegative) cleanDecimalString.drop(1) else cleanDecimalString

        val parts = absString.split(".")
        val integerPart = parts[0].ifEmpty { "0" }
        val fractionalPart = if (parts.size > 1) parts[1] else ""

        // Pad or truncate fractional part to match currency's fractionDigits
        val adjustedFraction = when {
            fractionalPart.length == fractionDigits -> fractionalPart
            fractionalPart.length < fractionDigits -> fractionalPart.padEnd(fractionDigits, '0')
            else -> fractionalPart.take(fractionDigits)
        }

        val combined = if (fractionDigits == 0) integerPart else integerPart + adjustedFraction
        val minorUnits = combined.toLongOrNull()
            ?: return Result.failure(KurrencyError.InvalidAmount(formattedText))

        return Result.success(if (isNegative) -minorUnits else minorUnits)
    }

    override fun parseToCurrencyAmountResult(formattedText: String, currency: Kurrency): Result<CurrencyAmount> {
        return parseToMinorUnitsResult(formattedText, currency.code).map { minorUnits ->
            CurrencyAmount(minorUnits, currency)
        }
    }

    /**
     * Cleans a formatted currency string into a plain decimal string (e.g., "-1234.56").
     * Returns null if the input cannot be parsed.
     *
     * Pipeline:
     * 1. Strip bidi marks
     * 2. Detect accounting notation
     * 3. Strip currency symbol and ISO code
     * 4. Detect compact suffixes (English only)
     * 5. Locale-aware grouping/decimal normalization
     * 6. Handle negative sign
     * 7. Validate
     */
    private fun cleanFormattedText(formattedText: String, currencyCode: String): String? {
        if (formattedText.isBlank()) return null

        var text = formattedText

        // Step 1: Strip bidi marks and convert non-Western digits
        text = text.replace("\u200F", "") // RTL mark
            .replace("\u200E", "")         // LTR mark
            .replace("\u061C", "")         // Arabic letter mark
            .replace("\u200B", "")         // Zero-width space
            .replace("\uFEFF", "")         // BOM / zero-width no-break space

        // Convert Eastern Arabic / Persian digits to Western
        text = convertToWesternDigits(text)

        // Step 2: Detect accounting notation (parentheses = negative)
        val isAccounting = text.trim().startsWith("(") && text.trim().endsWith(")")
        if (isAccounting) {
            text = text.trim().drop(1).dropLast(1)
        }

        // Step 3: Strip currency symbol and ISO code
        val metadata = CurrencyMetadata.parse(currencyCode).getOrNull()
        if (metadata != null) {
            text = text.replace(metadata.symbol, "")
        }
        // Also strip common variant currency symbols (e.g., fullwidth yen ￥ vs ¥)
        text = stripVariantCurrencySymbols(text, currencyCode)
        // Strip the ISO currency code
        text = text.replace(currencyCode, "")
        text = text.replace(currencyCode.uppercase(), "")

        // Step 4: Detect compact suffixes (English only: K, M, B, T)
        var compactMultiplier: Long? = null
        val trimmedForCompact = text.trim()
        val langTag = locale.languageTag.lowercase()
        val isEnglish = langTag.startsWith("en")
        if (isEnglish && trimmedForCompact.isNotEmpty()) {
            val lastChar = trimmedForCompact.last().uppercaseChar()
            val multiplier = when (lastChar) {
                'K' -> 1_000L
                'M' -> 1_000_000L
                'B' -> 1_000_000_000L
                'T' -> 1_000_000_000_000L
                else -> null
            }
            if (multiplier != null) {
                compactMultiplier = multiplier
                text = trimmedForCompact.dropLast(1)
            }
        }

        // Step 5: Locale-aware stripping of grouping separators and decimal normalization
        text = text.normalizeAmount(locale)

        // Step 6: Handle negative sign (unicode minus and regular minus)
        var isNegative = isAccounting
        text = text.replace("\u2212", "-")
        if (text.contains("-")) {
            // If accounting notation already set negative, a minus sign cancels it out
            isNegative = if (isAccounting) !isNegative else true
            text = text.replace("-", "")
        }

        // Step 7: Validate
        text = text.trim()
        if (text.isEmpty()) return null

        // Validate that remaining text is a valid number
        if (text.toDoubleOrNull() == null) return null

        // Apply compact multiplier if present
        if (compactMultiplier != null) {
            val baseValue = text.toDoubleOrNull() ?: return null
            if (!baseValue.isFinite()) return null
            val finalValue = baseValue * compactMultiplier
            val signedValue = if (isNegative) -finalValue else finalValue
            return if (signedValue == 0.0) "0" else doubleToPlainString(signedValue)
        }

        // For non-compact amounts, preserve the string representation to avoid floating-point loss
        val sign = if (isNegative) "-" else ""

        // Normalize: remove leading zeros from integer part (but keep at least one digit)
        val parts = text.split(".")
        val intPart = parts[0].trimStart('0').ifEmpty { "0" }
        val result = if (parts.size > 1) "$intPart.${parts[1]}" else intPart

        return if (result == "0" || result == "0.0" || result == "0.00") {
            "0"
        } else {
            "$sign$result"
        }
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

    /**
     * Formats an amount with fine-grained [CurrencyFormatOptions].
     *
     * This method uses the platform's standard currency formatter as a base and then
     * post-processes the output to apply the requested options (symbol display, grouping,
     * negative style, fraction digits, zero display, and symbol position).
     *
     * ```kotlin
     * val formatter = CurrencyFormatter(KurrencyLocale.US)
     * val opts = CurrencyFormatOptions {
     *     symbolDisplay = SymbolDisplay.ISO_CODE
     *     negativeStyle = NegativeStyle.PARENTHESES
     * }
     * formatter.formatWithOptions("1234.56", "USD", opts)
     * // => Result.success("(USD 1,234.56)")  // when amount is negative
     * ```
     *
     * @param amount The amount to format (supports locale-aware input like "1.234,56")
     * @param currencyCode The ISO 4217 currency code (e.g., "USD", "EUR")
     * @param options The formatting options to apply
     * @return Result containing the formatted string, or failure if validation fails
     */
    fun formatWithOptions(
        amount: String,
        currencyCode: String,
        options: CurrencyFormatOptions,
    ): Result<String> {
        return formatWithValidation(amount, currencyCode) { normalizedAmount ->
            runCatching {
                val numericValue = normalizedAmount.toDoubleOrNull()
                    ?: throw KurrencyError.InvalidAmount(amount)

                // Zero display handling
                if (numericValue == 0.0) {
                    when (options.zeroDisplay) {
                        ZeroDisplay.DASH -> return@runCatching "\u2014" // em-dash
                        ZeroDisplay.EMPTY -> return@runCatching ""
                        ZeroDisplay.SHOW -> { /* fall through to normal formatting */ }
                    }
                }

                val metadata = CurrencyMetadata.parse(currencyCode).getOrNull()
                val symbol = metadata?.symbol ?: ""
                val isNegative = numericValue < 0
                val absAmount = if (isNegative) {
                    if (normalizedAmount.startsWith("-")) normalizedAmount.drop(1) else normalizedAmount
                } else {
                    normalizedAmount
                }

                // Format the absolute amount with custom fraction digits if needed
                val formattedAbsAmount = formatNumberPortion(absAmount, currencyCode, options)

                // Build the currency indicator
                val currencyIndicator = when (options.symbolDisplay) {
                    SymbolDisplay.SYMBOL -> symbol
                    SymbolDisplay.ISO_CODE -> currencyCode
                    SymbolDisplay.NAME -> {
                        if (abs(numericValue) == 1.0) metadata?.displayName ?: currencyCode
                        else metadata?.displayNamePlural ?: currencyCode
                    }
                    SymbolDisplay.NONE -> ""
                }

                // Determine symbol position
                val effectivePosition = when (options.symbolPosition) {
                    SymbolPosition.LOCALE_DEFAULT -> detectSymbolPosition(currencyCode, symbol)
                    SymbolPosition.LEADING -> SymbolPosition.LEADING
                    SymbolPosition.TRAILING -> SymbolPosition.TRAILING
                }

                // Assemble result with currency indicator
                var result = when {
                    currencyIndicator.isEmpty() -> formattedAbsAmount
                    effectivePosition == SymbolPosition.LEADING || effectivePosition == SymbolPosition.LOCALE_DEFAULT ->
                        "$currencyIndicator$formattedAbsAmount"
                    else -> "$formattedAbsAmount $currencyIndicator"
                }

                // Handle negative style
                if (isNegative) {
                    result = when (options.negativeStyle) {
                        NegativeStyle.PARENTHESES -> "($result)"
                        NegativeStyle.MINUS_SIGN -> "-$result"
                        NegativeStyle.LOCALE_DEFAULT -> "-$result"
                    }
                }

                result
            }
        }
    }

    /**
     * Formats just the numeric portion of the amount (no currency symbol), applying
     * grouping and fraction-digit options.
     */
    private fun formatNumberPortion(
        absAmount: String,
        currencyCode: String,
        options: CurrencyFormatOptions,
    ): String {
        val fractionDigits = CurrencyMetadata.parse(currencyCode).getOrNull()?.fractionDigits
            ?: getFractionDigitsOrDefault(currencyCode)

        val rawMinFrac = options.minFractionDigits ?: fractionDigits
        val rawMaxFrac = options.maxFractionDigits ?: fractionDigits
        // Ensure min <= max when only one side is explicitly set
        val maxFrac = rawMaxFrac
        val minFrac = minOf(rawMinFrac, maxFrac)

        val doubleVal = absAmount.toDoubleOrNull() ?: 0.0

        // Format the number to a plain decimal string with the right precision
        val plainFormatted = formatDecimal(doubleVal, minFrac, maxFrac)

        // Split into integer and fractional parts
        val parts = plainFormatted.split(".")
        val integerPart = parts[0]
        val fractionalPart = if (parts.size > 1) parts[1] else ""

        // Apply grouping
        val groupedInteger = if (options.grouping) {
            applyGrouping(integerPart, locale.groupingSeparator)
        } else {
            integerPart
        }

        // Reassemble with locale decimal separator
        return if (fractionalPart.isNotEmpty()) {
            "$groupedInteger${locale.decimalSeparator}$fractionalPart"
        } else {
            groupedInteger
        }
    }

    /**
     * Detects whether the locale places the currency symbol before or after the number
     * by formatting a sample amount and checking the position.
     */
    private fun detectSymbolPosition(currencyCode: String, symbol: String): SymbolPosition {
        if (symbol.isEmpty()) return SymbolPosition.LEADING

        // Format a sample to detect position
        val sample = impl.formatCurrencyStyle("1", currencyCode)

        // Check if symbol appears before or after the digit
        val symbolIndex = sample.indexOf(symbol)
        val digitIndex = sample.indexOfFirst { it.isDigit() }

        return when {
            symbolIndex < 0 -> SymbolPosition.LEADING // fallback
            digitIndex < 0 -> SymbolPosition.LEADING // fallback
            symbolIndex < digitIndex -> SymbolPosition.LEADING
            else -> SymbolPosition.TRAILING
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

        /**
         * Converts Eastern Arabic and Persian digits to Western (ASCII) digits.
         */
        private fun convertToWesternDigits(text: String): String {
            val sb = StringBuilder(text.length)
            for (ch in text) {
                when (ch) {
                    // Eastern Arabic digits (٠-٩) U+0660..U+0669
                    in '\u0660'..'\u0669' -> sb.append((ch.code - 0x0660 + '0'.code).toChar())
                    // Persian digits (۰-۹) U+06F0..U+06F9
                    in '\u06F0'..'\u06F9' -> sb.append((ch.code - 0x06F0 + '0'.code).toChar())
                    else -> sb.append(ch)
                }
            }
            return sb.toString()
        }

        /**
         * Strips variant/alternate currency symbols that differ from CurrencyMetadata.
         * For example, JVM may produce fullwidth yen ￥ (U+FFE5) instead of ¥ (U+00A5).
         */
        private fun stripVariantCurrencySymbols(text: String, currencyCode: String): String {
            var result = text
            when (currencyCode.uppercase()) {
                "JPY", "CNY" -> {
                    result = result.replace("\uFFE5", "") // Fullwidth yen ￥
                    result = result.replace("\u00A5", "") // Standard yen ¥
                }
                "SAR", "QAR", "OMR" -> {
                    // Arabic rial symbol - various forms
                    result = result.replace("ر.س.", "")
                    result = result.replace("ر.س", "")
                    result = result.replace("\uFDFC", "") // ﷼
                }
            }
            return result
        }

        /**
         * Formats a Double to a plain decimal string with the specified fraction digit range.
         * Uses rounding-half-even (banker's rounding) via standard Kotlin rounding.
         */
        internal fun formatDecimal(value: Double, minFractionDigits: Int, maxFractionDigits: Int): String {
            // Round to maxFractionDigits
            val factor = tenPow(maxFractionDigits)
            val rounded = kotlin.math.round(value * factor) / factor

            val plain = doubleToPlainString(rounded)
            val parts = plain.split(".")
            val intPart = parts[0]
            val rawFrac = if (parts.size > 1) parts[1] else ""

            // Trim trailing zeros down to minFractionDigits, but keep at least minFractionDigits
            val paddedFrac = rawFrac.padEnd(maxFractionDigits, '0').take(maxFractionDigits)
            val trimmedFrac = if (paddedFrac.length > minFractionDigits) {
                val trimmed = paddedFrac.trimEnd('0')
                if (trimmed.length < minFractionDigits) trimmed.padEnd(minFractionDigits, '0')
                else trimmed
            } else {
                paddedFrac.padEnd(minFractionDigits, '0')
            }

            return if (trimmedFrac.isEmpty()) intPart else "$intPart.$trimmedFrac"
        }

        /**
         * Applies locale-specific grouping separators to an integer string.
         */
        internal fun applyGrouping(integerPart: String, separator: Char): String {
            if (integerPart.length <= 3) return integerPart
            val sb = StringBuilder()
            var count = 0
            for (i in integerPart.length - 1 downTo 0) {
                if (count > 0 && count % 3 == 0) {
                    sb.append(separator)
                }
                sb.append(integerPart[i])
                count++
            }
            return sb.reverse().toString()
        }

        private fun tenPow(n: Int): Double {
            var result = 1.0
            repeat(n) { result *= 10.0 }
            return result
        }

        /**
         * Converts a Double to a plain decimal string without scientific notation.
         * This is needed because Kotlin's Double.toString() may use scientific notation
         * for very large or very small numbers.
         */
        internal fun doubleToPlainString(value: Double): String {
            if (value < 0) return "-${doubleToPlainString(-value)}"
            val str = value.toString()
            if (!str.contains('E') && !str.contains('e')) {
                return str
            }

            // Handle scientific notation
            val parts = str.lowercase().split('e')
            val mantissa = parts[0]
            val exponent = parts[1].toInt()

            val mantissaParts = mantissa.split('.')
            val intPart = mantissaParts[0].replace("-", "")
            val fracPart = if (mantissaParts.size > 1) mantissaParts[1] else ""
            val isNeg = mantissa.startsWith("-")

            val allDigits = intPart + fracPart
            val currentDecimalPosition = intPart.length
            val newDecimalPosition = currentDecimalPosition + exponent

            val result = when {
                newDecimalPosition >= allDigits.length -> {
                    allDigits + "0".repeat(newDecimalPosition - allDigits.length)
                }
                newDecimalPosition <= 0 -> {
                    "0." + "0".repeat(-newDecimalPosition) + allDigits
                }
                else -> {
                    allDigits.substring(0, newDecimalPosition) + "." + allDigits.substring(newDecimalPosition)
                }
            }

            return if (isNeg) "-$result" else result
        }
    }
}
