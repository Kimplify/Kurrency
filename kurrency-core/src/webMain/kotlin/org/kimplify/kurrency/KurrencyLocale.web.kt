package org.kimplify.kurrency


internal expect fun getBrowserLocale(): String

internal expect fun getDecimalSeparatorForLocale(locale: String): String

internal expect fun getGroupingSeparatorForLocale(locale: String): String

actual class KurrencyLocale(actual val languageTag: String) {

    actual val decimalSeparator: Char
        get() = getDecimalSeparatorForLocale(languageTag).firstOrNull() ?: '.'

    actual val groupingSeparator: Char
        get() = getGroupingSeparatorForLocale(languageTag).firstOrNull() ?: ','

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
                    Result.failure(KurrencyError.InvalidLocale(languageTag))
                } else if (!isValidLanguageTag(languageTag)) {
                    Result.failure(KurrencyError.InvalidLocale(languageTag))
                } else {
                    Result.success(KurrencyLocale(languageTag))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        actual fun systemLocale(): KurrencyLocale {
            val browserLocale = getBrowserLocale()
            return KurrencyLocale(browserLocale)
        }

        actual val US: KurrencyLocale = KurrencyLocale("en-US")
        actual val UK: KurrencyLocale = KurrencyLocale("en-GB")
        actual val CANADA: KurrencyLocale = KurrencyLocale("en-CA")
        actual val CANADA_FRENCH: KurrencyLocale = KurrencyLocale("fr-CA")
        actual val GERMANY: KurrencyLocale = KurrencyLocale("de-DE")
        actual val FRANCE: KurrencyLocale = KurrencyLocale("fr-FR")
        actual val ITALY: KurrencyLocale = KurrencyLocale("it-IT")
        actual val SPAIN: KurrencyLocale = KurrencyLocale("es-ES")
        actual val JAPAN: KurrencyLocale = KurrencyLocale("ja-JP")
        actual val CHINA: KurrencyLocale = KurrencyLocale("zh-CN")
        actual val KOREA: KurrencyLocale = KurrencyLocale("ko-KR")
        actual val BRAZIL: KurrencyLocale = KurrencyLocale("pt-BR")
        actual val RUSSIA: KurrencyLocale = KurrencyLocale("ru-RU")
        actual val SAUDI_ARABIA: KurrencyLocale = KurrencyLocale("ar-SA")
        actual val INDIA: KurrencyLocale = KurrencyLocale("hi-IN")
        actual val ARABIC_EG: KurrencyLocale = KurrencyLocale("ar-EG")
        actual val HEBREW: KurrencyLocale = KurrencyLocale("he-IL")
        actual val PERSIAN: KurrencyLocale = KurrencyLocale("fa-IR")
        actual val URDU: KurrencyLocale = KurrencyLocale("ur-PK")

        private fun isValidLanguageTag(tag: String): Boolean =
            BCP47_LANGUAGE_TAG_REGEX.matches(tag)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KurrencyLocale) return false
        return languageTag == other.languageTag
    }

    override fun hashCode(): Int = languageTag.hashCode()

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
