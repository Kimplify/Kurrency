package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * ISO 4217 codes are upper case. A payload carrying a lower-case code used to produce a Kurrency
 * whose [Kurrency.code] compared unequal to the constant a caller was checking against.
 */
class KurrencyCodeNormalisationTest {

    @Test
    fun aLowerCaseCodeIsStoredUpperCase() {
        assertEquals("AUD", Kurrency.fromCode("aud").getOrThrow().code)
    }

    @Test
    fun aMixedCaseCodeIsStoredUpperCase() {
        assertEquals("GBP", Kurrency.fromCode("gBp").getOrThrow().code)
    }

    @Test
    fun anUpperCaseCodeIsUnchanged() {
        assertEquals("USD", Kurrency.fromCode("USD").getOrThrow().code)
    }

    @Test
    fun aNormalisedCodeEqualsItsConstant() {
        assertEquals(Kurrency.AUD, Kurrency.fromCode("aud").getOrThrow())
    }

    @Test
    fun anInvalidCodeStillFails() {
        assertTrue(Kurrency.fromCode("zzz").isFailure)
    }
}
