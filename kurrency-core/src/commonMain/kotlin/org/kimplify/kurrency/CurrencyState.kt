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
    var currency by mutableStateOf(Kurrency.fromCode(initialCurrencyCode).getOrElse { Kurrency.USD })
        private set

    var amount by mutableStateOf(initialAmount)
        private set

    val formattedAmountResult: Result<String>
        get() = currency.formatAmount(amount)

    val formattedAmountIsoResult: Result<String>
        get() = currency.formatAmount(amount, CurrencyStyle.Iso)

    val formattedAmount: String
        get() = formattedAmountResult.getOrDefault("")

    val formattedAmountIso: String
        get() = formattedAmountIsoResult.getOrDefault("")

    fun updateCurrency(currencyCode: String) {
        KurrencyLog.d { "Updating currency: $currencyCode" }
        currency = Kurrency.fromCode(currencyCode).getOrElse { Kurrency.USD }
    }

    fun updateAmount(newAmount: String) {
        KurrencyLog.d { "Updating amount: $newAmount" }
        amount = newAmount
    }

    fun updateCurrencyAndAmount(currencyCode: String, newAmount: String) {
        KurrencyLog.d { "Updating currency and amount: currency=$currencyCode, amount=$newAmount" }
        currency = Kurrency.fromCode(currencyCode).getOrElse { Kurrency.USD }
        amount = newAmount
    }
}

@ExperimentalKurrency
class FormattedAmountDelegate(
    private val state: CurrencyState,
    private val style: CurrencyStyle = CurrencyStyle.Standard
) : ReadOnlyProperty<Any?, String> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return state.currency.formatAmount(state.amount, style).getOrDefault("")
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
