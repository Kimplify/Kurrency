package org.kimplify.kurrency.serialization

import kotlinx.serialization.json.Json

/**
 * Pre-configured [Json] instance for Kurrency serialization.
 *
 * Configuration:
 * - `explicitNulls = false` — null fields are omitted from output
 * - `encodeDefaults = false` — fields with default values are omitted from output
 * - `ignoreUnknownKeys = true` — unknown JSON keys are silently ignored during deserialization
 */
val KurrencyJson = Json {
    explicitNulls = false
    encodeDefaults = false
    ignoreUnknownKeys = true
}
