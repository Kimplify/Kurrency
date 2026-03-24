package org.kimplify.kurrency

import java.text.DecimalFormatSymbols
import java.util.Locale

actual class KurrencyLocale(val locale: Locale) {
    actual val languageTag: String
        get() = locale.toLanguageTag()

    actual val decimalSeparator: Char
        get() = DecimalFormatSymbols.getInstance(locale).decimalSeparator

    actual val groupingSeparator: Char
        get() = DecimalFormatSymbols.getInstance(locale).groupingSeparator

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
                    return Result.failure(IllegalArgumentException("Language tag cannot be blank"))
                }

                if (!BCP47_LANGUAGE_TAG_REGEX.matches(languageTag)) {
                    return Result.failure(IllegalArgumentException("Invalid language tag format: $languageTag"))
                }

                val locale = Locale.forLanguageTag(languageTag)
                Result.success(KurrencyLocale(locale))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        actual fun systemLocale(): KurrencyLocale {
            return KurrencyLocale(Locale.getDefault())
        }

        actual val US: KurrencyLocale = KurrencyLocale(Locale.US)
        actual val UK: KurrencyLocale = KurrencyLocale(Locale.UK)
        actual val CANADA: KurrencyLocale = KurrencyLocale(Locale.CANADA)
        actual val CANADA_FRENCH: KurrencyLocale = KurrencyLocale(Locale.CANADA_FRENCH)
        actual val GERMANY: KurrencyLocale = KurrencyLocale(Locale.GERMANY)
        actual val FRANCE: KurrencyLocale = KurrencyLocale(Locale.FRANCE)
        actual val ITALY: KurrencyLocale = KurrencyLocale(Locale.ITALY)
        actual val SPAIN: KurrencyLocale = KurrencyLocale(Locale.forLanguageTag("es-ES"))
        actual val JAPAN: KurrencyLocale = KurrencyLocale(Locale.JAPAN)
        actual val CHINA: KurrencyLocale = KurrencyLocale(Locale.CHINA)
        actual val KOREA: KurrencyLocale = KurrencyLocale(Locale.KOREA)
        actual val BRAZIL: KurrencyLocale = KurrencyLocale(Locale.forLanguageTag("pt-BR"))
        actual val RUSSIA: KurrencyLocale = KurrencyLocale(Locale.forLanguageTag("ru-RU"))
        actual val SAUDI_ARABIA: KurrencyLocale = KurrencyLocale(Locale.forLanguageTag("ar-SA"))
        actual val INDIA: KurrencyLocale = KurrencyLocale(Locale.forLanguageTag("hi-IN"))
        actual val ARABIC_EG: KurrencyLocale = KurrencyLocale(Locale.forLanguageTag("ar-EG"))
        actual val HEBREW: KurrencyLocale = KurrencyLocale(Locale.forLanguageTag("he-IL"))
        actual val PERSIAN: KurrencyLocale = KurrencyLocale(Locale.forLanguageTag("fa-IR"))
        actual val URDU: KurrencyLocale = KurrencyLocale(Locale.forLanguageTag("ur-PK"))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KurrencyLocale) return false
        return locale == other.locale
    }

    override fun hashCode(): Int = locale.hashCode()

    override fun toString(): String = "KurrencyLocale($languageTag)"
}

private val RTL_LANGUAGES = setOf("ar", "he", "iw", "fa", "ur", "dv", "ps", "yi", "ku", "sd")

private fun numeralSystemFromTag(tag: String): NumeralSystem {
    val lang = tag.substringBefore("-").lowercase()
    return when (lang) {
        "fa" -> NumeralSystem.PERSIAN
        "ar" -> NumeralSystem.EASTERN_ARABIC
        "ur", "ps" -> NumeralSystem.EASTERN_ARABIC
        else -> NumeralSystem.WESTERN
    }
}
