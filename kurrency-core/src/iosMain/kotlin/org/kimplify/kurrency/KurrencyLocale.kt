package org.kimplify.kurrency

import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleDecimalSeparator
import platform.Foundation.NSLocaleGroupingSeparator
import platform.Foundation.currentLocale
import platform.Foundation.localeIdentifier

actual class KurrencyLocale(internal val nsLocale: NSLocale) {

    actual val languageTag: String
        get() = nsLocale.localeIdentifier.replace("_", "-")

    actual val decimalSeparator: Char
        get() = (nsLocale.objectForKey(NSLocaleDecimalSeparator) as? String)
            ?.firstOrNull() ?: '.'

    actual val groupingSeparator: Char
        get() = (nsLocale.objectForKey(NSLocaleGroupingSeparator) as? String)
            ?.firstOrNull() ?: ','

    actual val usesCommaAsDecimalSeparator: Boolean
        get() = decimalSeparator == ','

    actual val isRightToLeft: Boolean
        get() = languageTag.substringBefore("-").lowercase() in RTL_LANGUAGES

    actual val numeralSystem: NumeralSystem
        get() = numeralSystemFromTag(languageTag)

    actual companion object {
        actual fun fromLanguageTag(languageTag: String): Result<KurrencyLocale> {
            return try {
                if (languageTag.isBlank()) {
                    return Result.failure(KurrencyError.InvalidLocale(languageTag))
                }

                if (!BCP47_LANGUAGE_TAG_REGEX.matches(languageTag)) {
                    return Result.failure(KurrencyError.InvalidLocale(languageTag))
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
        actual val ARABIC_EG: KurrencyLocale =
            KurrencyLocale(NSLocale(localeIdentifier = "ar_EG"))
        actual val HEBREW: KurrencyLocale =
            KurrencyLocale(NSLocale(localeIdentifier = "he_IL"))
        actual val PERSIAN: KurrencyLocale =
            KurrencyLocale(NSLocale(localeIdentifier = "fa_IR"))
        actual val URDU: KurrencyLocale =
            KurrencyLocale(NSLocale(localeIdentifier = "ur_PK"))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KurrencyLocale) return false
        return nsLocale.localeIdentifier == other.nsLocale.localeIdentifier
    }

    override fun hashCode(): Int = nsLocale.localeIdentifier.hashCode()

    override fun toString(): String = "KurrencyLocale($languageTag)"
}

private val RTL_LANGUAGES = setOf("ar", "he", "fa", "ur", "dv", "ps", "yi", "ku", "sd")

private fun numeralSystemFromTag(tag: String): NumeralSystem {
    val lang = tag.substringBefore("-").lowercase()
    return when (lang) {
        "fa" -> NumeralSystem.PERSIAN
        "ar" -> NumeralSystem.EASTERN_ARABIC
        "ur", "ps" -> NumeralSystem.EASTERN_ARABIC
        else -> NumeralSystem.WESTERN
    }
}