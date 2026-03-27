package org.kimplify.kurrency

/**
 * Represents the numeral system used for formatting digits in a locale.
 *
 * Different locales use different digit characters:
 * - [WESTERN]: 0-9 (used by most European languages, East Asian, etc.)
 * - [EASTERN_ARABIC]: ٠-٩ (used by Arabic, Urdu, Pashto)
 * - [PERSIAN]: ۰-۹ (used by Persian/Farsi)
 */
enum class NumeralSystem {
    WESTERN,
    EASTERN_ARABIC,
    PERSIAN
}
