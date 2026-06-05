# Kurrency 💱

![CI](https://github.com/Kimplify/Kurrency/workflows/CI/badge.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-2.4.0-blue.svg?style=flat&logo=kotlin)
![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin_Multiplatform-2.4.0-blue.svg?style=flat&logo=kotlin)
![Android](https://img.shields.io/badge/Android-24%2B-green.svg?style=flat&logo=android)
![iOS](https://img.shields.io/badge/iOS-13%2B-lightgrey.svg?style=flat&logo=apple)
![JVM](https://img.shields.io/badge/JVM-17%2B-orange.svg?style=flat&logo=openjdk)
![JS](https://img.shields.io/badge/JS-IR-yellow.svg?style=flat&logo=javascript)
![WasmJs](https://img.shields.io/badge/WasmJs-✓-purple.svg?style=flat&logo=webassembly)
![License](https://img.shields.io/badge/License-Apache%202.0-orange.svg?style=flat)

Type-safe currency formatting for Kotlin Multiplatform, with locale-aware output and optional Compose integration.

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Locale Management](#locale-management)
- [Compose Integration](#compose-integration)
- [API Reference](#api-reference)
- [Error Handling](#error-handling)
- [Platform Support](#platform-support)
- [License](#license)

## Features

- 🌍 **Multiplatform** — Android, iOS, JVM, JS, and WasmJs from a single API
- 🌐 **Locale-aware** — separators, grouping, and symbol placement per locale
- 🎨 **Compose integration** — ready-to-use composables with reactive locale updates
- ✅ **Type-safe errors** — `Result`-based API with a sealed `KurrencyError` hierarchy
- 🔄 **Multiple styles** — symbol, ISO code, and compact formatting
- 📦 **Lightweight** — minimal dependencies

## Installation

### Core library

```kotlin
dependencies {
    implementation("org.kimplify:kurrency-core:0.3.1")
}
```

### Compose integration (optional)

```kotlin
dependencies {
    implementation("org.kimplify:kurrency-core:0.3.1")
    implementation("org.kimplify:kurrency-compose:0.3.1")
}
```

## Quick Start

Create a `CurrencyFormatter` for a locale (the no-argument constructor uses the system locale), then format with `Result`-based methods.

```kotlin
import org.kimplify.kurrency.CurrencyFormatter

val formatter = CurrencyFormatter() // system locale

formatter.formatCurrencyStyleResult("1234.56", "USD")    // "$1,234.56" (en-US)
formatter.formatIsoCurrencyStyleResult("1234.56", "USD") // "USD 1,234.56"
formatter.formatCompactStyleResult("1234567.89", "USD")  // "$1.2M"
```

Fraction digits are a property of the currency and never vary by locale, so they are available statically:

```kotlin
CurrencyFormatter.getFractionDigits("USD")          // Result.success(2)
CurrencyFormatter.getFractionDigitsOrDefault("JPY") // 0
```

### Locale vs. fraction digits

A currency always uses the same number of fraction digits, regardless of where it is displayed:

- USD → 2 digits, JPY → 0 digits, BHD → 3 digits

The **locale** only controls presentation: decimal separator, grouping separator, symbol placement, and spacing.

```kotlin
val us = CurrencyFormatter(KurrencyLocale.US)
val de = CurrencyFormatter(KurrencyLocale.GERMANY)

us.formatCurrencyStyleResult("1234.56", "USD") // "$1,234.56"
de.formatCurrencyStyleResult("1234.56", "USD") // "1.234,56 $"

// JPY has no fraction digits in any locale
us.formatCurrencyStyleResult("1234", "JPY")    // "¥1,234"
```

### Working with the Currency type

Currencies are represented by `Kurrency`. Build one from a code with `fromCode`, or use a predefined constant.

```kotlin
import org.kimplify.kurrency.Kurrency

val usd = Kurrency.fromCode("USD").getOrThrow()
val eur = Kurrency.EUR
val valid = Kurrency.isValid("USD") // true

usd.formatAmount("1234.56").getOrNull() // "$1,234.56" (system locale)
eur.formatAmount("1234.56", locale = KurrencyLocale.GERMANY).getOrNull() // "1.234,56 €"
```

## Locale Management

### Predefined locales

```kotlin
import org.kimplify.kurrency.CurrencyFormatter
import org.kimplify.kurrency.KurrencyLocale

val formatter = CurrencyFormatter(KurrencyLocale.JAPAN)
formatter.formatCurrencyStyleResult("1234.56", "JPY") // "¥1,235"
```

Available constants include:

```kotlin
KurrencyLocale.US             // en-US
KurrencyLocale.UK             // en-GB
KurrencyLocale.CANADA         // en-CA
KurrencyLocale.CANADA_FRENCH  // fr-CA
KurrencyLocale.GERMANY        // de-DE
KurrencyLocale.FRANCE         // fr-FR
KurrencyLocale.ITALY          // it-IT
KurrencyLocale.SPAIN          // es-ES
KurrencyLocale.JAPAN          // ja-JP
KurrencyLocale.CHINA          // zh-CN
KurrencyLocale.KOREA          // ko-KR
KurrencyLocale.BRAZIL         // pt-BR
KurrencyLocale.RUSSIA         // ru-RU
KurrencyLocale.SAUDI_ARABIA   // ar-SA
KurrencyLocale.INDIA          // hi-IN
```

### Custom and system locales

```kotlin
// BCP 47 language tag
val austrian = KurrencyLocale.fromLanguageTag("de-AT").getOrNull()

// Device locale
val system = KurrencyLocale.systemLocale()
```

## Compose Integration

Add `kurrency-compose` for Jetpack Compose Multiplatform support.

### rememberCurrencyFormatter

The formatter is recreated when the locale changes (key-based recomposition). Formatting is cheap, so call it directly during composition rather than caching it.

```kotlin
import org.kimplify.kurrency.compose.rememberCurrencyFormatter
import org.kimplify.kurrency.KurrencyLocale

@Composable
fun PriceDisplay(amount: String, currencyCode: String) {
    var locale by remember { mutableStateOf(KurrencyLocale.US) }
    val formatter = rememberCurrencyFormatter(locale)

    Column {
        Text("Price: ${formatter.formatCurrencyStyle(amount, currencyCode)}")
        Button(onClick = { locale = KurrencyLocale.GERMANY }) {
            Text("Switch to German locale")
        }
    }
}
```

### LocalCurrencyFormatter

Provide a formatter to an entire subtree via `CompositionLocal`.

```kotlin
import org.kimplify.kurrency.compose.ProvideCurrencyFormatter
import org.kimplify.kurrency.compose.LocalCurrencyFormatter
import org.kimplify.kurrency.KurrencyLocale

@Composable
fun App() {
    ProvideCurrencyFormatter(locale = KurrencyLocale.US) {
        ProductScreen()
    }
}

@Composable
fun ProductScreen() {
    val formatter = LocalCurrencyFormatter.current
    Text("Price: ${formatter.formatCurrencyStyle("99.99", "USD")}")
}
```

### Compose locale interop

Convert a Compose `Locale` to a `KurrencyLocale`:

```kotlin
import androidx.compose.ui.text.intl.Locale
import org.kimplify.kurrency.KurrencyLocale
import org.kimplify.kurrency.compose.fromComposeLocale

@Composable
fun MyComposable() {
    val kurrencyLocale = KurrencyLocale.fromComposeLocale(Locale.current)
    val formatter = rememberCurrencyFormatter(kurrencyLocale)
}
```

## API Reference

### CurrencyFormatter

A formatter bound to a locale. Fraction-digit lookups are available on the companion object.

```kotlin
// Construction
CurrencyFormatter(locale: KurrencyLocale = KurrencyLocale.systemLocale())

// Instance formatting (Result-based)
fun formatCurrencyStyleResult(amount: String, currencyCode: String): Result<String>
fun formatIsoCurrencyStyleResult(amount: String, currencyCode: String): Result<String>
fun formatCompactStyleResult(amount: String, currencyCode: String): Result<String>

// Companion (fraction digits do not vary by locale)
CurrencyFormatter.getFractionDigits(currencyCode: String): Result<Int>
CurrencyFormatter.getFractionDigitsOrDefault(currencyCode: String): Int
```

### Kurrency

```kotlin
// Factory and validation (no public constructor)
Kurrency.fromCode(code: String): Result<Kurrency>
Kurrency.isValid(code: String): Boolean

// Predefined constants: USD, EUR, GBP, JPY, AUD, CAD, CHF, CNY, INR, KRW, MXN, ...

// Instance formatting
fun formatAmount(amount: String, style: CurrencyStyle = CurrencyStyle.Standard, locale: KurrencyLocale = KurrencyLocale.systemLocale()): Result<String>
fun formatAmount(amount: Double, style: CurrencyStyle = CurrencyStyle.Standard, locale: KurrencyLocale = KurrencyLocale.systemLocale()): Result<String>
fun formatAmountOrEmpty(amount: String, style: CurrencyStyle = CurrencyStyle.Standard, locale: KurrencyLocale = KurrencyLocale.systemLocale()): String
```

`CurrencyStyle` is one of `Standard` (symbol), `Iso` (ISO code), or `Accounting` (parentheses for negatives).

### CurrencyFormat (interface)

The shared surface implemented per platform. These methods return a plain `String` (falling back to the input on failure); use the `*Result` methods on `CurrencyFormatter` for explicit error handling.

```kotlin
interface CurrencyFormat {
    fun getFractionDigitsOrDefault(currencyCode: String, default: Int = 2): Int
    fun formatCurrencyStyle(amount: String, currencyCode: String): String
    fun formatIsoCurrencyStyle(amount: String, currencyCode: String): String
    fun formatCompactStyle(amount: String, currencyCode: String): String
}
```

### KurrencyLocale

```kotlin
KurrencyLocale.fromLanguageTag(languageTag: String): Result<KurrencyLocale>
KurrencyLocale.systemLocale(): KurrencyLocale

val languageTag: String        // e.g. "en-US"
val decimalSeparator: Char
val groupingSeparator: Char
```

### Compose extensions (`kurrency-compose`)

```kotlin
@Composable fun rememberCurrencyFormatter(locale: KurrencyLocale = KurrencyLocale.current()): CurrencyFormat
@Composable fun rememberSystemCurrencyFormatter(): CurrencyFormat

val LocalCurrencyFormatter: ProvidableCompositionLocal<CurrencyFormat>

@Composable fun ProvideCurrencyFormatter(locale: KurrencyLocale, content: @Composable () -> Unit)
@Composable fun ProvideSystemCurrencyFormatter(content: @Composable () -> Unit)

fun KurrencyLocale.Companion.fromComposeLocale(composeLocale: Locale): KurrencyLocale
@Composable fun KurrencyLocale.Companion.current(): KurrencyLocale
```

## Error Handling

Result-based methods return `Result<String>`, with failures modeled as `KurrencyError`:

```kotlin
CurrencyFormatter(KurrencyLocale.US)
    .formatCurrencyStyleResult("1234.56", "USD")
    .onSuccess { println(it) }
    .onFailure { error ->
        when (error) {
            is KurrencyError.InvalidAmount -> println("Invalid amount")
            is KurrencyError.InvalidCurrencyCode -> println("Invalid currency")
            else -> println("Formatting error")
        }
    }
```

| Error | Meaning |
|-------|---------|
| `KurrencyError.InvalidCurrencyCode` | Unknown or malformed currency code |
| `KurrencyError.InvalidAmount` | Amount could not be parsed |
| `KurrencyError.FormattingFailure` | Platform formatting error |
| `KurrencyError.FractionDigitsFailure` | Could not resolve fraction digits |
| `KurrencyError.InvalidLocale` | Unrecognized locale tag |

## Platform Support

| Platform | Backend |
|----------|---------|
| Android (API 24+) | ICU (`android.icu`) |
| iOS (13+) | `NSNumberFormatter` |
| JVM (17+) | `java.text.NumberFormat` |
| JS (Browser / Node.js) | `Intl.NumberFormat` |
| WasmJs (Browser) | `Intl.NumberFormat` |

## License

Apache License 2.0 — Copyright © 2025
