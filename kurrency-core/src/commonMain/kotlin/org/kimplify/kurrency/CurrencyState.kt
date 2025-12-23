package org.kimplify.kurrency

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@ExperimentalKurrency
@Stable
class CurrencyState(
    initialCurrencyCode: String,
    initialAmount: String = "0.00"
) {
    var currencyCode by mutableStateOf(initialCurrencyCode)
        private set

    var amount by mutableStateOf(initialAmount)
        private set

    val currencyResult: Result<Kurrency>
        get() = Kurrency.fromCode(currencyCode)

    val currency: Kurrency?
        get() = currencyResult.getOrNull()

    val formattedAmountResult: Result<String>
        get() = currencyResult.fold(
            onSuccess = { it.formatAmount(amount) },
            onFailure = { Result.failure(it) }
        )

    val formattedAmountIsoResult: Result<String>
        get() = currencyResult.fold(
            onSuccess = { it.formatAmount(amount, CurrencyStyle.Iso) },
            onFailure = { Result.failure(it) }
        )

    val formattedAmount: String
        get() = formattedAmountResult.getOrDefault("")

    val formattedAmountIso: String
        get() = formattedAmountIsoResult.getOrDefault("")

    fun updateCurrency(currencyCode: String) {
        KurrencyLog.d { "Updating currency: $currencyCode" }
        this.currencyCode = currencyCode
    }

    fun updateAmount(newAmount: String) {
        KurrencyLog.d { "Updating amount: $newAmount" }
        amount = newAmount
    }

    fun updateCurrencyAndAmount(currencyCode: String, newAmount: String) {
        KurrencyLog.d { "Updating currency and amount: currency=$currencyCode, amount=$newAmount" }
        this.currencyCode = currencyCode
        amount = newAmount
    }
}

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

@ExperimentalKurrency
fun CurrencyState.formattedAmount(style: CurrencyStyle = CurrencyStyle.Standard) =
    FormattedAmountDelegate(this, style)

@ExperimentalKurrency
@Composable
fun rememberCurrencyState(
    currencyCode: String,
    initialAmount: String = "0.00"
): CurrencyState = remember(currencyCode) {
    CurrencyState(currencyCode, initialAmount)
}

@ExperimentalKurrency
@Composable
fun rememberCurrencyState(
    currencyCode: String,
    initialAmount: Double
): CurrencyState = remember(currencyCode, initialAmount) {
    CurrencyState(currencyCode, initialAmount.toString())
}
