package org.kimplify.kurrency.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import org.kimplify.kurrency.CurrencyAmount
import org.kimplify.kurrency.Kurrency

/**
 * Serializes [CurrencyAmount] as a JSON object with `minorUnits` (Long) and `currency` (String code).
 *
 * JSON representation:
 * ```json
 * { "minorUnits": 12345, "currency": "USD" }
 * ```
 *
 * Usage:
 * ```kotlin
 * @Serializable
 * data class Order(
 *     @Serializable(with = CurrencyAmountSerializer::class)
 *     val total: CurrencyAmount
 * )
 * ```
 */
object CurrencyAmountSerializer : KSerializer<CurrencyAmount> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("CurrencyAmount") {
        element<Long>("minorUnits")
        element<String>("currency")
    }

    override fun serialize(encoder: Encoder, value: CurrencyAmount) {
        encoder.encodeStructure(descriptor) {
            encodeLongElement(descriptor, 0, value.minorUnits)
            encodeStringElement(descriptor, 1, value.currency.code)
        }
    }

    override fun deserialize(decoder: Decoder): CurrencyAmount {
        return decoder.decodeStructure(descriptor) {
            var minorUnits: Long? = null
            var currencyCode: String? = null

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> minorUnits = decodeLongElement(descriptor, 0)
                    1 -> currencyCode = decodeStringElement(descriptor, 1)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> throw SerializationException("Unexpected index: $index")
                }
            }

            val units = minorUnits ?: throw SerializationException("Missing field: minorUnits")
            val code = currencyCode ?: throw SerializationException("Missing field: currency")
            val currency = Kurrency.fromCode(code).getOrElse {
                throw SerializationException("Invalid currency code: $code", it)
            }
            CurrencyAmount.of(units, currency)
        }
    }
}
