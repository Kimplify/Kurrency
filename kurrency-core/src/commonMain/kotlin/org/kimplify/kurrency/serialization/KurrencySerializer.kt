package org.kimplify.kurrency.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.kimplify.kurrency.Kurrency

/**
 * Serializes [Kurrency] as its ISO 4217 currency code string (e.g., `"USD"`).
 *
 * Usage:
 * ```kotlin
 * @Serializable
 * data class Price(
 *     @Serializable(with = KurrencySerializer::class)
 *     val currency: Kurrency
 * )
 * ```
 */
object KurrencySerializer : KSerializer<Kurrency> {
    override val descriptor = PrimitiveSerialDescriptor("Kurrency", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Kurrency) = encoder.encodeString(value.code)

    override fun deserialize(decoder: Decoder): Kurrency {
        val code = decoder.decodeString()
        return Kurrency.fromCode(code).getOrElse {
            throw SerializationException("Invalid currency code: $code", it)
        }
    }
}
