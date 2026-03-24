package org.kimplify.kurrency

/**
 * Represents a locale for currency formatting across all platforms.
 *
 * This class wraps platform-specific locale implementations and provides
 * a consistent API for locale handling in Kurrency.
 *
 * ## Platform Behavior Differences
 *
 * On **Android/JVM/Web**, the locale provided to [CurrencyFormatter] directly controls
 * formatting output (decimal separators, grouping, symbol placement, etc.).
 *
 * On **iOS**, the locale and formatting locale are separate concepts. iOS users can customize
 * formatting preferences (decimal/grouping separators) in Settings independently of their
 * language/region. [CurrencyFormatter] on iOS always uses `NSLocale.currentLocale` for
 * formatting to respect these custom preferences, regardless of which [KurrencyLocale] is
 * provided. The properties on this class ([decimalSeparator], [groupingSeparator]) return
 * the locale's standard defaults, not the user's custom overrides.
 *
 * @property languageTag The BCP 47 language tag (e.g., "en-US", "de-DE", "ja-JP")
 * @property platformLocale The underlying platform-specific locale object
 *   - Android/JVM: `java.util.Locale`
 *   - iOS: `platform.Foundation.NSLocale`
 *   - Web: `String` (language tag, used with Intl APIs)
 */
expect class KurrencyLocale {

    val languageTag: String

    /**
     * The decimal separator character for this locale.
     * For example: '.' for US English, ',' for German.
     */
    val decimalSeparator: Char

    /**
     * The grouping (thousands) separator character for this locale.
     * For example: ',' for US English, '.' for German, ' ' for some locales.
     */
    val groupingSeparator: Char

    /**
     * Returns true if this locale uses comma as the decimal separator.
     * This is a convenience property equivalent to `decimalSeparator == ','`.
     *
     * Useful for:
     * - Input validation (accepting "100,50" vs "100.50")
     * - Display formatting hints
     * - Keyboard configuration
     *
     * Examples:
     * - `true` for: de-DE, fr-FR, es-ES, it-IT, pt-BR, ru-RU, etc.
     * - `false` for: en-US, en-GB, ja-JP, zh-CN, etc.
     */
    val usesCommaAsDecimalSeparator: Boolean

    /**
     * Returns true if this locale uses right-to-left text direction.
     *
     * RTL locales include Arabic, Hebrew, Persian, Urdu, and others.
     * This can be used to adjust UI layout and text alignment for currency displays.
     */
    val isRightToLeft: Boolean

    /**
     * The numeral system used by this locale for digit representation.
     *
     * @see NumeralSystem
     */
    val numeralSystem: NumeralSystem

    companion object {
        /**
         * Creates a KurrencyLocale from a BCP 47 language tag.
         *
         * @param languageTag The language tag string (e.g., "en-US", "fr-FR")
         * @return Result with KurrencyLocale on success, or failure if the tag is invalid
         */
        fun fromLanguageTag(languageTag: String): Result<KurrencyLocale>

        /**
         * Returns the system's current locale.
         */
        fun systemLocale(): KurrencyLocale

        /** United States English (en-US) */
        val US: KurrencyLocale

        /** United Kingdom English (en-GB) */
        val UK: KurrencyLocale

        /** Canadian English (en-CA) */
        val CANADA: KurrencyLocale

        /** Canadian French (fr-CA) */
        val CANADA_FRENCH: KurrencyLocale

        /** German (Germany) (de-DE) */
        val GERMANY: KurrencyLocale

        /** French (France) (fr-FR) */
        val FRANCE: KurrencyLocale

        /** Italian (Italy) (it-IT) */
        val ITALY: KurrencyLocale

        /** Spanish (Spain) (es-ES) */
        val SPAIN: KurrencyLocale

        /** Japanese (Japan) (ja-JP) */
        val JAPAN: KurrencyLocale

        /** Chinese Simplified (China) (zh-CN) */
        val CHINA: KurrencyLocale

        /** Korean (South Korea) (ko-KR) */
        val KOREA: KurrencyLocale

        /** Portuguese (Brazil) (pt-BR) */
        val BRAZIL: KurrencyLocale

        /** Russian (Russia) (ru-RU) */
        val RUSSIA: KurrencyLocale

        /** Arabic (Saudi Arabia) (ar-SA) */
        val SAUDI_ARABIA: KurrencyLocale

        /** Hindi (India) (hi-IN) */
        val INDIA: KurrencyLocale

        /** Arabic (Egypt) (ar-EG) */
        val ARABIC_EG: KurrencyLocale

        /** Hebrew (Israel) (he-IL) */
        val HEBREW: KurrencyLocale

        /** Persian (Iran) (fa-IR) */
        val PERSIAN: KurrencyLocale

        /** Urdu (Pakistan) (ur-PK) */
        val URDU: KurrencyLocale
    }
}