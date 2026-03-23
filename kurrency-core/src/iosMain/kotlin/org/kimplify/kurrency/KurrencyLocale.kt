package org.kimplify.kurrency

import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleDecimalSeparator
import platform.Foundation.NSLocaleGroupingSeparator
import platform.Foundation.currentLocale
import platform.Foundation.localeIdentifier

/**
 * iOS implementation of KurrencyLocale using NSLocale.
 *
 * ## iOS Locale vs Formatting Locale
 *
 * On iOS, there is an important distinction between the **locale** and the **formatting locale**:
 *
 * - **Locale ([NSLocale] with a fixed identifier):** Represents a language/region combination
 *   (e.g., "en_US", "de_DE") with its standard, default formatting rules. This is what
 *   [KurrencyLocale] wraps — it provides the locale's standard decimal separator, grouping
 *   separator, and other properties as defined by Unicode CLDR.
 *
 * - **Formatting locale ([NSLocale.currentLocale]):** Reflects the user's **actual** formatting
 *   preferences from iOS Settings > General > Language & Region. Users can customize decimal
 *   separators, grouping separators, and other formatting details independently of their
 *   chosen language/region. For example, a user with "en-US" locale can change their decimal
 *   separator to a comma.
 *
 * The properties on this class ([decimalSeparator], [groupingSeparator]) return the **standard**
 * locale defaults, not the user's custom overrides. For formatting that respects the user's
 * custom preferences, use [CurrencyFormatter], which uses [NSLocale.currentLocale] internally.
 *
 * @see CurrencyFormatterImpl for how formatting respects user's custom preferences
 */
actual class KurrencyLocale(internal val nsLocale: NSLocale) {

    actual val languageTag: String
        get() = nsLocale.localeIdentifier.replace("_", "-")

    /**
     * Returns the **standard** decimal separator for this locale as defined by Unicode CLDR.
     *
     * This is the locale's default, **not** the user's custom preference from iOS Settings.
     * For example, `KurrencyLocale.US.decimalSeparator` always returns `'.'`, even if the
     * user has customized their decimal separator to `','` in iOS Settings.
     *
     * For formatting that respects the user's custom preferences, use [CurrencyFormatter].
     */
    actual val decimalSeparator: Char
        get() = (nsLocale.objectForKey(NSLocaleDecimalSeparator) as? String)
            ?.firstOrNull() ?: '.'

    /**
     * Returns the **standard** grouping separator for this locale as defined by Unicode CLDR.
     *
     * This is the locale's default, **not** the user's custom preference from iOS Settings.
     * For example, `KurrencyLocale.US.groupingSeparator` always returns `','`, even if the
     * user has customized their grouping separator in iOS Settings.
     *
     * For formatting that respects the user's custom preferences, use [CurrencyFormatter].
     */
    actual val groupingSeparator: Char
        get() = (nsLocale.objectForKey(NSLocaleGroupingSeparator) as? String)
            ?.firstOrNull() ?: ','

    actual val usesCommaAsDecimalSeparator: Boolean
        get() = decimalSeparator == ','

    actual companion object {
        actual fun fromLanguageTag(languageTag: String): Result<KurrencyLocale> {
            return try {
                if (languageTag.isBlank()) {
                    return Result.failure(IllegalArgumentException("Language tag cannot be blank"))
                }

                if (!BCP47_LANGUAGE_TAG_REGEX.matches(languageTag)) {
                    return Result.failure(IllegalArgumentException("Invalid language tag format: $languageTag"))
                }

                val localeIdentifier = languageTag.replace("-", "_")
                val locale = NSLocale(localeIdentifier = localeIdentifier)
                Result.success(KurrencyLocale(locale))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        actual fun systemLocale(): KurrencyLocale {
            return KurrencyLocale(NSLocale.currentLocale)
        }

        // Predefined locales
        actual val US: KurrencyLocale = KurrencyLocale(NSLocale(localeIdentifier = "en_US"))
        actual val UK: KurrencyLocale = KurrencyLocale(NSLocale(localeIdentifier = "en_GB"))
        actual val CANADA: KurrencyLocale = KurrencyLocale(NSLocale(localeIdentifier = "en_CA"))
        actual val CANADA_FRENCH: KurrencyLocale =
            KurrencyLocale(NSLocale(localeIdentifier = "fr_CA"))
        actual val GERMANY: KurrencyLocale = KurrencyLocale(NSLocale(localeIdentifier = "de_DE"))
        actual val FRANCE: KurrencyLocale = KurrencyLocale(NSLocale(localeIdentifier = "fr_FR"))
        actual val ITALY: KurrencyLocale = KurrencyLocale(NSLocale(localeIdentifier = "it_IT"))
        actual val SPAIN: KurrencyLocale = KurrencyLocale(NSLocale(localeIdentifier = "es_ES"))
        actual val JAPAN: KurrencyLocale = KurrencyLocale(NSLocale(localeIdentifier = "ja_JP"))
        actual val CHINA: KurrencyLocale = KurrencyLocale(NSLocale(localeIdentifier = "zh_CN"))
        actual val KOREA: KurrencyLocale = KurrencyLocale(NSLocale(localeIdentifier = "ko_KR"))
        actual val BRAZIL: KurrencyLocale = KurrencyLocale(NSLocale(localeIdentifier = "pt_BR"))
        actual val RUSSIA: KurrencyLocale = KurrencyLocale(NSLocale(localeIdentifier = "ru_RU"))
        actual val SAUDI_ARABIA: KurrencyLocale =
            KurrencyLocale(NSLocale(localeIdentifier = "ar_SA"))
        actual val INDIA: KurrencyLocale = KurrencyLocale(NSLocale(localeIdentifier = "hi_IN"))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KurrencyLocale) return false
        return nsLocale.localeIdentifier == other.nsLocale.localeIdentifier
    }

    override fun hashCode(): Int = nsLocale.localeIdentifier.hashCode()

    override fun toString(): String = "KurrencyLocale($languageTag)"
}