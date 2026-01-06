package org.kimplify.kurrency

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Stateful holder for currency code and amount with reactive formatting.
 *
 * This class is designed for Compose UI scenarios where you need to reactively
 * update and format currency values. It manages mutable state for currency code
 * and amount, automatically providing formatted output that updates when either value changes.
 *
 * ## When to Use
 *
 * - **Use CurrencyState** when building interactive UIs with editable currency fields
 * - **Use direct formatting** (CurrencyFormatter) for one-time formatting or read-only displays
 *
 * ## Example Usage
 *
 * ```kotlin
 * @Composable
 * fun PriceEditor() {
 *     val currencyState = rememberCurrencyState("USD", "99.99")
 *
 *     Column {
 *         Text("Formatted: ${currencyState.formattedAmount}")
 *
 *         TextField(
 *             value = currencyState.amount,
 *             onValueChange = { currencyState.updateAmount(it) }
 *         )
 *     }
 * }
 * ```
 *
 * ## Experimental Status
 *
 * This API is marked experimental because:
 * - The state management approach may evolve based on user feedback
 * - Additional reactive properties may be added
 * - Integration patterns with Compose state hoisting are still being refined
 *
 * @param initialCurrencyCode Initial ISO 4217 currency code (e.g., "USD", "EUR")
 * @param initialAmount Initial amount as a string (default: "0.00")
 */
@ExperimentalKurrency
@Stable
class CurrencyState(
    initialCurrencyCode: String,
    initialAmount: String = "0.00"
) {
    /**
     * Current currency code.
     *
     * Must be a 3-letter ISO 4217 code. Updates to this property trigger recomposition
     * of any Composables observing [formattedAmount] or [currency].
     *
     * Use [updateCurrency] to modify this value.
     */
    var currencyCode by mutableStateOf(initialCurrencyCode)
        private set

    /**
     * Current amount as a string.
     *
     * Can include decimal separators (both `.` and `,` are accepted).
     * Updates to this property trigger recomposition of any Composables
     * observing [formattedAmount].
     *
     * Use [updateAmount] or [updateCurrencyAndAmount] to modify this value.
     */
    var amount by mutableStateOf(initialAmount)
        private set

    /**
     * Result containing the validated currency, or an error if the currency code is invalid.
     *
     * Returns `Result.success(Kurrency)` if [currencyCode] is valid,
     * or `Result.failure(KurrencyError.InvalidCurrencyCode)` otherwise.
     */
    val currencyResult: Result<Kurrency>
        get() = Kurrency.fromCode(currencyCode)

    /**
     * The validated currency object, or null if [currencyCode] is invalid.
     *
     * Convenience property equivalent to `currencyResult.getOrNull()`.
     */
    val currency: Kurrency?
        get() = currencyResult.getOrNull()

    /**
     * Result containing the formatted amount in standard currency style, or an error if formatting fails.
     *
     * Formats using the currency symbol (e.g., "$1,234.56" for USD).
     * Returns failure if either the currency code or amount is invalid.
     */
    val formattedAmountResult: Result<String>
        get() = currencyResult.fold(
            onSuccess = { it.formatAmount(amount) },
            onFailure = { Result.failure(it) }
        )

    /**
     * Result containing the formatted amount in ISO currency style, or an error if formatting fails.
     *
     * Formats using the currency code prefix (e.g., "USD 1,234.56").
     * Returns failure if either the currency code or amount is invalid.
     */
    val formattedAmountIsoResult: Result<String>
        get() = currencyResult.fold(
            onSuccess = { it.formatAmount(amount, CurrencyStyle.Iso) },
            onFailure = { Result.failure(it) }
        )

    /**
     * Formatted amount in standard currency style, or empty string if formatting fails.
     *
     * Convenience property equivalent to `formattedAmountResult.getOrDefault("")`.
     * Reactively updates when [currencyCode] or [amount] changes.
     */
    val formattedAmount: String
        get() = formattedAmountResult.getOrDefault("")

    /**
     * Formatted amount in ISO currency style, or empty string if formatting fails.
     *
     * Convenience property equivalent to `formattedAmountIsoResult.getOrDefault("")`.
     * Reactively updates when [currencyCode] or [amount] changes.
     */
    val formattedAmountIso: String
        get() = formattedAmountIsoResult.getOrDefault("")

    /**
     * Updates the currency code.
     *
     * @param currencyCode New ISO 4217 currency code
     */
    fun updateCurrency(currencyCode: String) {
        KurrencyLog.d { "Updating currency: $currencyCode" }
        this.currencyCode = currencyCode
    }

    /**
     * Updates the amount value.
     *
     * @param newAmount New amount as a string
     */
    fun updateAmount(newAmount: String) {
        KurrencyLog.d { "Updating amount: $newAmount" }
        amount = newAmount
    }

    /**
     * Updates both currency code and amount atomically.
     *
     * More efficient than calling [updateCurrency] and [updateAmount] separately
     * when both values need to change.
     *
     * @param currencyCode New ISO 4217 currency code
     * @param newAmount New amount as a string
     */
    fun updateCurrencyAndAmount(currencyCode: String, newAmount: String) {
        KurrencyLog.d { "Updating currency and amount: currency=$currencyCode, amount=$newAmount" }
        this.currencyCode = currencyCode
        amount = newAmount
    }
}

/**
 * Property delegate that provides formatted currency amounts.
 *
 * Allows using Kotlin property delegation syntax with CurrencyState:
 * ```kotlin
 * val formatted by currencyState.formattedAmount()
 * ```
 *
 * @param state The CurrencyState to format
 * @param style The formatting style to use (Standard or Iso)
 */
@ExperimentalKurrency
class FormattedAmountDelegate(
    private val state: CurrencyState,
    private val style: CurrencyStyle = CurrencyStyle.Standard
) : ReadOnlyProperty<Any?, String> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return state.currencyResult.fold(
            onSuccess = { it.formatAmount(state.amount, style).getOrDefault("") },
            onFailure = { "" }
        )
    }
}

/**
 * Creates a property delegate for formatted amounts.
 *
 * @param style The formatting style (default: Standard)
 * @return A read-only property delegate that formats the amount
 */
@ExperimentalKurrency
fun CurrencyState.formattedAmount(style: CurrencyStyle = CurrencyStyle.Standard) =
    FormattedAmountDelegate(this, style)

/**
 * Remembers a CurrencyState across recompositions, keyed by currency code.
 *
 * The state will be recreated if the currency code changes.
 *
 * @param currencyCode ISO 4217 currency code
 * @param initialAmount Initial amount string (default: "0.00")
 * @return Remembered CurrencyState instance
 */
@ExperimentalKurrency
@Composable
fun rememberCurrencyState(
    currencyCode: String,
    initialAmount: String = "0.00"
): CurrencyState = remember(currencyCode) {
    CurrencyState(currencyCode, initialAmount)
}

/**
 * Remembers a CurrencyState across recompositions, keyed by currency code and amount.
 *
 * The state will be recreated if either the currency code or initial amount changes.
 *
 * @param currencyCode ISO 4217 currency code
 * @param initialAmount Initial amount as a double
 * @return Remembered CurrencyState instance
 */
@ExperimentalKurrency
@Composable
fun rememberCurrencyState(
    currencyCode: String,
    initialAmount: Double
): CurrencyState = remember(currencyCode, initialAmount) {
    CurrencyState(currencyCode, initialAmount.toString())
}
