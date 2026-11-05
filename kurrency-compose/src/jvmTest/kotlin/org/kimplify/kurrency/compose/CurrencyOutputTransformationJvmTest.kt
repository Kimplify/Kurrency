package org.kimplify.kurrency.compose

import androidx.compose.foundation.text.input.TextFieldState
import kotlin.test.Test
import kotlin.test.assertEquals
import org.kimplify.kurrency.CurrencyFormatter
import org.kimplify.kurrency.KurrencyLocale

class CurrencyOutputTransformationJvmTest {

    @Test
    fun pseudoCurrencyUsesZeroInputScaleWhenMetadataIsNegative() {
        val state = TextFieldState("1234")
        val transformation = CurrencyOutputTransformation("XDR", KurrencyLocale.US)

        state.edit {
            with(transformation) { transformOutput() }
        }

        val expected = CurrencyFormatter(KurrencyLocale.US).formatCurrencyStyle("1234", "XDR")
        assertEquals(expected, state.text.toString())
    }
}
