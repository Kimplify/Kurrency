package org.kimplify.kurrency

import kotlinx.serialization.Serializable

/**
 * Rounding strategy applied when an amount has more fraction digits than the
 * target scale. Used by [CurrencyFormatOptions.roundingMode].
 */
@Serializable
enum class RoundingMode {
    /** Round to the nearest neighbor; ties go to the even digit (banker's rounding). */
    HALF_EVEN,

    /** Round to the nearest neighbor; ties round away from zero. */
    HALF_UP,

    /** Round toward zero (truncate). */
    DOWN,

    /** Round away from zero. */
    UP,
}
