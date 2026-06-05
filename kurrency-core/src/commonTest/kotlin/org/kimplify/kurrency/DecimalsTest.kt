package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DecimalsTest {

    @Test fun halfEven_tieToEven_roundsDown() {
        assertEquals("2.66", Decimals.roundToScale("2.665", 2, RoundingMode.HALF_EVEN))
    }

    @Test fun halfEven_tieToEven_roundsUp() {
        assertEquals("2.68", Decimals.roundToScale("2.675", 2, RoundingMode.HALF_EVEN))
    }

    @Test fun halfEven_aboveHalf_roundsUp() {
        assertEquals("2.68", Decimals.roundToScale("2.6751", 2, RoundingMode.HALF_EVEN))
    }

    @Test fun halfEven_belowHalf_roundsDown() {
        assertEquals("2.67", Decimals.roundToScale("2.6749", 2, RoundingMode.HALF_EVEN))
    }

    @Test fun halfEven_scaleZero() {
        assertEquals("2", Decimals.roundToScale("2.5", 0, RoundingMode.HALF_EVEN))
        assertEquals("4", Decimals.roundToScale("3.5", 0, RoundingMode.HALF_EVEN))
    }

    @Test fun halfUp_tie_roundsAwayFromZero() {
        assertEquals("2.67", Decimals.roundToScale("2.665", 2, RoundingMode.HALF_UP))
        assertEquals("3", Decimals.roundToScale("2.5", 0, RoundingMode.HALF_UP))
    }

    @Test fun down_truncates() {
        assertEquals("2.67", Decimals.roundToScale("2.679", 2, RoundingMode.DOWN))
        assertEquals("2", Decimals.roundToScale("2.999", 0, RoundingMode.DOWN))
    }

    @Test fun up_anyRemainderRoundsUp() {
        assertEquals("2.68", Decimals.roundToScale("2.671", 2, RoundingMode.UP))
        assertEquals("2.67", Decimals.roundToScale("2.670", 2, RoundingMode.UP))
        assertEquals("3", Decimals.roundToScale("2.001", 0, RoundingMode.UP))
    }

    @Test fun carryPropagates() {
        assertEquals("10.00", Decimals.roundToScale("9.999", 2, RoundingMode.HALF_UP))
        assertEquals("1", Decimals.roundToScale("0.999", 0, RoundingMode.HALF_UP))
        assertEquals("100.0", Decimals.roundToScale("99.95", 1, RoundingMode.HALF_UP))
    }

    @Test fun padsWhenFewerDigitsThanScale() {
        assertEquals("2.50", Decimals.roundToScale("2.5", 2, RoundingMode.HALF_EVEN))
        assertEquals("2.00", Decimals.roundToScale("2", 2, RoundingMode.HALF_EVEN))
    }

    @Test fun beyondDoublePrecision_isExact() {
        assertEquals(
            "9007199254740993.01",
            Decimals.roundToScale("9007199254740993.005", 2, RoundingMode.HALF_UP),
        )
    }

    @Test fun isZero_variants() {
        assertTrue(Decimals.isZero("0"))
        assertTrue(Decimals.isZero("0.00"))
        assertTrue(Decimals.isZero("-0.0"))
        assertFalse(Decimals.isZero("0.01"))
    }

    @Test fun isNegative_excludesNegativeZero() {
        assertTrue(Decimals.isNegative("-3.50"))
        assertFalse(Decimals.isNegative("-0.00"))
        assertFalse(Decimals.isNegative("3.50"))
    }

    @Test fun abs_stripsSign() {
        assertEquals("3.50", Decimals.abs("-3.50"))
        assertEquals("3.50", Decimals.abs("3.50"))
    }

    @Test fun abs_stripsLeadingPlus() {
        assertEquals("3.50", Decimals.abs("+3.50"))
    }

    @Test fun isOne_variants() {
        assertTrue(Decimals.isOne("1"))
        assertTrue(Decimals.isOne("1.00"))
        assertTrue(Decimals.isOne("-1.0"))
        assertFalse(Decimals.isOne("1.5"))
        assertFalse(Decimals.isOne("10"))
    }

    @Test fun carryGrowsIntegerWidthAtNonZeroScale() {
        assertEquals("100.00", Decimals.roundToScale("99.999", 2, RoundingMode.HALF_UP))
    }

    @Test fun halfEven_tieWithTrailingNonZero_roundsUp() {
        assertEquals("2.67", Decimals.roundToScale("2.6650001", 2, RoundingMode.HALF_EVEN))
    }
}
