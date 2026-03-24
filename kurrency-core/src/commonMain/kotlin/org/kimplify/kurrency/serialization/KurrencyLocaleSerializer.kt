package org.kimplify.kurrency.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.kimplify.kurrency.KurrencyLocale

/**
 * Serializes [KurrencyLocale] as its BCP 47 language tag string (e.g., `"en-US"`).
 *
 * Usage:
 * ```kotlin
 * @Serializable
 * data class UserPrefs(
 *     @Serializable(with = KurrencyLocaleSerializer::class)
 *     val locale: KurrencyLocale
 * )
 * ```
 */
object KurrencyLocaleSerializer : KSerializer<KurrencyLocale> {
    override val descriptor = PrimitiveSerialDescriptor("KurrencyLocale", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: KurrencyLocale) =
        encoder.encodeString(value.languageTag)

    override fun deserialize(decoder: Decoder): KurrencyLocale {
        val tag = decoder.decodeString()
        return KurrencyLocale.fromLanguageTag(tag).getOrElse {
            throw SerializationException("Invalid locale language tag: $tag", it)
        }
    }
}
