# Kurrency Improvements & Features — Design Specification

**Date:** 2026-03-24
**Branch:** `improvement/normalized-formatting` (base for Sprint 0)
**Current Version:** 0.2.5-alpha01
**Scope:** 8 sprints, 14 improvements, ~250+ new tests

---

## Table of Contents

1. [Overview](#overview)
2. [Design Decisions](#design-decisions)
3. [Sprint 0 — Housekeeping & Foundation](#sprint-0--housekeeping--foundation)
4. [Sprint 1 — Core Quality Baseline](#sprint-1--core-quality-baseline)
5. [Sprint 2 — RTL & Bidirectional Locale Support](#sprint-2--rtl--bidirectional-locale-support)
6. [Sprint 3 — Currency Parsing](#sprint-3--currency-parsing)
7. [Sprint 4 — Custom Format Options (Builder API)](#sprint-4--custom-format-options-builder-api)
8. [Sprint 5 — kotlinx.serialization in kurrency-core](#sprint-5--kotlinxserialization-in-kurrency-core)
9. [Sprint 6 — Currency Conversion & Range Formatting](#sprint-6--currency-conversion--range-formatting)
10. [Sprint 7 — Accessibility & Plural Currency Names](#sprint-7--accessibility--plural-currency-names)
11. [Sprint 8 — Performance Benchmarks & Golden Tests](#sprint-8--performance-benchmarks--golden-tests)
12. [Summary](#summary)

---

## Overview

This spec defines a dependency-ordered roadmap for the Kurrency library — a Kotlin Multiplatform library for type-safe currency formatting. The plan covers housekeeping, quality improvements, new features, and performance validation across 8 sprints.

**Ordering rationale:**
- Sprint 0–1 stabilize the base before adding features
- Sprint 2 (RTL) comes before Sprint 3–4 so parsing and format options account for RTL from the start
- Sprint 5 (serialization) depends on stable data classes from Sprints 2–4
- Sprint 6–7 are higher-level features built on the stabilized core
- Sprint 8 benchmarks everything after the API surface is final

---

## Design Decisions

Decisions made during brainstorming that shape the design:

| # | Decision | Choice | Rationale |
|---|----------|--------|-----------|
| 1 | Sprint structure | Dependency-ordered | Features build on each other; ensures stable foundations |
| 2 | Conversion API depth | Pluggable provider interface (no built-in providers) | Keeps library free of HTTP/networking dependencies |
| 3 | Serialization module | Built into `kurrency-core` (not separate module) | Most KMP projects already use kotlinx.serialization |
| 4 | RTL scope | Full suite (formatting + Compose + locales + tests) | Half-supporting RTL leaves gaps |
| 5 | Accessibility scope | Core `formatSpoken()` + Compose helpers | Spoken text logic is platform-useful beyond Compose |
| 6 | Custom format patterns | Builder API (`CurrencyFormatOptions`) | Type-safe, discoverable, avoids cross-platform pattern string issues |
| 7 | Range formatting | Simple utility function | Convenience feature, not a core primitive |

---

## Sprint 0 — Housekeeping & Foundation

**Goal:** Clean slate for all subsequent work.

### 0.1 Merge `improvement/normalized-formatting` → `main`

The branch contains 7 commits with substantial fixes:

- `CurrencyOffsetMapping` cursor off-by-one fix
- iOS `formatCompactStyle` suffix placement for trailing-symbol locales (EUR)
- `normalizeAmount()` integration into `CurrencyAmount.fromMajorUnits`
- `SystemFormattingProvider` visibility fix (internal → public)
- Removal of buggy `CurrencyFormatter.forLocale()` cache
- CI Xcode 16.2 pinning for Compose/Xcode 16.4 link error
- `gradle-daemon-jvm.properties` for toolchain configuration

**Action:** Create PR, merge to main, tag as `0.2.5-alpha01`.

### 0.2 Create `CLAUDE.md`

Project-level CLAUDE.md containing:

- **Build commands:** `./gradlew build`, `./gradlew :kurrency-core:allTests`, platform-specific test tasks
- **Module structure:** core, compose, deci, sample — their purposes and dependencies
- **Key conventions:**
  - Result-based error handling (never throw from public API)
  - `expect`/`actual` pattern for platform-specific implementations
  - KDoc on all public API
  - Conventional Commits for git messages
- **Test commands per platform:** Android instrumented, JVM, iOS (requires Xcode), JS/Node, WasmJs
- **Publishing workflow:** Maven Central via vanniktech plugin

### Deliverables

| Item | Type |
|------|------|
| PR merged to main | Git |
| `0.2.5-alpha01` tag | Git |
| `CLAUDE.md` | File |

---

## Sprint 1 — Core Quality Baseline

**Goal:** Establish confidence in existing code before adding new features.

### 1.1 Platform-Specific Test Expansion

Currently 92 tests live almost entirely in `commonTest`. Platform-specific edge cases can slip through undetected.

**New test files:**

| Platform | Source Set | File | Focus |
|----------|-----------|------|-------|
| Android | `androidInstrumentedTest` | `AndroidFormatterTest.kt` | ICU edge cases, API 24 vs 34 behavior, infinity/NaN |
| JVM | `jvmTest` | `JvmFormatterTest.kt` | `java.text.NumberFormat` rounding modes, thread-local behavior |
| iOS | `iosTest` | `IosFormatterTest.kt` | `NSNumberFormatter` locale quirks, compact style suffix |
| JS | `jsTest` | `JsFormatterTest.kt` | `Intl.NumberFormat` polyfill gaps, Node vs browser |
| WasmJs | `wasmJsTest` | `WasmJsFormatterTest.kt` | Wasm number precision, Intl availability |

**Test categories per platform (~10-15 tests each):**

- **Formatting consistency:** Same input across all styles produces expected output
- **Locale-specific separators:** Verify grouping/decimal chars match platform native
- **Edge cases:** Very large numbers, zero, negative zero, max fraction digits
- **`CurrencyFormatterImpl` direct testing:** Bypass common layer, test platform impl directly

### 1.2 Thread Safety Tests

Verify that `CurrencyFormatter` and `Kurrency` are safe under concurrent access.

**Location:** `commonTest/.../ConcurrencyTest.kt` using `kotlinx-coroutines-test`

**Tests (~8-10):**

- Concurrent `Kurrency.fromCode()` calls for same and different codes
- Concurrent `formatAmount()` on shared `Kurrency` instance
- Concurrent `CurrencyFormatter` creation with different locales
- Concurrent `CurrencyAmount.fromMajorUnits()` parsing
- Race condition: format while changing locale on `CurrencyState`

**Approach:** `withContext(Dispatchers.Default)` + `async` launching 100+ coroutines hitting the same formatter simultaneously, asserting no crashes or corrupted output.

### Deliverables

| Item | Count |
|------|-------|
| Platform-specific test files | 5 |
| Platform-specific tests | ~60 |
| Concurrency test file | 1 |
| Concurrency tests | ~10 |

---

## Sprint 2 — RTL & Bidirectional Locale Support

**Goal:** Full RTL support across formatting, Compose input, and predefined locales.

### 2.1 New Predefined Locales

Added to `KurrencyLocale` companion object:

| Locale Constant | BCP 47 Tag | Script Direction | Note |
|----------------|------------|-----------------|------|
| `SAUDI_ARABIA` | `ar-SA` | RTL | **Already exists** — no new constant needed |
| `ARABIC_EG` | `ar-EG` | RTL | New |
| `HEBREW` | `he-IL` | RTL | New |
| `PERSIAN` | `fa-IR` | RTL | New |
| `URDU` | `ur-PK` | RTL | New |

> **Note:** `SAUDI_ARABIA` (`ar-SA`) already exists in the `KurrencyLocale` companion on all platforms. It is reused as-is for RTL coverage — no rename or duplication.

Each new locale gets platform-specific `actual` implementations with correct `decimalSeparator` and `groupingSeparator`.

### 2.2 RTL-Aware Formatting in Core

**New properties on `KurrencyLocale`:**

```kotlin
val isRightToLeft: Boolean    // derived from language tag
val numeralSystem: NumeralSystem  // WESTERN, EASTERN_ARABIC, PERSIAN
```

```kotlin
enum class NumeralSystem { WESTERN, EASTERN_ARABIC, PERSIAN }
```

> **Important: `expect`/`actual` implementation required.** `KurrencyLocale` is an `expect class` with platform-specific `actual` declarations. Both new properties require an `actual` implementation on every platform:
>
> | Platform | `isRightToLeft` derivation | `numeralSystem` derivation |
> |----------|---------------------------|---------------------------|
> | **Android** | `TextUtils.getLayoutDirectionFromLocale(locale)` | Check locale script via `UScript` or hardcoded tag lookup |
> | **iOS** | `NSLocale.characterDirectionForLanguage(tag)` | `NSLocale.exemplarCharacterSet` inspection |
> | **JVM** | Hardcoded language-tag lookup (`ar`, `he`, `fa`, `ur`, `dv`, `ps` → `true`). Avoids `java.awt.ComponentOrientation` which is unreliable in headless environments. | Hardcoded tag-based lookup (no JVM native API) |
> | **JS/WasmJs** | `Intl.Locale(tag).textInfo.direction` (with fallback for older engines) | Hardcoded tag-based lookup |

**Changes to `CurrencyFormatterImpl` (all platforms):**

- **Bidi mark handling:** Verify platform formatters inject correct Unicode directional marks (`U+200F` RLM, `U+200E` LRM). Normalize behavior across platforms where native formatters diverge.
- **Arabic numeral handling:** Verify Western digits (`0-9`) by default; Eastern Arabic (`٠-٩`) only when locale explicitly uses them. `numeralSystem` property enables consumers to introspect.

### 2.3 RTL-Aware Compose Support

**Changes to `CurrencyVisualTransformation`:**

- **Cursor mapping for RTL:** Current `CurrencyOffsetMapping` assumes LTR text. For RTL locales, symbol and separator positions are mirrored. Add RTL-aware offset calculation accounting for leading/trailing bidi marks.
- **`layoutDirection` awareness:** Read `LocalLayoutDirection` inside `rememberCurrencyVisualTransformation` and pass to transformation so cursor behavior matches text direction.

### 2.4 Dedicated RTL Test Suite

**Location:** `commonTest/.../RtlFormattingTest.kt` (~20-25 tests)

- Formatting output for each RTL locale × all styles (Standard, ISO, Accounting)
- Bidi mark presence/absence verification
- Cursor position correctness in `CurrencyVisualTransformation` for RTL
- Round-trip preparation: RTL formatted strings parseable (verified in Sprint 3)
- Mixed-direction text: RTL currency symbol with LTR digits
- `isRightToLeft` correctness for all predefined locales (LTR and RTL)
- `numeralSystem` correctness

### Deliverables

| Item | Count |
|------|-------|
| New predefined locales | 4 (reuse existing `SAUDI_ARABIA`) |
| New `KurrencyLocale` properties | 2 (`isRightToLeft`, `numeralSystem`) |
| New enum | 1 (`NumeralSystem`) |
| RTL test file | 1 |
| RTL tests | ~25 |

---

## Sprint 3 — Currency Parsing

**Goal:** Parse formatted currency strings back into numeric values — the inverse of formatting.

### 3.1 Robust Parsing Pipeline

`CurrencyFormatter` already declares `parseCurrencyAmount` on the `CurrencyFormat` interface. This sprint makes the implementation comprehensive.

**Parsing pipeline:**

```
"$1,234.56"    → strip symbol → strip grouping → normalize decimal → "1234.56"  → 1234.56
"(€1.234,56)"  → detect accounting → strip parens → negate → strip symbol       → -1234.56
"¥1,235"       → strip symbol → strip grouping → "1235"                          → 1235.0
"USD 1,234.56" → strip ISO code → strip grouping → normalize decimal             → 1234.56
"$1.2K"        → strip symbol → detect compact suffix → multiply                 → 1200.0
```

**Parsing rules (locale-aware):**

1. Detect and strip currency symbol or ISO code (position varies by locale)
2. Detect accounting notation — `(...)` means negative
3. Strip grouping separators (locale-specific: `,` or `.` or ` ` or `\u00A0`)
4. Normalize decimal separator to `.`
5. Detect compact suffixes — **English only for v1** (`K`, `M`, `B`, `T`). Compact round-trip for non-English locales is explicitly out of scope because iOS hardcodes English suffixes and locale-specific suffix tables (e.g., German `Mio.`, `Mrd.`) are not standardized. Non-English compact strings return `KurrencyError.InvalidAmount` when parsed.
6. Strip RTL bidi marks (`LRM`/`RLM`) before processing
7. Validate remaining string is a valid number
8. Return `KurrencyError.InvalidAmount` on failure

### 3.2 New/Enhanced API

**On `CurrencyFormatter`:**

| Function | Returns | Description |
|----------|---------|-------------|
| `parseCurrencyAmount(formattedText, currencyCode)` | `Double?` | Existing — made robust |
| `parseCurrencyAmountResult(formattedText, currencyCode)` | `Result<Double>` | Existing — made robust |
| `parseToMinorUnits(formattedText, currencyCode)` | `Result<Long>` | New — returns minor units. **Precision note:** Uses `String`-based decimal arithmetic internally (not `Double` multiplication) to avoid floating-point precision loss for 3-decimal currencies like KWD/BHD. The parsed decimal string is split at the decimal point and digits are counted/shifted to produce the `Long` minor units value directly. |
| `parseToCurrencyAmount(formattedText, currency)` | `Result<CurrencyAmount>` | New — returns `CurrencyAmount` |

**On `CurrencyAmount`:**

```kotlin
companion object {
    fun parse(
        formattedText: String,
        currency: Kurrency,
        locale: KurrencyLocale
    ): Result<CurrencyAmount>
}
```

### 3.3 Round-Trip Guarantee

Core contract: `parse(format(amount)) == amount` for Standard, ISO, and Accounting styles across all locales.

> **Compact style caveat:** Round-trip is guaranteed for English locales only. Compact formatting is lossy by nature (`1,234` → `$1.2K` → `1200`), so the round-trip guarantee for compact is: `parse(formatCompact(amount))` produces the compact-rounded value, not the original.

### 3.4 Tests

**Location:** `commonTest/.../CurrencyParsingTest.kt` (~30-35 tests)

- Round-trip for every `CurrencyStyle` × 5+ locales (US, Germany, Japan, Brazil, Saudi Arabia)
- Accounting negatives: `($1,234.56)` → `-1234.56`
- Compact parsing (English only): `$1.2K` → `1200.0`, `$1.5M` → `1500000.0`
- Compact parsing (non-English rejected): `€1,5Mio` → `KurrencyError.InvalidAmount`
- ISO format: `USD 1,234.56` → `1234.56`
- Edge cases: `$0.00`, `¥0`, `$-0.00`, whitespace-only, empty string
- RTL formatted strings from Sprint 2 locales
- Invalid input returns `KurrencyError.InvalidAmount`
- Minor units round-trip: format minor → parse to minor → same value

### Deliverables

| Item | Count |
|------|-------|
| Enhanced parsing functions | 2 |
| New parsing functions | 2 |
| `CurrencyAmount.parse()` convenience | 1 |
| Parsing test file | 1 |
| Parsing tests | ~35 |

---

## Sprint 4 — Custom Format Options (Builder API)

**Goal:** Type-safe builder for fine-grained formatting control beyond predefined `CurrencyStyle`.

### 4.1 `CurrencyFormatOptions` Data Class

```kotlin
data class CurrencyFormatOptions(
    val symbolPosition: SymbolPosition = SymbolPosition.LOCALE_DEFAULT,
    val grouping: Boolean = true,
    val minFractionDigits: Int? = null,   // null = use currency default
    val maxFractionDigits: Int? = null,
    val negativeStyle: NegativeStyle = NegativeStyle.MINUS_SIGN,
    val symbolDisplay: SymbolDisplay = SymbolDisplay.SYMBOL,
    val zeroDisplay: ZeroDisplay = ZeroDisplay.SHOW,
)
```

### 4.2 Supporting Enums

| Enum | Values | Purpose |
|------|--------|---------|
| `SymbolPosition` | `LEADING`, `TRAILING`, `LOCALE_DEFAULT` | Override symbol placement |
| `NegativeStyle` | `MINUS_SIGN`, `PARENTHESES`, `LOCALE_DEFAULT` | How negatives render |
| `SymbolDisplay` | `SYMBOL` (`$`), `ISO_CODE` (`USD`), `NAME` (`US Dollar`), `NONE` | What identifies the currency |
| `ZeroDisplay` | `SHOW`, `DASH` (`—`), `EMPTY` | How zero amounts render |

### 4.3 Builder + DSL

**Builder pattern:**

```kotlin
CurrencyFormatOptions.builder()
    .grouping(false)
    .symbolPosition(SymbolPosition.TRAILING)
    .negativeStyle(NegativeStyle.PARENTHESES)
    .maxFractionDigits(4)
    .build()
```

**Kotlin DSL:**

```kotlin
CurrencyFormatOptions {
    grouping = false
    symbolPosition = SymbolPosition.TRAILING
}
```

### 4.4 Integration Points

**On `Kurrency`:** Uses a distinct method name `formatAmountWithOptions` to avoid overload confusion with the existing `formatAmount(amount, style, locale)`:

```kotlin
fun formatAmountWithOptions(amount: String, options: CurrencyFormatOptions, locale: KurrencyLocale): Result<String>
fun formatAmountWithOptions(amount: Double, options: CurrencyFormatOptions, locale: KurrencyLocale): Result<String>
```

**On `CurrencyFormatter`:**

```kotlin
fun formatWithOptions(amount: String, currencyCode: String, options: CurrencyFormatOptions): Result<String>
```

**On `CurrencyAmount`:**

```kotlin
fun format(options: CurrencyFormatOptions, locale: KurrencyLocale): Result<String>
```

Existing `CurrencyStyle` methods remain unchanged. Internally, each `CurrencyStyle` maps to a predefined `CurrencyFormatOptions`:

| `CurrencyStyle` | Equivalent `CurrencyFormatOptions` |
|-----------------|-----------------------------------|
| `Standard` | All defaults |
| `Iso` | `symbolDisplay = SymbolDisplay.ISO_CODE` |
| `Accounting` | `negativeStyle = NegativeStyle.PARENTHESES` |

> **Note on Accounting style:** The existing `Kurrency.formatAmount` for `CurrencyStyle.Accounting` uses post-processing to wrap negative amounts in parentheses. `NegativeStyle.PARENTHESES` in `CurrencyFormatOptions` delegates to this same post-processing logic — it is not a separate code path.

### 4.5 Platform Implementation

Each `CurrencyFormatterImpl` applies options by configuring the native formatter:

- **Android/JVM:** Map to `DecimalFormat` properties (`setGroupingUsed`, `setMinimumFractionDigits`, etc.)
- **iOS:** Map to `NSNumberFormatter` properties (`groupingSeparator`, `minimumFractionDigits`, etc.)
- **JS/WasmJs:** Map to `Intl.NumberFormat` options object (`useGrouping`, `minimumFractionDigits`, etc.)

Options that can't be expressed natively (like `ZeroDisplay.DASH`) are applied as post-processing on the formatted string.

> **`ZeroDisplay` + `CurrencyVisualTransformation` interaction:** When `ZeroDisplay.DASH` or `ZeroDisplay.EMPTY` replaces the formatted output, the digit position list becomes empty and `CurrencyOffsetMapping` cannot map cursor positions. `CurrencyVisualTransformation` must detect these sentinel values and use a `ZeroOffsetMapping` that maps all source offsets to `0` (for `EMPTY`) or `formattedLength` (for `DASH`). This is handled as a special case in the `filter()` method, not in the formatter.

### 4.6 Tests

**Location:** `commonTest/.../CurrencyFormatOptionsTest.kt` (~25 tests)

- Each option in isolation × 3+ locales
- Combined options (no grouping + trailing symbol + parentheses)
- `ZeroDisplay` variants
- `SymbolDisplay.NAME` with plural names from `CurrencyMetadata`
- Default options produce identical output to `CurrencyStyle.Standard`
- Invalid combinations (`minFractionDigits > maxFractionDigits`) return error
- Round-trip with parsing: format with options → parse → same value

### Deliverables

| Item | Count |
|------|-------|
| `CurrencyFormatOptions` data class | 1 |
| Supporting enums | 4 |
| Builder + DSL | 1 |
| New `formatAmountWithOptions`/`formatWithOptions` overloads | 4 |
| Format options test file | 1 |
| Format options tests | ~25 |

---

## Sprint 5 — kotlinx.serialization in `kurrency-core`

**Goal:** Add serialization support directly into `kurrency-core` for core types.

### 5.1 Dependency Addition

Add to `libs.versions.toml`:

```toml
kotlinx-serialization = "1.7.x"  # latest stable at implementation time
```

Apply `kotlin("plugin.serialization")` to `kurrency-core/build.gradle.kts`.

### 5.2 Serializers

**`Kurrency` → string code:**

```json
"USD"
```

Custom `KSerializer<Kurrency>` using `Kurrency.fromCode()`. Throws `SerializationException` wrapping `KurrencyError.InvalidCurrencyCode` on invalid input.

**`CurrencyAmount` → structured object:**

```json
{ "minorUnits": 123456, "currency": "USD" }
```

Minor units as canonical wire format — avoids floating-point ambiguity.

**`KurrencyLocale` → language tag string:**

```json
"en-US"
```

Custom `KSerializer<KurrencyLocale>` using `KurrencyLocale.fromLanguageTag()`. On invalid tags, throws `SerializationException` wrapping `KurrencyError.InvalidLocale` (new error type — see below).

> **New error type required:** `KurrencyLocale.fromLanguageTag()` currently returns `Result.failure(IllegalArgumentException(...))`, not a `KurrencyError`. As part of this sprint, add:
>
> ```kotlin
> class InvalidLocale(val languageTag: String) : KurrencyError("Invalid locale: $languageTag")
> ```
>
> Uses `class` (not `data class`) to be consistent with all existing `KurrencyError` subclasses and avoid `data class`-on-`Exception` pitfalls.
>
> Update `fromLanguageTag()` on all platforms to return `KurrencyError.InvalidLocale` instead of `IllegalArgumentException`. This is a behavioral change but not a binary-breaking one since the error is inside a `Result`.

**`CurrencyFormatOptions` → auto-generated:**

**Example with all defaults overridden (full form):**

```json
{
    "symbolPosition": "TRAILING",
    "grouping": false,
    "minFractionDigits": 2,
    "maxFractionDigits": 4,
    "negativeStyle": "PARENTHESES",
    "symbolDisplay": "ISO_CODE",
    "zeroDisplay": "DASH"
}
```

**Example with only overrides (default-omitted form — this is what `KurrencyJson` actually produces):**

```json
{
    "symbolPosition": "TRAILING",
    "grouping": false,
    "minFractionDigits": 2,
    "maxFractionDigits": 4,
    "negativeStyle": "PARENTHESES",
    "symbolDisplay": "ISO_CODE",
    "zeroDisplay": "DASH"
}
```

> When all fields hold their default values, the serialized output is `{}`. Fields matching their Kotlin default are omitted.

All supporting enums annotated `@Serializable`. Nullable fields (`minFractionDigits`, `maxFractionDigits`) with `null` defaults are omitted, and non-null fields matching their declared default are also omitted. This requires configuring the `Json` instance:

```kotlin
val KurrencyJson = Json {
    explicitNulls = false       // omit null fields
    encodeDefaults = false      // omit fields matching their default value
    ignoreUnknownKeys = true    // forward compatibility
}
```

This `KurrencyJson` instance is provided as a public constant for consumers who need serialization. The library does not force a global `Json` configuration.

### 5.3 Implementation Strategy

| Type | Strategy | Reason |
|------|----------|--------|
| `Kurrency` | `@Serializable(with = KurrencySerializer::class)` | Private constructor, needs factory validation |
| `CurrencyAmount` | `@Serializable(with = CurrencyAmountSerializer::class)` | Custom to enforce minor units format |
| `KurrencyLocale` | `@Serializable(with = KurrencyLocaleSerializer::class)` | `expect`/`actual` class, can't annotate directly |
| `CurrencyFormatOptions` | `@Serializable` directly | Plain data class |
| Enums | `@Serializable` directly | Simple enums |

Serializers live in `org.kimplify.kurrency.serialization` package within `kurrency-core`.

### 5.4 Tests

**Location:** `commonTest/.../serialization/` (~20 tests)

- `KurrencySerializerTest`: round-trip encode/decode, invalid code throws
- `CurrencyAmountSerializerTest`: round-trip, verify minor units (not float) in JSON
- `KurrencyLocaleSerializerTest`: round-trip for predefined + custom locales
- `CurrencyFormatOptionsSerializerTest`: round-trip, defaults omitted, full options
- Cross-format: verify with `Cbor`, `ProtoBuf` (not just JSON)
- Backward compatibility: JSON missing optional fields → defaults applied

### Deliverables

| Item | Count |
|------|-------|
| Custom serializers | 3 (`Kurrency`, `CurrencyAmount`, `KurrencyLocale`) |
| New error type | 1 (`KurrencyError.InvalidLocale`) |
| `KurrencyJson` configuration | 1 |
| `@Serializable` annotations | `CurrencyFormatOptions` + 4 enums |
| Serialization test files | 4 |
| Serialization tests | ~20 |

---

## Sprint 6 — Currency Conversion & Range Formatting

**Goal:** Pluggable conversion with rate provider interface + simple range formatting.

### 6.1 `CurrencyConverter` Object

**Pure function (user supplies rate):**

```kotlin
object CurrencyConverter {
    fun convert(
        amount: Double,
        from: Kurrency,
        to: Kurrency,
        rate: Double
    ): Result<CurrencyAmount>
}
```

Validates inputs (rate > 0, amount is finite), multiplies, and returns `CurrencyAmount` in target currency.

**Rounding specification:** The input `amount: Double` is treated as major units. The conversion result (`amount * rate`) is rounded to the target currency's fraction digits using `HALF_EVEN` (banker's rounding) before converting to minor units (`Long`). For example:
- `convert(100.0, USD, JPY, 150.456)` → `15046` minor units (JPY has 0 fraction digits, so `15045.6` rounds to `15046`, and minor = major for JPY)
- `convert(100.0, USD, KWD, 0.30712)` → `30712` minor units (KWD has 3 fraction digits, so `30.712` × 1000)
- `convert(100.0, USD, KWD, 0.307125)` → `30712` minor units (HALF_EVEN rounds `30.7125` to `30.712` because the preceding digit `2` is even; HALF_UP would incorrectly give `30713`)

String-based decimal arithmetic is used internally to avoid `Double` precision loss during the minor units conversion step.

### 6.2 `RateProvider` Interface

```kotlin
fun interface RateProvider {
    suspend fun getRate(from: Kurrency, to: Kurrency): Result<Double>
}
```

`fun interface` allows lambda usage:

```kotlin
RateProvider { from, to -> myApi.fetchRate(from.code, to.code) }
```

### 6.3 High-Level Suspend Functions

```kotlin
object CurrencyConverter {
    // Pure — user supplies rate
    fun convert(amount: Double, from: Kurrency, to: Kurrency, rate: Double): Result<CurrencyAmount>

    // Provider-based
    suspend fun convert(amount: Double, from: Kurrency, to: Kurrency, provider: RateProvider): Result<CurrencyAmount>

    // CurrencyAmount convenience
    suspend fun convert(amount: CurrencyAmount, to: Kurrency, provider: RateProvider): Result<CurrencyAmount>
}
```

### 6.4 New Error Type

```kotlin
sealed class KurrencyError : Exception() {
    // ... existing ...
    data class ConversionFailure(
        val from: String,
        val to: String,
        override val cause: Throwable?
    ) : KurrencyError()
}
```

### 6.5 Range Formatting

**On `CurrencyFormatter`:**

```kotlin
fun formatRange(
    min: String,
    max: String,
    currencyCode: String,
    locale: KurrencyLocale = this.locale
): Result<String>
```

**Behavior:**
- Shared symbol: `$10 – 50` (symbol on first value only)
- Locale-aware en dash separator with proper spacing
- Both values formatted with Standard style
- `min > max` returns error

**On `Kurrency`:**

```kotlin
fun formatRange(min: String, max: String, locale: KurrencyLocale): Result<String>
```

### 6.6 Tests

**`commonTest/.../CurrencyConverterTest.kt` (~20 tests):**

- Pure conversion: USD→EUR with known rate, verify minor units
- Round-trip: USD→EUR→USD with inverse rates, within rounding tolerance
- Zero amount, negative amount
- Invalid rate (0, negative, NaN, Infinity) returns error
- Different fraction digits: USD(2)→JPY(0)→BHD(3)
- `RateProvider` mock, verify suspend flow
- Provider failure propagates as `ConversionFailure`

**`commonTest/.../RangeFormattingTest.kt` (~12 tests):**

- Basic: `$10 – 50` for US locale
- European: `10 – 50 €` with trailing symbol
- Zero-fraction: `¥1,000 – 5,000`
- min == max: valid, not collapsed
- min > max: returns error
- RTL locale range

### Deliverables

| Item | Count |
|------|-------|
| `CurrencyConverter` object | 1 |
| `RateProvider` interface | 1 |
| `ConversionFailure` error type | 1 |
| `formatRange` functions | 2 |
| Converter test file | 1 |
| Converter tests | ~20 |
| Range test file | 1 |
| Range tests | ~12 |

---

## Sprint 7 — Accessibility & Plural Currency Names

**Goal:** Spoken-text formatting in core, plural-aware names, and Compose accessibility helpers.

### 7.1 `formatSpoken()` in Core

**On `CurrencyFormatter`:**

```kotlin
fun formatSpoken(
    amount: String,
    currencyCode: String,
    locale: KurrencyLocale = this.locale
): Result<String>
```

**Output examples:**

| Input | Locale | Output |
|-------|--------|--------|
| `"1234.56"`, `"USD"` | en-US | `"one thousand two hundred thirty-four US dollars and fifty-six cents"` |
| `"1.00"`, `"EUR"` | en-US | `"one euro and zero cents"` |
| `"0.50"`, `"GBP"` | en-US | `"zero British pounds and fifty pence"` |
| `"1000"`, `"JPY"` | en-US | `"one thousand Japanese yen"` |
| `"-42.10"`, `"USD"` | en-US | `"negative forty-two US dollars and ten cents"` |

### 7.2 Number-to-Words Engine

**Approach: Custom shared implementation in `commonMain`, NOT platform-native APIs.**

Using platform-native spell-out APIs (`NSNumberFormatter.spellOut`, ICU `RuleBasedNumberFormat`) would produce divergent output across platforms (e.g., iOS says "minus" vs ICU says "negative"), making `commonTest` assertions impossible. Instead:

- **Single `NumberToWords` object in `commonMain`** with a pure Kotlin implementation for English.
- Covers: 0–999,999,999,999 (trillions), negatives, decimals.
- No `expect`/`actual` needed — fully shared code.
- Future locale support can be added by registering language-specific word tables.

> **JVM dependency note:** No `icu4j` dependency needed since we use our own implementation. This avoids adding a ~13MB transitive dependency to `kurrency-core`.

**Trade-off:** We lose automatic multi-language support from native APIs. This is acceptable because `formatSpoken()` is primarily for accessibility (screen readers), and English covers the vast majority of accessibility use cases. Non-English spoken format can be added incrementally via language word tables without platform-specific code.

### 7.3 Sub-Unit Names in `CurrencyMetadata`

New fields added to the enum:

```kotlin
enum class CurrencyMetadata(
    // ... existing fields ...
    val subUnitName: String,       // "cent"
    val subUnitNamePlural: String, // "cents"
)
```

Covers all 47+ existing currencies (e.g., cent/cents, pence/pence, sen/sen, fils/fils).

### 7.4 Plural-Aware Name Formatting

**On `CurrencyFormatter`:**

```kotlin
fun formatWithName(
    amount: String,
    currencyCode: String,
    locale: KurrencyLocale = this.locale
): Result<String>
```

Output: `"1,234.56 US Dollars"` or `"1.00 US Dollar"` — singular/plural based on amount. Connects to `SymbolDisplay.NAME` from Sprint 4.

### 7.5 Compose Accessibility Helpers

**Location:** `kurrency-compose` module

**Semantics modifier:**

```kotlin
@Composable
fun Modifier.currencySemantics(
    amount: String,
    currencyCode: String,
    locale: KurrencyLocale = KurrencyLocale.current()
): Modifier
```

Applies `semantics { contentDescription = formatSpoken(...) }`.

**Composable component:**

```kotlin
@Composable
fun CurrencyText(
    amount: String,
    currencyCode: String,
    modifier: Modifier = Modifier,
    locale: KurrencyLocale = KurrencyLocale.current(),
    style: TextStyle = LocalTextStyle.current
)
```

Renders formatted amount visually + sets spoken description automatically.

### 7.6 Tests

**`commonTest/.../SpokenFormattingTest.kt` (~25 tests):**

- All major currencies with singular and plural amounts
- Zero, negative amounts
- Sub-unit only (`$0.50`)
- Zero-fraction currencies (JPY — no sub-unit portion)
- Three-fraction currencies (BHD — fils)
- Large numbers (millions, billions)
- `formatWithName` singular vs plural

**`commonTest/.../accessibility/ComposeAccessibilityTest.kt` (~10 tests):**

- `currencySemantics` applies correct content description
- `CurrencyText` renders visual format + sets spoken description
- Locale changes update spoken text

### Deliverables

| Item | Count |
|------|-------|
| `formatSpoken()` | 1 |
| `formatWithName()` | 1 |
| `NumberToWords` engine (commonMain, pure Kotlin) | 1 |
| `CurrencyMetadata` sub-unit fields | 2 |
| `Modifier.currencySemantics()` | 1 |
| `CurrencyText` composable | 1 |
| Spoken formatting tests | ~25 |
| Compose accessibility tests | ~10 |

---

## Sprint 8 — Performance Benchmarks & Golden Tests

**Goal:** Quantify performance and lock down formatting output to prevent regressions.

### 8.1 Performance Benchmarks (kotlinx-benchmark)

**Benchmark suites:**

| Benchmark | What It Measures |
|-----------|-----------------|
| `KurrencyCreationBenchmark` | `Kurrency.fromCode()` cold + warm |
| `FormatterCreationBenchmark` | `CurrencyFormatter(locale)` construction |
| `FormatAmountBenchmark` | `formatAmount()` × all styles × 5 locales |
| `ParseAmountBenchmark` | `parseCurrencyAmount()` × all styles × 5 locales |
| `NormalizeAmountBenchmark` | `normalizeAmount()` with various inputs |
| `FormatOptionsBenchmark` | `formatWithOptions()` vs standard — overhead |
| `ConversionBenchmark` | `CurrencyConverter.convert()` throughput |
| `SpokenFormatBenchmark` | `formatSpoken()` — number-to-words cost |
| `SerializationBenchmark` | JSON encode/decode for core types |

**Targets:** Android, JVM, JS. iOS and WasmJs if kotlinx-benchmark supports them.

**Output:** Results as JSON in `benchmarks/` directory. Not in CI (noisy). Runnable via `./gradlew :kurrency-core:benchmark`.

### 8.2 Golden Tests for `CurrencyVisualTransformation`

Capture exact `TransformedText` output (formatted string + offset mapping) for known inputs.

**Golden test matrix (~30 entries):**

| Input | Currency | Locale | Asserted Output | Cursor Mapping |
|-------|----------|--------|-----------------|----------------|
| `"1234"` | USD | en-US | `$1,234` | 0→0, 1→2, 2→3, 3→5, 4→6 |
| `"1234.56"` | USD | en-US | `$1,234.56` | full |
| `"1234.56"` | EUR | de-DE | `1.234,56 €` | full |
| `"1000"` | JPY | ja-JP | `¥1,000` | full |
| `"1234.567"` | BHD | ar-SA | RTL output | full |
| `""` | USD | en-US | edge case | edge case |
| `"0"` | USD | en-US | edge case | edge case |

Covers: each `CurrencyStyle` × 5 representative locales, RTL locales, edge cases.

**Location:** `kurrency-compose/src/commonTest/.../golden/VisualTransformationGoldenTest.kt`

> **Module note:** `CurrencyVisualTransformation` lives in `kurrency-compose`, not `kurrency-core`. Golden tests must be in `kurrency-compose`'s test source set. The `updateGoldens` Gradle task is registered in `kurrency-compose/build.gradle.kts`.

**Update workflow:** When a golden test fails, developer reviews diff and runs `./gradlew :kurrency-compose:updateGoldens` to accept new output.

### 8.3 Meta-Tests (~5)

- All golden entries are complete (no missing locale/style combos)
- Golden file is parseable
- Offset mappings are monotonically increasing

### Deliverables

| Item | Count |
|------|-------|
| Benchmark suites | 9 |
| Golden test entries | ~30 |
| Golden test infrastructure | 1 (custom Gradle task) |
| Meta-tests | ~5 |

---

## Summary

| Sprint | Theme | Key Deliverables | Est. Tests |
|--------|-------|-----------------|------------|
| **0** | Housekeeping | PR merge, CLAUDE.md | 0 |
| **1** | Quality Baseline | Platform tests, concurrency tests | ~70 |
| **2** | RTL Support | 5 locales, bidi formatting, Compose RTL | ~25 |
| **3** | Parsing | Robust reverse formatting, round-trip guarantee | ~35 |
| **4** | Format Options | Builder API, 6 knobs, 4 enums | ~25 |
| **5** | Serialization | 4 serializers in core | ~20 |
| **6** | Conversion & Range | Converter + RateProvider + range util | ~32 |
| **7** | Accessibility | formatSpoken, currency names, Compose helpers | ~35 |
| **8** | Benchmarks & Golden | 9 benchmarks, 30 golden entries | ~35 |
| | | **Total** | **~277** |

### Version Plan

| Sprint | Version |
|--------|---------|
| 0 | `0.2.5-alpha01` (tag existing work) |
| 1–2 | `0.3.0-alpha01` (quality + RTL) |
| 3–4 | `0.3.0-beta01` (parsing + options) |
| 5 | `0.3.0-rc01` (serialization) |
| 6–7 | `0.4.0-alpha01` (conversion + accessibility) |
| 8 | `0.4.0` (stable with benchmarks) |
