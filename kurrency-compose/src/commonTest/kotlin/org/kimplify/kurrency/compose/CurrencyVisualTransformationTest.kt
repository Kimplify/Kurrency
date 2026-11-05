package org.kimplify.kurrency.compose

import androidx.compose.ui.text.AnnotatedString
import kotlin.test.Test
import kotlin.test.assertEquals
import org.kimplify.kurrency.KurrencyLocale

class CurrencyVisualTransformationTest {

    @Test
    fun preservesExistingMinorUnitFormatting() {
        val transformation = CurrencyVisualTransformation("USD", KurrencyLocale.US)
        val examples = listOf(
            "" to "$0.00",
            "1" to "$0.01",
            "1234" to "$12.34",
            "0001234" to "$12.34",
            "a1b2" to "$0.12",
        )

        for ((raw, expected) in examples) {
            assertEquals(expected, transformation.filter(AnnotatedString(raw)).text.text)
        }
    }

    @Test
    fun customScaleKeepsCurrencyDisplayPrecision() {
        val transformation = CurrencyVisualTransformation("USD", KurrencyLocale.US, fractionDigits = 3)

        assertEquals("$1.23", transformation.filter(AnnotatedString("1234")).text.text)
    }
}
