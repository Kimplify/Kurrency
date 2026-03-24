# Kurrency Improvements Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver 8 sprints of improvements to the Kurrency KMP library: housekeeping, quality baseline, RTL support, currency parsing, custom format options, serialization, conversion/range formatting, accessibility, and benchmarks.

**Architecture:** Kurrency is a Kotlin Multiplatform library with `expect`/`actual` pattern. Core formatting lives in `kurrency-core`, Compose integration in `kurrency-compose`, Deci integration in `kurrency-deci`. All public APIs use `Result<T>` for error handling. Platform targets: Android, iOS, JVM, JS, WasmJs.

**Tech Stack:** Kotlin 2.3.20, Compose Multiplatform 1.11.0-alpha04, Gradle 8.x, kotlinx-serialization, kotlinx-coroutines, kotlinx-benchmark

**Spec:** `docs/superpowers/specs/2026-03-24-kurrency-improvements-design.md`

---

## File Structure Overview

### New Files by Sprint

**Sprint 0:**
- Create: `CLAUDE.md`

**Sprint 1:**
- Create: `kurrency-core/src/jvmTest/kotlin/org/kimplify/kurrency/JvmFormatterTest.kt`
- Create: `kurrency-core/src/jsTest/kotlin/org/kimplify/kurrency/JsFormatterTest.kt`
- Create: `kurrency-core/src/wasmJsTest/kotlin/org/kimplify/kurrency/WasmJsFormatterTest.kt`
- Create: `kurrency-core/src/iosTest/kotlin/org/kimplify/kurrency/IosFormatterTest.kt`
- Modify: `kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/CurrencyFormatterThreadSafetyTest.kt`
- Modify: existing `androidTest` files (add platform-specific edge case tests)
- Note: Verify that `jsTest`, `wasmJsTest`, and `iosTest` source sets are declared in `kurrency-core/build.gradle.kts` before creating files. If missing, add them in the `kotlin { sourceSets { } }` block.

**Sprint 2:**
- Create: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/NumeralSystem.kt`
- Create: `kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/RtlFormattingTest.kt`
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/KurrencyLocale.kt` (add `isRightToLeft`, `numeralSystem`)
- Modify: all platform `actual` `KurrencyLocale.kt` files (4 platforms)
- Modify: `kurrency-compose/src/commonMain/kotlin/org/kimplify/kurrency/compose/CurrencyVisualTransformation.kt`

**Sprint 3:**
- Create: `kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/CurrencyParsingTest.kt`
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyFormatter.kt` (robust parsing)
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyFormat.kt` (new parse methods)
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyAmount.kt` (add `parse()`)
- Modify: all platform `CurrencyFormatterImpl.kt` files

**Sprint 4:**
- Create: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyFormatOptions.kt`
- Create: `kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/CurrencyFormatOptionsTest.kt`
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/Kurrency.kt` (add `formatAmountWithOptions`)
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyFormatter.kt` (add `formatWithOptions`)
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyAmount.kt` (add options overload)
- Modify: all platform `CurrencyFormatterImpl.kt` files
- Modify: `kurrency-compose/src/commonMain/kotlin/org/kimplify/kurrency/compose/CurrencyVisualTransformation.kt` (ZeroDisplay handling)

**Sprint 4 prerequisite:**
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/extensions/StringExtensions.kt` (add locale-aware `normalizeAmount(locale)` overload — needed by parsing, formatting options, range, and spoken formatting)

**Sprint 5:**
- Create: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/serialization/KurrencySerializer.kt`
- Create: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/serialization/CurrencyAmountSerializer.kt`
- Create: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/serialization/KurrencyLocaleSerializer.kt`
- Create: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/serialization/KurrencyJson.kt`
- Create: `kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/serialization/KurrencySerializerTest.kt`
- Create: `kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/serialization/CurrencyAmountSerializerTest.kt`
- Create: `kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/serialization/KurrencyLocaleSerializerTest.kt`
- Create: `kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/serialization/CurrencyFormatOptionsSerializerTest.kt`
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/KurrencyError.kt` (add `InvalidLocale`)
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyFormatOptions.kt` (add `@Serializable`)
- Modify: `kurrency-core/build.gradle.kts` (add serialization plugin + dependency)
- Modify: `gradle/libs.versions.toml` (add kotlinx-serialization version)
- Modify: all platform `KurrencyLocale.kt` files (change error type in `fromLanguageTag`)

**Sprint 6:**
- Create: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyConverter.kt`
- Create: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/RateProvider.kt`
- Create: `kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/CurrencyConverterTest.kt`
- Create: `kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/RangeFormattingTest.kt`
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/KurrencyError.kt` (add `ConversionFailure`)
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyFormatter.kt` (add `formatRange`)
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/Kurrency.kt` (add `formatRange`)

**Sprint 7:**
- Create: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/NumberToWords.kt`
- Create: `kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/SpokenFormattingTest.kt`
- Create: `kurrency-compose/src/commonMain/kotlin/org/kimplify/kurrency/compose/CurrencyAccessibility.kt`
- Create: `kurrency-compose/src/commonMain/kotlin/org/kimplify/kurrency/compose/CurrencyText.kt`
- Create: `kurrency-compose/src/commonTest/kotlin/org/kimplify/kurrency/compose/ComposeAccessibilityTest.kt`
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyMetadata.kt` (add sub-unit names)
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyFormatter.kt` (add `formatSpoken`, `formatWithName`)

**Sprint 8:**
- Create: `kurrency-compose/src/commonTest/kotlin/org/kimplify/kurrency/compose/golden/VisualTransformationGoldenTest.kt`
- Create: `kurrency-compose/src/commonTest/resources/golden/visual-transformation-golden.json`
- Modify: `kurrency-core/build.gradle.kts` (add benchmark configuration)
- Create benchmark files in a benchmark source set (details in Sprint 8 tasks)

---

## Sprint 0 — Housekeeping & Foundation

### Task 0.1: Merge Branch to Main

**Files:**
- No file changes — git operations only

- [ ] **Step 1: Verify all tests pass on current branch**

Run: `./gradlew :kurrency-core:allTests :kurrency-compose:allTests :kurrency-deci:allTests`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Create PR for `improvement/normalized-formatting` → `main`**

```bash
git push -u origin improvement/normalized-formatting
gh pr create --title "feat: Normalized formatting improvements" --body "$(cat <<'EOF'
## Summary
- Fix CurrencyOffsetMapping cursor off-by-one
- Fix iOS formatCompactStyle suffix placement for trailing-symbol locales
- Integrate normalizeAmount() into CurrencyAmount.fromMajorUnits
- Fix SystemFormattingProvider visibility (internal → public)
- Remove buggy CurrencyFormatter.forLocale() cache
- Pin CI to Xcode 16.2
- Add gradle-daemon-jvm.properties

## Test plan
- [ ] All common tests pass
- [ ] Android instrumented tests pass
- [ ] iOS tests pass (Xcode 16.2)
EOF
)"
```

- [ ] **Step 3: Merge PR**

```bash
gh pr merge --squash
git checkout main && git pull
```

- [ ] **Step 4: Tag release**

```bash
git tag v0.2.5-alpha01
git push origin v0.2.5-alpha01
```

### Task 0.2: Create CLAUDE.md

**Files:**
- Create: `CLAUDE.md`

- [ ] **Step 1: Create CLAUDE.md**

```markdown
# Kurrency

Kotlin Multiplatform library for type-safe currency formatting with locale management and Compose support.

## Build Commands

```bash
# Full build
./gradlew build

# Core tests (common)
./gradlew :kurrency-core:allTests

# Android instrumented tests (requires emulator/device)
./gradlew :kurrency-core:connectedAndroidTest

# JVM tests only
./gradlew :kurrency-core:jvmTest

# iOS tests (requires macOS with Xcode 16.2)
./gradlew :kurrency-core:iosSimulatorArm64Test

# JS tests
./gradlew :kurrency-core:jsTest

# WasmJs tests
./gradlew :kurrency-core:wasmJsNodeTest

# Compose module tests
./gradlew :kurrency-compose:allTests

# Deci module tests
./gradlew :kurrency-deci:allTests

# Publish to Maven Central
./gradlew publishAllPublicationsToMavenCentralRepository -x test
```

## Module Structure

- **kurrency-core** — Foundation: `CurrencyFormatter`, `Kurrency`, `CurrencyAmount`, `KurrencyLocale`, `KurrencyError`. Platform-specific `expect`/`actual` for Android (ICU), iOS (NSNumberFormatter), JVM (NumberFormat), JS/WasmJs (Intl.NumberFormat).
- **kurrency-compose** — Compose Multiplatform integration: `rememberCurrencyFormatter()`, `LocalCurrencyFormatter`, `CurrencyVisualTransformation`.
- **kurrency-deci** — Extension functions for formatting Deci decimal numbers.
- **sample** — Multiplatform sample app (Android, iOS, Desktop, Web).

## Conventions

- **Error handling:** All public APIs return `Result<T>`. Never throw from public API.
- **Platform code:** Use `expect`/`actual` pattern. Common interface in `commonMain`, platform implementations in `androidMain`, `iosMain`, `jvmMain`, `webMain` (shared JS/WasmJs), `jsMain`, `wasmJsMain`.
- **Documentation:** KDoc on all public API surfaces.
- **Commits:** Conventional Commits (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`).
- **Testing:** Target 80%+ coverage. Tests live in `commonTest` for shared logic, platform-specific tests in respective test source sets.
- **Formatting:** Kotlin coding conventions, 120 char line limit.
```

- [ ] **Step 2: Commit**

```bash
git add CLAUDE.md
git commit -m "docs: Add CLAUDE.md with build commands and project conventions"
```

---

## Sprint 1 — Core Quality Baseline

### Task 1.1: JVM Platform-Specific Tests

**Files:**
- Create: `kurrency-core/src/jvmTest/kotlin/org/kimplify/kurrency/JvmFormatterTest.kt`

- [ ] **Step 1: Write JVM formatter tests**

```kotlin
package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JvmFormatterTest {

    // -- Formatting consistency across styles --

    @Test
    fun formatCurrencyStyle_usd_producesExpectedOutput() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyle("1234.56", "USD")
        assertEquals("$1,234.56", result)
    }

    @Test
    fun formatIsoCurrencyStyle_usd_producesExpectedOutput() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatIsoCurrencyStyle("1234.56", "USD")
        assertTrue(result.contains("USD"))
        assertTrue(result.contains("1,234.56"))
    }

    @Test
    fun formatCompactStyle_largeNumber_producesCompactOutput() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCompactStyle("1500000", "USD")
        assertTrue(result.contains("M") || result.contains("1.5"))
    }

    // -- Locale-specific separators --

    @Test
    fun germanLocale_usesCommaDecimalAndDotGrouping() {
        val formatter = CurrencyFormatter(KurrencyLocale.GERMANY)
        val result = formatter.formatCurrencyStyle("1234.56", "EUR")
        assertTrue(result.contains(","))  // decimal separator
    }

    @Test
    fun japaneseLocale_noDecimalForJpy() {
        val formatter = CurrencyFormatter(KurrencyLocale.JAPAN)
        val result = formatter.formatCurrencyStyle("1234", "JPY")
        assertTrue(result.contains("1,234") || result.contains("1234"))
    }

    // -- Edge cases --

    @Test
    fun formatZeroAmount() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyle("0", "USD")
        assertTrue(result.contains("0"))
    }

    @Test
    fun formatNegativeAmount() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyle("-1234.56", "USD")
        assertTrue(result.contains("1,234.56"))
    }

    @Test
    fun formatVeryLargeNumber() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyle("999999999999.99", "USD")
        assertTrue(result.contains("999"))
    }

    @Test
    fun formatMaxFractionDigits_bhd() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyle("1234.567", "BHD")
        assertTrue(result.contains("567"))
    }

    // -- Direct CurrencyFormatterImpl testing --

    @Test
    fun formatterImpl_getFractionDigits_usd() {
        val impl = CurrencyFormatterImpl(KurrencyLocale.US)
        assertEquals(2, impl.getFractionDigitsOrDefault("USD"))
    }

    @Test
    fun formatterImpl_getFractionDigits_jpy() {
        val impl = CurrencyFormatterImpl(KurrencyLocale.US)
        assertEquals(0, impl.getFractionDigitsOrDefault("JPY"))
    }

    @Test
    fun formatterImpl_getFractionDigits_bhd() {
        val impl = CurrencyFormatterImpl(KurrencyLocale.US)
        assertEquals(3, impl.getFractionDigitsOrDefault("BHD"))
    }
}
```

- [ ] **Step 2: Run tests to verify they pass**

Run: `./gradlew :kurrency-core:jvmTest --tests "org.kimplify.kurrency.JvmFormatterTest" -i`
Expected: All tests PASS

- [ ] **Step 3: Commit**

```bash
git add kurrency-core/src/jvmTest/kotlin/org/kimplify/kurrency/JvmFormatterTest.kt
git commit -m "test: Add JVM platform-specific formatter tests"
```

### Task 1.2: JS Platform-Specific Tests

**Files:**
- Create: `kurrency-core/src/jsTest/kotlin/org/kimplify/kurrency/JsFormatterTest.kt`

- [ ] **Step 1: Write JS formatter tests**

```kotlin
package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JsFormatterTest {

    @Test
    fun formatCurrencyStyle_usd_producesExpectedOutput() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyle("1234.56", "USD")
        assertEquals("$1,234.56", result)
    }

    @Test
    fun formatIsoCurrencyStyle_usd_containsCurrencyCode() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatIsoCurrencyStyle("1234.56", "USD")
        assertTrue(result.contains("USD"))
    }

    @Test
    fun germanLocale_usesCorrectSeparators() {
        val formatter = CurrencyFormatter(KurrencyLocale.GERMANY)
        val result = formatter.formatCurrencyStyle("1234.56", "EUR")
        assertTrue(result.contains(","))
    }

    @Test
    fun formatZeroAmount_usd() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyle("0", "USD")
        assertTrue(result.contains("0"))
    }

    @Test
    fun formatNegativeAmount_usd() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyle("-50.00", "USD")
        assertTrue(result.contains("50"))
    }

    @Test
    fun formatVeryLargeNumber() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyle("1000000000.00", "USD")
        assertTrue(result.contains("1,000,000,000") || result.contains("1000000000"))
    }

    @Test
    fun intlNumberFormat_fractionDigits_jpy() {
        val impl = CurrencyFormatterImpl(KurrencyLocale.US)
        assertEquals(0, impl.getFractionDigitsOrDefault("JPY"))
    }

    @Test
    fun intlNumberFormat_fractionDigits_usd() {
        val impl = CurrencyFormatterImpl(KurrencyLocale.US)
        assertEquals(2, impl.getFractionDigitsOrDefault("USD"))
    }

    @Test
    fun intlNumberFormat_fractionDigits_bhd() {
        val impl = CurrencyFormatterImpl(KurrencyLocale.US)
        assertEquals(3, impl.getFractionDigitsOrDefault("BHD"))
    }

    @Test
    fun multipleLocales_produceDistinctOutput() {
        val usFormatter = CurrencyFormatter(KurrencyLocale.US)
        val deFormatter = CurrencyFormatter(KurrencyLocale.GERMANY)
        val usResult = usFormatter.formatCurrencyStyle("1234.56", "USD")
        val deResult = deFormatter.formatCurrencyStyle("1234.56", "EUR")
        assertTrue(usResult != deResult, "US and DE formatting should differ")
    }
}
```

- [ ] **Step 2: Run tests**

Run: `./gradlew :kurrency-core:jsTest --tests "org.kimplify.kurrency.JsFormatterTest" -i`
Expected: All tests PASS

- [ ] **Step 3: Commit**

```bash
git add kurrency-core/src/jsTest/kotlin/org/kimplify/kurrency/JsFormatterTest.kt
git commit -m "test: Add JS platform-specific formatter tests"
```

### Task 1.3: WasmJs Platform-Specific Tests

**Files:**
- Create: `kurrency-core/src/wasmJsTest/kotlin/org/kimplify/kurrency/WasmJsFormatterTest.kt`

- [ ] **Step 1: Write WasmJs formatter tests**

```kotlin
package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WasmJsFormatterTest {

    @Test
    fun formatCurrencyStyle_usd() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyle("1234.56", "USD")
        assertEquals("$1,234.56", result)
    }

    @Test
    fun formatIsoCurrencyStyle_eur() {
        val formatter = CurrencyFormatter(KurrencyLocale.GERMANY)
        val result = formatter.formatIsoCurrencyStyle("1234.56", "EUR")
        assertTrue(result.contains("EUR"))
    }

    @Test
    fun wasmNumberPrecision_largeNumber() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyle("9007199254740992", "USD")
        // 2^53 — max safe integer in JS/Wasm
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun wasmNumberPrecision_smallFraction() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyle("0.01", "USD")
        assertTrue(result.contains("0.01"))
    }

    @Test
    fun formatZero() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyle("0", "USD")
        assertTrue(result.contains("0"))
    }

    @Test
    fun fractionDigits_jpy() {
        val impl = CurrencyFormatterImpl(KurrencyLocale.US)
        assertEquals(0, impl.getFractionDigitsOrDefault("JPY"))
    }

    @Test
    fun fractionDigits_usd() {
        val impl = CurrencyFormatterImpl(KurrencyLocale.US)
        assertEquals(2, impl.getFractionDigitsOrDefault("USD"))
    }
}
```

- [ ] **Step 2: Run tests**

Run: `./gradlew :kurrency-core:wasmJsNodeTest --tests "org.kimplify.kurrency.WasmJsFormatterTest" -i`
Expected: All tests PASS

- [ ] **Step 3: Commit**

```bash
git add kurrency-core/src/wasmJsTest/kotlin/org/kimplify/kurrency/WasmJsFormatterTest.kt
git commit -m "test: Add WasmJs platform-specific formatter tests"
```

### Task 1.4: Expand Android Instrumented Tests

**Files:**
- Modify: `kurrency-core/src/androidTest/kotlin/org/kimplify/kurrency/CurrencyTestInstrumented.kt`

- [ ] **Step 1: Add Android-specific edge case tests**

Add the following tests to the existing `CurrencyTestInstrumented.kt` file (append before final closing brace):

```kotlin
    // -- Android ICU-specific edge cases --

    @Test
    fun icuFormatting_infinityAmount_returnsError() {
        val currency = Kurrency.fromCode("USD").getOrThrow()
        val result = currency.formatAmount(Double.POSITIVE_INFINITY, locale = KurrencyLocale.US)
        assertTrue(result.isFailure)
    }

    @Test
    fun icuFormatting_nanAmount_returnsError() {
        val currency = Kurrency.fromCode("USD").getOrThrow()
        val result = currency.formatAmount(Double.NaN, locale = KurrencyLocale.US)
        assertTrue(result.isFailure)
    }

    @Test
    fun icuFormatting_negativeZero() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyle("-0.00", "USD")
        assertTrue(result.contains("0"))
    }

    @Test
    fun icuFormatting_bhd_threeFractionDigits() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyle("1234.567", "BHD")
        assertTrue(result.contains("567"))
    }

    @Test
    fun icuFormatting_veryLargeAmount() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyle("999999999999.99", "USD")
        assertTrue(result.isNotEmpty())
    }
```

- [ ] **Step 2: Run tests**

Run: `./gradlew :kurrency-core:connectedAndroidTest --tests "org.kimplify.kurrency.CurrencyTestInstrumented" -i`
Expected: All tests PASS

- [ ] **Step 3: Commit**

```bash
git add kurrency-core/src/androidTest/kotlin/org/kimplify/kurrency/CurrencyTestInstrumented.kt
git commit -m "test: Add Android ICU-specific edge case tests"
```

### Task 1.5: iOS Platform-Specific Tests

**Files:**
- Create: `kurrency-core/src/iosTest/kotlin/org/kimplify/kurrency/IosFormatterTest.kt`

- [ ] **Step 1: Verify iosTest source set exists in build.gradle.kts**

Read `kurrency-core/build.gradle.kts` and verify `iosTest` source set is declared. If not, add it.

- [ ] **Step 2: Write iOS formatter tests**

```kotlin
package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IosFormatterTest {

    @Test
    fun formatCurrencyStyle_usd_producesExpectedOutput() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyle("1234.56", "USD")
        assertEquals("$1,234.56", result)
    }

    @Test
    fun formatCompactStyle_trailingSymbolLocale_eur() {
        val formatter = CurrencyFormatter(KurrencyLocale.GERMANY)
        val result = formatter.formatCompactStyle("1500000", "EUR")
        assertTrue(result.isNotEmpty(), "Compact EUR should produce output")
    }

    @Test
    fun nsNumberFormatter_fractionDigits_jpy() {
        val impl = CurrencyFormatterImpl(KurrencyLocale.US)
        assertEquals(0, impl.getFractionDigitsOrDefault("JPY"))
    }

    @Test
    fun nsNumberFormatter_fractionDigits_bhd() {
        val impl = CurrencyFormatterImpl(KurrencyLocale.US)
        assertEquals(3, impl.getFractionDigitsOrDefault("BHD"))
    }

    @Test
    fun formatNegativeAmount() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyle("-1234.56", "USD")
        assertTrue(result.contains("1,234.56"))
    }

    @Test
    fun formatZeroAmount() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyle("0", "USD")
        assertTrue(result.contains("0"))
    }

    @Test
    fun formatVeryLargeNumber() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyle("999999999999.99", "USD")
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun multipleLocales_produceDistinctOutput() {
        val usFormatter = CurrencyFormatter(KurrencyLocale.US)
        val jpFormatter = CurrencyFormatter(KurrencyLocale.JAPAN)
        val usResult = usFormatter.formatCurrencyStyle("1234.56", "USD")
        val jpResult = jpFormatter.formatCurrencyStyle("1234", "JPY")
        assertTrue(usResult != jpResult)
    }
}
```

- [ ] **Step 3: Run tests**

Run: `./gradlew :kurrency-core:iosSimulatorArm64Test --tests "org.kimplify.kurrency.IosFormatterTest" -i`
Expected: All tests PASS

- [ ] **Step 4: Commit**

```bash
git add kurrency-core/src/iosTest/kotlin/org/kimplify/kurrency/IosFormatterTest.kt
git commit -m "test: Add iOS platform-specific formatter tests"
```

### Task 1.6: Strengthen Thread Safety Tests

**Files:**
- Modify: `kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/CurrencyFormatterThreadSafetyTest.kt`

- [ ] **Step 1: Read existing thread safety test file**

Read: `kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/CurrencyFormatterThreadSafetyTest.kt`
Understand what's already tested, then add missing scenarios.

- [ ] **Step 2: Add concurrent CurrencyAmount.fromMajorUnits test**

Append to the existing file:

```kotlin
    @Test
    fun concurrentFromMajorUnits_noCorruption() = runTest {
        val currency = Kurrency.fromCode("USD").getOrThrow()
        val results = (1..100).map { i ->
            async(Dispatchers.Default) {
                CurrencyAmount.fromMajorUnits("${i}.99", currency)
            }
        }.awaitAll()

        results.forEachIndexed { index, result ->
            assertTrue(result.isSuccess, "fromMajorUnits failed for amount ${index + 1}.99")
        }
    }

    @Test
    fun concurrentFormatterCreation_differentLocales() = runTest {
        val locales = listOf(
            KurrencyLocale.US, KurrencyLocale.GERMANY, KurrencyLocale.JAPAN,
            KurrencyLocale.FRANCE, KurrencyLocale.UK, KurrencyLocale.BRAZIL,
            KurrencyLocale.INDIA, KurrencyLocale.KOREA, KurrencyLocale.ITALY,
            KurrencyLocale.SPAIN
        )
        val results = (1..100).map { i ->
            async(Dispatchers.Default) {
                val locale = locales[i % locales.size]
                val formatter = CurrencyFormatter(locale)
                formatter.formatCurrencyStyle("1234.56", "USD")
            }
        }.awaitAll()

        results.forEach { result ->
            assertTrue(result.isNotEmpty(), "Concurrent formatter creation produced empty result")
        }
    }
```

- [ ] **Step 3: Run tests**

Run: `./gradlew :kurrency-core:allTests --tests "org.kimplify.kurrency.CurrencyFormatterThreadSafetyTest" -i`
Expected: All tests PASS

- [ ] **Step 4: Commit**

```bash
git add kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/CurrencyFormatterThreadSafetyTest.kt
git commit -m "test: Expand thread safety tests with concurrent fromMajorUnits and formatter creation"
```

---

## Sprint 2 — RTL & Bidirectional Locale Support

### Task 2.1: Add NumeralSystem Enum

**Files:**
- Create: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/NumeralSystem.kt`

- [ ] **Step 1: Create NumeralSystem enum**

```kotlin
package org.kimplify.kurrency

/**
 * Represents the numeral system used by a locale for digit rendering.
 */
enum class NumeralSystem {
    /** Western Arabic digits: 0-9 */
    WESTERN,
    /** Eastern Arabic digits: ٠-٩ */
    EASTERN_ARABIC,
    /** Persian/Farsi digits: ۰-۹ */
    PERSIAN
}
```

- [ ] **Step 2: Commit**

```bash
git add kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/NumeralSystem.kt
git commit -m "feat: Add NumeralSystem enum for locale digit rendering"
```

### Task 2.2: Add RTL Properties to KurrencyLocale (expect)

**Files:**
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/KurrencyLocale.kt`

- [ ] **Step 1: Read current KurrencyLocale expect declaration**

Read: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/KurrencyLocale.kt`

- [ ] **Step 2: Add isRightToLeft and numeralSystem to expect class**

Add the two new properties to the `expect class KurrencyLocale` body. These must have matching `actual` implementations on every platform.

```kotlin
/** Whether this locale's script direction is right-to-left. */
val isRightToLeft: Boolean

/** The numeral system used by this locale. */
val numeralSystem: NumeralSystem
```

- [ ] **Step 3: Commit (will not compile yet — actual implementations pending)**

```bash
git add kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/KurrencyLocale.kt
git commit -m "feat: Add isRightToLeft and numeralSystem to KurrencyLocale expect class"
```

### Task 2.3: Implement Actual KurrencyLocale on All Platforms

**Files:**
- Modify: `kurrency-core/src/androidMain/kotlin/org/kimplify/kurrency/KurrencyLocale.kt`
- Modify: `kurrency-core/src/iosMain/kotlin/org/kimplify/kurrency/KurrencyLocale.kt`
- Modify: `kurrency-core/src/jvmMain/kotlin/org/kimplify/kurrency/KurrencyLocale.kt`
- Modify: `kurrency-core/src/webMain/kotlin/org/kimplify/kurrency/KurrencyLocale.web.kt` (shared JS/WasmJs)

- [ ] **Step 1: Read all platform actual files**

Read each of the four platform `KurrencyLocale` actual files to understand their structure.

- [ ] **Step 2: Implement platform-specific RTL detection**

Each platform uses its best available API per the spec:

**Android** (`kurrency-core/src/androidMain/.../KurrencyLocale.kt`):
```kotlin
override val isRightToLeft: Boolean
    get() = android.text.TextUtils.getLayoutDirectionFromLocale(locale) ==
        android.util.LayoutDirection.RTL

override val numeralSystem: NumeralSystem
    get() = numeralSystemFromTag(languageTag)
```

**iOS** (`kurrency-core/src/iosMain/.../KurrencyLocale.kt`):
```kotlin
override val isRightToLeft: Boolean
    get() = platform.Foundation.NSLocale.characterDirectionForLanguage(
        languageTag.substringBefore("-")
    ) == platform.Foundation.NSLocaleLanguageDirectionRightToLeft

override val numeralSystem: NumeralSystem
    get() = numeralSystemFromTag(languageTag)
```

**JVM** (`kurrency-core/src/jvmMain/.../KurrencyLocale.kt`):
```kotlin
// Hardcoded lookup — avoids java.awt.ComponentOrientation (unreliable in headless)
override val isRightToLeft: Boolean
    get() = languageTag.substringBefore("-").lowercase() in RTL_LANGUAGES

override val numeralSystem: NumeralSystem
    get() = numeralSystemFromTag(languageTag)
```

**Web (shared JS/WasmJs)** (`kurrency-core/src/webMain/.../KurrencyLocale.web.kt`):
```kotlin
// Hardcoded lookup — Intl.Locale.textInfo not universally available
override val isRightToLeft: Boolean
    get() = languageTag.substringBefore("-").lowercase() in RTL_LANGUAGES

override val numeralSystem: NumeralSystem
    get() = numeralSystemFromTag(languageTag)
```

**Shared helper (add to each platform file or extract to common internal):**
```kotlin
private val RTL_LANGUAGES = setOf("ar", "he", "fa", "ur", "dv", "ps", "yi", "ku", "sd")

private fun numeralSystemFromTag(tag: String): NumeralSystem {
    val lang = tag.substringBefore("-").lowercase()
    return when (lang) {
        "fa" -> NumeralSystem.PERSIAN
        "ar" -> NumeralSystem.EASTERN_ARABIC
        "ur", "ps" -> NumeralSystem.EASTERN_ARABIC  // Urdu/Pashto use Eastern Arabic digits, not Persian
        else -> NumeralSystem.WESTERN
    }
}
```

> **Note:** Urdu (`ur`) and Pashto (`ps`) use Eastern Arabic digits (`٠-٩`), not Persian digits (`۰-۹`). This is corrected from the initial design.

- [ ] **Step 3: Verify compilation**

Run: `./gradlew :kurrency-core:compileKotlinJvm :kurrency-core:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add kurrency-core/src/androidMain/kotlin/org/kimplify/kurrency/KurrencyLocale.kt \
       kurrency-core/src/iosMain/kotlin/org/kimplify/kurrency/KurrencyLocale.kt \
       kurrency-core/src/jvmMain/kotlin/org/kimplify/kurrency/KurrencyLocale.kt \
       kurrency-core/src/webMain/kotlin/org/kimplify/kurrency/KurrencyLocale.web.kt
git commit -m "feat: Implement isRightToLeft and numeralSystem on all platforms"
```

### Task 2.4: Add New RTL Locale Constants

**Files:**
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/KurrencyLocale.kt` (expect companion)
- Modify: all 4 platform actual files (add actual companion values)

- [ ] **Step 1: Add expect companion constants**

Add to the `KurrencyLocale` expect companion object (SAUDI_ARABIA already exists):

```kotlin
val ARABIC_EG: KurrencyLocale
val HEBREW: KurrencyLocale
val PERSIAN: KurrencyLocale
val URDU: KurrencyLocale
```

- [ ] **Step 2: Add actual implementations on each platform**

On each platform, add the actual companion values using the platform-specific locale constructor:

- **Android:** `actual val ARABIC_EG = KurrencyLocale(java.util.Locale("ar", "EG"))` etc.
- **iOS:** Create from language tags using `NSLocale(localeIdentifier: "ar_EG")` etc.
- **JVM:** Same as Android approach with `java.util.Locale`
- **Web:** `actual val ARABIC_EG = KurrencyLocale("ar-EG")` etc.

- [ ] **Step 3: Verify compilation and run locale tests**

Run: `./gradlew :kurrency-core:allTests --tests "org.kimplify.kurrency.KurrencyLocaleTest" -i`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/KurrencyLocale.kt \
       kurrency-core/src/androidMain/kotlin/org/kimplify/kurrency/KurrencyLocale.kt \
       kurrency-core/src/iosMain/kotlin/org/kimplify/kurrency/KurrencyLocale.kt \
       kurrency-core/src/jvmMain/kotlin/org/kimplify/kurrency/KurrencyLocale.kt \
       kurrency-core/src/webMain/kotlin/org/kimplify/kurrency/KurrencyLocale.web.kt
git commit -m "feat: Add ARABIC_EG, HEBREW, PERSIAN, URDU locale constants"
```

### Task 2.5: RTL-Aware CurrencyVisualTransformation

**Files:**
- Modify: `kurrency-compose/src/commonMain/kotlin/org/kimplify/kurrency/compose/CurrencyVisualTransformation.kt`

- [ ] **Step 1: Read current CurrencyVisualTransformation**

Read: `kurrency-compose/src/commonMain/kotlin/org/kimplify/kurrency/compose/CurrencyVisualTransformation.kt`

- [ ] **Step 2: Add RTL bidi mark handling to offset mapping**

In the `filter()` method and `CurrencyOffsetMapping`, add logic to skip Unicode bidi marks (`\u200F`, `\u200E`, `\u061C`) when calculating digit positions. These marks have zero visual width but occupy positions in the string:

```kotlin
// When building digitPositions list in filter(), skip bidi marks:
private fun isBidiMark(c: Char): Boolean =
    c == '\u200F' || c == '\u200E' || c == '\u061C' || c == '\u200B'
```

When iterating the formatted string to find digit positions, treat bidi marks as invisible separators (same as grouping separators).

- [ ] **Step 3: Verify compilation**

Run: `./gradlew :kurrency-compose:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add kurrency-compose/src/commonMain/kotlin/org/kimplify/kurrency/compose/CurrencyVisualTransformation.kt
git commit -m "feat: Add RTL bidi mark handling to CurrencyVisualTransformation offset mapping"
```

### Task 2.6: RTL Test Suite

**Files:**
- Create: `kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/RtlFormattingTest.kt`

- [ ] **Step 1: Write RTL formatting tests**

```kotlin
package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RtlFormattingTest {

    // -- isRightToLeft property --

    @Test
    fun usLocale_isNotRtl() {
        assertFalse(KurrencyLocale.US.isRightToLeft)
    }

    @Test
    fun germanyLocale_isNotRtl() {
        assertFalse(KurrencyLocale.GERMANY.isRightToLeft)
    }

    @Test
    fun saudiArabiaLocale_isRtl() {
        assertTrue(KurrencyLocale.SAUDI_ARABIA.isRightToLeft)
    }

    @Test
    fun arabicEgLocale_isRtl() {
        assertTrue(KurrencyLocale.ARABIC_EG.isRightToLeft)
    }

    @Test
    fun hebrewLocale_isRtl() {
        assertTrue(KurrencyLocale.HEBREW.isRightToLeft)
    }

    @Test
    fun persianLocale_isRtl() {
        assertTrue(KurrencyLocale.PERSIAN.isRightToLeft)
    }

    @Test
    fun urduLocale_isRtl() {
        assertTrue(KurrencyLocale.URDU.isRightToLeft)
    }

    // -- numeralSystem property --

    @Test
    fun usLocale_westernNumerals() {
        assertEquals(NumeralSystem.WESTERN, KurrencyLocale.US.numeralSystem)
    }

    @Test
    fun saudiArabiaLocale_easternArabicNumerals() {
        assertEquals(NumeralSystem.EASTERN_ARABIC, KurrencyLocale.SAUDI_ARABIA.numeralSystem)
    }

    @Test
    fun persianLocale_persianNumerals() {
        assertEquals(NumeralSystem.PERSIAN, KurrencyLocale.PERSIAN.numeralSystem)
    }

    @Test
    fun hebrewLocale_westernNumerals() {
        assertEquals(NumeralSystem.WESTERN, KurrencyLocale.HEBREW.numeralSystem)
    }

    // -- RTL locale formatting produces non-empty output --

    @Test
    fun saudiArabia_formatCurrencyStyle_sar() {
        val formatter = CurrencyFormatter(KurrencyLocale.SAUDI_ARABIA)
        val result = formatter.formatCurrencyStyle("1234.56", "SAR")
        assertTrue(result.isNotEmpty(), "SAR formatting should produce output")
    }

    @Test
    fun saudiArabia_formatIsoCurrencyStyle_sar() {
        val formatter = CurrencyFormatter(KurrencyLocale.SAUDI_ARABIA)
        val result = formatter.formatIsoCurrencyStyle("1234.56", "SAR")
        assertTrue(result.contains("SAR"), "ISO style should contain currency code")
    }

    @Test
    fun hebrew_formatCurrencyStyle_ils() {
        val formatter = CurrencyFormatter(KurrencyLocale.HEBREW)
        val result = formatter.formatCurrencyStyle("1234.56", "ILS")
        assertTrue(result.isNotEmpty(), "ILS formatting should produce output")
    }

    @Test
    fun persian_formatCurrencyStyle_irr() {
        val formatter = CurrencyFormatter(KurrencyLocale.PERSIAN)
        val result = formatter.formatCurrencyStyle("1234", "IRR")
        assertTrue(result.isNotEmpty(), "IRR formatting should produce output")
    }

    @Test
    fun arabicEg_formatCurrencyStyle_egp() {
        val formatter = CurrencyFormatter(KurrencyLocale.ARABIC_EG)
        val result = formatter.formatCurrencyStyle("1234.56", "EGP")
        assertTrue(result.isNotEmpty(), "EGP formatting should produce output")
    }

    // -- All predefined locales have correct isRightToLeft --

    @Test
    fun allLtrLocales_areNotRtl() {
        val ltrLocales = listOf(
            KurrencyLocale.US, KurrencyLocale.UK, KurrencyLocale.CANADA,
            KurrencyLocale.CANADA_FRENCH, KurrencyLocale.GERMANY, KurrencyLocale.FRANCE,
            KurrencyLocale.ITALY, KurrencyLocale.SPAIN, KurrencyLocale.JAPAN,
            KurrencyLocale.CHINA, KurrencyLocale.KOREA, KurrencyLocale.BRAZIL,
            KurrencyLocale.RUSSIA, KurrencyLocale.INDIA,
        )
        ltrLocales.forEach { locale ->
            assertFalse(locale.isRightToLeft, "${locale.languageTag} should be LTR")
        }
    }
}
```

- [ ] **Step 2: Run tests**

Run: `./gradlew :kurrency-core:allTests --tests "org.kimplify.kurrency.RtlFormattingTest" -i`
Expected: All tests PASS

- [ ] **Step 3: Commit**

```bash
git add kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/RtlFormattingTest.kt
git commit -m "test: Add comprehensive RTL formatting test suite"
```

---

## Sprint 3 — Currency Parsing

### Task 3.1: Enhance Parsing in CurrencyFormat Interface

**Files:**
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyFormat.kt`

- [ ] **Step 1: Read current CurrencyFormat interface**

Read: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyFormat.kt`

- [ ] **Step 2: Add new parsing method declarations with Result return types**

Add to the `CurrencyFormat` interface (matching spec and project convention):

```kotlin
/**
 * Parses a formatted currency string to minor units (e.g., cents).
 * Uses string-based arithmetic to avoid Double precision loss.
 */
fun parseToMinorUnitsResult(formattedText: String, currencyCode: String): Result<Long>

/**
 * Parses a formatted currency string to a [CurrencyAmount].
 */
fun parseToCurrencyAmountResult(formattedText: String, currency: Kurrency): Result<CurrencyAmount>
```

- [ ] **Step 3: Commit**

```bash
git add kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyFormat.kt
git commit -m "feat: Add parseToMinorUnits and parseToCurrencyAmount to CurrencyFormat interface"
```

### Task 3.2: Write Parsing Tests First (TDD)

**Files:**
- Create: `kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/CurrencyParsingTest.kt`

- [ ] **Step 1: Write comprehensive parsing tests**

```kotlin
package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

class CurrencyParsingTest {

    // -- Round-trip: Standard style --

    @Test
    fun roundTrip_standardStyle_usd_usLocale() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val formatted = formatter.formatCurrencyStyle("1234.56", "USD")
        val parsed = formatter.parseCurrencyAmountResult(formatted, "USD")
        assertTrue(parsed.isSuccess)
        assertEquals(1234.56, parsed.getOrThrow(), 0.001)
    }

    @Test
    fun roundTrip_standardStyle_eur_germanyLocale() {
        val formatter = CurrencyFormatter(KurrencyLocale.GERMANY)
        val formatted = formatter.formatCurrencyStyle("1234.56", "EUR")
        val parsed = formatter.parseCurrencyAmountResult(formatted, "EUR")
        assertTrue(parsed.isSuccess)
        assertEquals(1234.56, parsed.getOrThrow(), 0.001)
    }

    @Test
    fun roundTrip_standardStyle_jpy_japanLocale() {
        val formatter = CurrencyFormatter(KurrencyLocale.JAPAN)
        val formatted = formatter.formatCurrencyStyle("1234", "JPY")
        val parsed = formatter.parseCurrencyAmountResult(formatted, "JPY")
        assertTrue(parsed.isSuccess)
        assertEquals(1234.0, parsed.getOrThrow(), 0.001)
    }

    @Test
    fun roundTrip_standardStyle_brl_brazilLocale() {
        val formatter = CurrencyFormatter(KurrencyLocale.BRAZIL)
        val formatted = formatter.formatCurrencyStyle("1234.56", "BRL")
        val parsed = formatter.parseCurrencyAmountResult(formatted, "BRL")
        assertTrue(parsed.isSuccess)
        assertEquals(1234.56, parsed.getOrThrow(), 0.001)
    }

    // -- Round-trip: ISO style --

    @Test
    fun roundTrip_isoStyle_usd_usLocale() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val formatted = formatter.formatIsoCurrencyStyle("1234.56", "USD")
        val parsed = formatter.parseCurrencyAmountResult(formatted, "USD")
        assertTrue(parsed.isSuccess)
        assertEquals(1234.56, parsed.getOrThrow(), 0.001)
    }

    // -- Accounting negatives --

    @Test
    fun parse_accountingNegative_parentheses() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val parsed = formatter.parseCurrencyAmountResult("($1,234.56)", "USD")
        assertTrue(parsed.isSuccess)
        assertEquals(-1234.56, parsed.getOrThrow(), 0.001)
    }

    // -- Compact parsing (English only) --

    @Test
    fun parse_compactK() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val parsed = formatter.parseCurrencyAmountResult("$1.2K", "USD")
        assertTrue(parsed.isSuccess)
        assertEquals(1200.0, parsed.getOrThrow(), 0.001)
    }

    @Test
    fun parse_compactM() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val parsed = formatter.parseCurrencyAmountResult("$1.5M", "USD")
        assertTrue(parsed.isSuccess)
        assertEquals(1500000.0, parsed.getOrThrow(), 0.001)
    }

    @Test
    fun parse_compactB() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val parsed = formatter.parseCurrencyAmountResult("$2.1B", "USD")
        assertTrue(parsed.isSuccess)
        assertEquals(2100000000.0, parsed.getOrThrow(), 1.0)
    }

    @Test
    fun parse_nonEnglishCompact_returnsError() {
        val formatter = CurrencyFormatter(KurrencyLocale.GERMANY)
        val parsed = formatter.parseCurrencyAmountResult("1,5Mio", "EUR")
        assertTrue(parsed.isFailure)
    }

    // -- Edge cases --

    @Test
    fun parse_zero_usd() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val parsed = formatter.parseCurrencyAmountResult("$0.00", "USD")
        assertTrue(parsed.isSuccess)
        assertEquals(0.0, parsed.getOrThrow(), 0.001)
    }

    @Test
    fun parse_zeroJpy() {
        val formatter = CurrencyFormatter(KurrencyLocale.JAPAN)
        val parsed = formatter.parseCurrencyAmountResult("¥0", "JPY")
        assertTrue(parsed.isSuccess)
        assertEquals(0.0, parsed.getOrThrow(), 0.001)
    }

    @Test
    fun parse_emptyString_returnsError() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val parsed = formatter.parseCurrencyAmountResult("", "USD")
        assertTrue(parsed.isFailure)
    }

    @Test
    fun parse_whitespaceOnly_returnsError() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val parsed = formatter.parseCurrencyAmountResult("   ", "USD")
        assertTrue(parsed.isFailure)
    }

    @Test
    fun parse_invalidInput_returnsInvalidAmountError() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.parseCurrencyAmountResult("not a number", "USD")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is KurrencyError.InvalidAmount)
    }

    // -- Minor units round-trip (Result<Long> API) --

    @Test
    fun parseToMinorUnits_usd() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.parseToMinorUnitsResult("$1,234.56", "USD")
        assertTrue(result.isSuccess)
        assertEquals(123456L, result.getOrThrow())
    }

    @Test
    fun parseToMinorUnits_jpy() {
        val formatter = CurrencyFormatter(KurrencyLocale.JAPAN)
        val result = formatter.parseToMinorUnitsResult("¥1,234", "JPY")
        assertTrue(result.isSuccess)
        assertEquals(1234L, result.getOrThrow())
    }

    @Test
    fun parseToMinorUnits_bhd_threeDecimals() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.parseToMinorUnitsResult("BHD1,234.567", "BHD")
        assertTrue(result.isSuccess)
        assertEquals(1234567L, result.getOrThrow())
    }

    // -- parseToCurrencyAmount (Result<CurrencyAmount> API) --

    @Test
    fun parseToCurrencyAmount_usd() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val usd = Kurrency.fromCode("USD").getOrThrow()
        val result = formatter.parseToCurrencyAmountResult("$1,234.56", usd)
        assertTrue(result.isSuccess)
        assertEquals(123456L, result.getOrThrow().minorUnits)
    }

    // -- CurrencyAmount.parse convenience --

    @Test
    fun currencyAmount_parse_usd() {
        val usd = Kurrency.fromCode("USD").getOrThrow()
        val result = CurrencyAmount.parse("$1,234.56", usd, KurrencyLocale.US)
        assertTrue(result.isSuccess)
        assertEquals(123456L, result.getOrThrow().minorUnits)
    }

    // -- RTL formatted strings --

    @Test
    fun parse_saudiArabiaFormatted_sar() {
        val formatter = CurrencyFormatter(KurrencyLocale.SAUDI_ARABIA)
        val formatted = formatter.formatCurrencyStyle("1234.56", "SAR")
        val parsed = formatter.parseCurrencyAmountResult(formatted, "SAR")
        assertTrue(parsed.isSuccess, "Should parse SAR formatted in ar-SA locale")
        assertEquals(1234.56, parsed.getOrThrow(), 0.01)
    }
}
```

- [ ] **Step 2: Run tests to verify they FAIL (parsing not yet implemented)**

Run: `./gradlew :kurrency-core:allTests --tests "org.kimplify.kurrency.CurrencyParsingTest" -i`
Expected: FAIL (many tests fail because parsing is not robust yet)

- [ ] **Step 3: Commit failing tests**

```bash
git add kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/CurrencyParsingTest.kt
git commit -m "test: Add comprehensive currency parsing tests (TDD — tests fail)"
```

### Task 3.3: Implement Robust Parsing Pipeline

**Files:**
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyFormatter.kt`
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyAmount.kt`

- [ ] **Step 1: Read current CurrencyFormatter parsing code**

Read: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyFormatter.kt`
Focus on `parseCurrencyAmount` and `parseCurrencyAmountResult` methods.

- [ ] **Step 2: Add locale-aware normalizeAmount overload to StringExtensions**

The existing `String.normalizeAmount()` takes no parameters. Many new functions need locale-aware normalization. Add an overload in `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/extensions/StringExtensions.kt`:

```kotlin
/**
 * Normalizes a currency amount string using locale-specific separators.
 * Strips grouping separators and normalizes decimal separator to '.'.
 */
internal fun String.normalizeAmount(locale: KurrencyLocale): String {
    val groupingSep = locale.groupingSeparator
    val decimalSep = locale.decimalSeparator
    var result = this.replace(groupingSep.toString(), "")
        .replace("\u00A0", "") // non-breaking space
        .replace("\u202F", "") // narrow no-break space
    if (decimalSep != '.') {
        result = result.replace(decimalSep, '.')
    }
    return result.trim()
}
```

- [ ] **Step 3: Implement robust parsing in CurrencyFormatter**

Replace the existing `parseCurrencyAmountResult` body. The new implementation handles the full pipeline in `CurrencyFormatter` directly (not delegating to platform `impl.parseCurrencyAmount`):

```kotlin
fun parseCurrencyAmountResult(formattedText: String, currencyCode: String): Result<Double> = runCatching {
    val text = formattedText.trim()
    if (text.isEmpty()) throw KurrencyError.InvalidAmount(formattedText)

    var working = text

    // 1. Strip bidi marks
    working = working.replace("\u200F", "").replace("\u200E", "")
        .replace("\u061C", "").replace("\u200B", "")

    // 2. Detect and handle accounting notation (parentheses = negative)
    val isAccountingNegative = working.startsWith("(") && working.endsWith(")")
    if (isAccountingNegative) {
        working = working.substring(1, working.length - 1).trim()
    }

    // 3. Strip currency symbol or ISO code
    val metadata = CurrencyMetadata.parse(currencyCode).getOrNull()
    if (metadata != null) {
        working = working.replace(metadata.symbol, "").trim()
    }
    working = working.replace(currencyCode, "").trim()

    // 4. Detect compact suffixes (English only — non-English suffixes like "Mio"
    //    will remain in the string and cause toDoubleOrNull() to return null,
    //    which correctly produces KurrencyError.InvalidAmount)
    val compactMultiplier = detectCompactMultiplier(working)
    if (compactMultiplier != null) {
        working = working.dropLast(compactMultiplier.second.length).trim()
    }

    // 5. Strip grouping separators and normalize decimal (locale-aware)
    working = working.normalizeAmount(locale)

    // 6. Handle negative sign
    val isNegative = isAccountingNegative || working.startsWith("-")
    working = working.removePrefix("-").removePrefix("+")

    // 7. Validate and parse
    val value = working.toDoubleOrNull() ?: throw KurrencyError.InvalidAmount(formattedText)

    var result = if (isNegative) -value else value
    if (compactMultiplier != null) {
        result *= compactMultiplier.first
    }
    result
}

private fun detectCompactMultiplier(text: String): Pair<Double, String>? {
    val suffixes = listOf(
        "T" to 1_000_000_000_000.0,
        "B" to 1_000_000_000.0,
        "M" to 1_000_000.0,
        "K" to 1_000.0
    )
    for ((suffix, multiplier) in suffixes) {
        if (text.endsWith(suffix, ignoreCase = true)) {
            return multiplier to suffix
        }
    }
    return null
}
```

- [ ] **Step 4: Implement parseToMinorUnitsResult using string-based arithmetic (NOT through Double)**

This function parses the formatted string to a normalized decimal string, then splits at the decimal point and converts directly to Long minor units — never going through `Double`:

```kotlin
fun parseToMinorUnitsResult(formattedText: String, currencyCode: String): Result<Long> = runCatching {
    val text = formattedText.trim()
    if (text.isEmpty()) throw KurrencyError.InvalidAmount(formattedText)

    // Reuse the stripping logic but stop before converting to Double
    var working = text
    working = working.replace("\u200F", "").replace("\u200E", "")
        .replace("\u061C", "").replace("\u200B", "")

    val isAccountingNegative = working.startsWith("(") && working.endsWith(")")
    if (isAccountingNegative) working = working.substring(1, working.length - 1).trim()

    val metadata = CurrencyMetadata.parse(currencyCode).getOrNull()
    if (metadata != null) working = working.replace(metadata.symbol, "").trim()
    working = working.replace(currencyCode, "").trim()

    working = working.normalizeAmount(locale)

    val isNegative = isAccountingNegative || working.startsWith("-")
    working = working.removePrefix("-").removePrefix("+")

    // String-based decimal → minor units conversion
    val fractionDigits = getFractionDigitsOrDefault(currencyCode)
    val parts = working.split(".")
    val intPart = parts[0].toLongOrNull() ?: throw KurrencyError.InvalidAmount(formattedText)
    val fracStr = if (parts.size > 1) {
        parts[1].padEnd(fractionDigits, '0').take(fractionDigits)
    } else {
        "0".repeat(fractionDigits)
    }
    val fracPart = fracStr.toLongOrNull() ?: throw KurrencyError.InvalidAmount(formattedText)

    var multiplier = 1L
    repeat(fractionDigits) { multiplier *= 10 }

    val minorUnits = intPart * multiplier + fracPart
    if (isNegative) -minorUnits else minorUnits
}
```

- [ ] **Step 5: Implement parseToCurrencyAmountResult**

```kotlin
fun parseToCurrencyAmountResult(formattedText: String, currency: Kurrency): Result<CurrencyAmount> = runCatching {
    val minorUnits = parseToMinorUnitsResult(formattedText, currency.code).getOrThrow()
    CurrencyAmount.of(minorUnits, currency)
}
```

- [ ] **Step 5: Add CurrencyAmount.parse convenience**

In `CurrencyAmount.kt`, add to companion object:

```kotlin
fun parse(
    formattedText: String,
    currency: Kurrency,
    locale: KurrencyLocale
): Result<CurrencyAmount> = runCatching {
    val formatter = CurrencyFormatter(locale)
    formatter.parseToCurrencyAmount(formattedText, currency)
        ?: throw KurrencyError.InvalidAmount(formattedText)
}
```

- [ ] **Step 6: Run tests**

Run: `./gradlew :kurrency-core:allTests --tests "org.kimplify.kurrency.CurrencyParsingTest" -i`
Expected: All tests PASS

- [ ] **Step 7: Run all existing tests for regression**

Run: `./gradlew :kurrency-core:allTests -i`
Expected: All tests PASS

- [ ] **Step 8: Commit**

```bash
git add kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyFormatter.kt \
       kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyAmount.kt \
       kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyFormat.kt
git commit -m "feat: Implement robust currency parsing pipeline with round-trip support"
```

---

## Sprint 4 — Custom Format Options (Builder API)

### Task 4.1: Create CurrencyFormatOptions and Enums

**Files:**
- Create: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyFormatOptions.kt`

- [ ] **Step 1: Create the options data class with supporting enums**

```kotlin
package org.kimplify.kurrency

/**
 * Fine-grained formatting control beyond predefined [CurrencyStyle].
 */
data class CurrencyFormatOptions(
    val symbolPosition: SymbolPosition = SymbolPosition.LOCALE_DEFAULT,
    val grouping: Boolean = true,
    val minFractionDigits: Int? = null,
    val maxFractionDigits: Int? = null,
    val negativeStyle: NegativeStyle = NegativeStyle.MINUS_SIGN,
    val symbolDisplay: SymbolDisplay = SymbolDisplay.SYMBOL,
    val zeroDisplay: ZeroDisplay = ZeroDisplay.SHOW,
) {
    init {
        if (minFractionDigits != null && maxFractionDigits != null) {
            require(minFractionDigits <= maxFractionDigits) {
                "minFractionDigits ($minFractionDigits) must be <= maxFractionDigits ($maxFractionDigits)"
            }
        }
    }

    companion object {
        /** Options equivalent to [CurrencyStyle.Standard]. */
        val STANDARD = CurrencyFormatOptions()

        /** Options equivalent to [CurrencyStyle.Iso]. */
        val ISO = CurrencyFormatOptions(symbolDisplay = SymbolDisplay.ISO_CODE)

        /** Options equivalent to [CurrencyStyle.Accounting]. */
        val ACCOUNTING = CurrencyFormatOptions(negativeStyle = NegativeStyle.PARENTHESES)

        fun builder() = Builder()

        inline operator fun invoke(block: Builder.() -> Unit): CurrencyFormatOptions =
            Builder().apply(block).build()
    }

    class Builder {
        var symbolPosition: SymbolPosition = SymbolPosition.LOCALE_DEFAULT
        var grouping: Boolean = true
        var minFractionDigits: Int? = null
        var maxFractionDigits: Int? = null
        var negativeStyle: NegativeStyle = NegativeStyle.MINUS_SIGN
        var symbolDisplay: SymbolDisplay = SymbolDisplay.SYMBOL
        var zeroDisplay: ZeroDisplay = ZeroDisplay.SHOW

        fun symbolPosition(value: SymbolPosition) = apply { symbolPosition = value }
        fun grouping(value: Boolean) = apply { grouping = value }
        fun minFractionDigits(value: Int?) = apply { minFractionDigits = value }
        fun maxFractionDigits(value: Int?) = apply { maxFractionDigits = value }
        fun negativeStyle(value: NegativeStyle) = apply { negativeStyle = value }
        fun symbolDisplay(value: SymbolDisplay) = apply { symbolDisplay = value }
        fun zeroDisplay(value: ZeroDisplay) = apply { zeroDisplay = value }

        fun build() = CurrencyFormatOptions(
            symbolPosition, grouping, minFractionDigits, maxFractionDigits,
            negativeStyle, symbolDisplay, zeroDisplay
        )
    }
}

enum class SymbolPosition { LEADING, TRAILING, LOCALE_DEFAULT }

enum class NegativeStyle { MINUS_SIGN, PARENTHESES, LOCALE_DEFAULT }

enum class SymbolDisplay { SYMBOL, ISO_CODE, NAME, NONE }

enum class ZeroDisplay { SHOW, DASH, EMPTY }
```

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :kurrency-core:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyFormatOptions.kt
git commit -m "feat: Add CurrencyFormatOptions data class with builder and DSL"
```

### Task 4.2: Write Format Options Tests (TDD)

**Files:**
- Create: `kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/CurrencyFormatOptionsTest.kt`

- [ ] **Step 1: Write tests**

```kotlin
package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class CurrencyFormatOptionsTest {

    // -- Default options match Standard style --

    @Test
    fun defaultOptions_matchStandardStyle() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val standard = formatter.formatCurrencyStyle("1234.56", "USD")
        val withOptions = formatter.formatWithOptions("1234.56", "USD", CurrencyFormatOptions.STANDARD)
        assertTrue(withOptions.isSuccess)
        assertEquals(standard, withOptions.getOrThrow())
    }

    // -- Grouping --

    @Test
    fun groupingDisabled_noGroupingSeparators() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val options = CurrencyFormatOptions(grouping = false)
        val result = formatter.formatWithOptions("1234.56", "USD", options)
        assertTrue(result.isSuccess)
        val formatted = result.getOrThrow()
        assertTrue(!formatted.contains(","), "Should not contain grouping separator: $formatted")
    }

    // -- Symbol display --

    @Test
    fun symbolDisplayNone_noSymbolOrCode() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val options = CurrencyFormatOptions(symbolDisplay = SymbolDisplay.NONE)
        val result = formatter.formatWithOptions("1234.56", "USD", options)
        assertTrue(result.isSuccess)
        val formatted = result.getOrThrow()
        assertTrue(!formatted.contains("$"), "Should not contain symbol: $formatted")
        assertTrue(!formatted.contains("USD"), "Should not contain code: $formatted")
    }

    @Test
    fun symbolDisplayIsoCode_containsCode() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val options = CurrencyFormatOptions(symbolDisplay = SymbolDisplay.ISO_CODE)
        val result = formatter.formatWithOptions("1234.56", "USD", options)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().contains("USD"))
    }

    // -- Negative style --

    @Test
    fun negativeParentheses_wrapsInParens() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val options = CurrencyFormatOptions(negativeStyle = NegativeStyle.PARENTHESES)
        val result = formatter.formatWithOptions("-1234.56", "USD", options)
        assertTrue(result.isSuccess)
        val formatted = result.getOrThrow()
        assertTrue(formatted.contains("(") && formatted.contains(")"), "Should wrap in parens: $formatted")
    }

    // -- Zero display --

    @Test
    fun zeroDisplayDash_showsDash() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val options = CurrencyFormatOptions(zeroDisplay = ZeroDisplay.DASH)
        val result = formatter.formatWithOptions("0", "USD", options)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().contains("—"), "Should show dash for zero")
    }

    @Test
    fun zeroDisplayEmpty_showsEmpty() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val options = CurrencyFormatOptions(zeroDisplay = ZeroDisplay.EMPTY)
        val result = formatter.formatWithOptions("0", "USD", options)
        assertTrue(result.isSuccess)
        assertEquals("", result.getOrThrow())
    }

    // -- Fraction digits --

    @Test
    fun customFractionDigits() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val options = CurrencyFormatOptions(minFractionDigits = 4, maxFractionDigits = 4)
        val result = formatter.formatWithOptions("1234.5", "USD", options)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().contains("1234.5000") || result.getOrThrow().contains("1,234.5000"))
    }

    // -- Validation --

    @Test
    fun invalidFractionDigits_throws() {
        assertFailsWith<IllegalArgumentException> {
            CurrencyFormatOptions(minFractionDigits = 4, maxFractionDigits = 2)
        }
    }

    // -- Builder API --

    @Test
    fun builderProducesCorrectOptions() {
        val options = CurrencyFormatOptions.builder()
            .grouping(false)
            .symbolPosition(SymbolPosition.TRAILING)
            .build()
        assertEquals(false, options.grouping)
        assertEquals(SymbolPosition.TRAILING, options.symbolPosition)
    }

    // -- DSL --

    @Test
    fun dslProducesCorrectOptions() {
        val options = CurrencyFormatOptions {
            grouping = false
            symbolDisplay = SymbolDisplay.NONE
        }
        assertEquals(false, options.grouping)
        assertEquals(SymbolDisplay.NONE, options.symbolDisplay)
    }
}
```

- [ ] **Step 2: Run tests to verify they FAIL**

Run: `./gradlew :kurrency-core:allTests --tests "org.kimplify.kurrency.CurrencyFormatOptionsTest" -i`
Expected: FAIL (formatWithOptions not implemented yet)

- [ ] **Step 3: Commit**

```bash
git add kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/CurrencyFormatOptionsTest.kt
git commit -m "test: Add CurrencyFormatOptions tests (TDD — tests fail)"
```

### Task 4.3: Implement formatWithOptions

**Files:**
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyFormatter.kt`
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/Kurrency.kt`
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyAmount.kt`

- [ ] **Step 1: Read current CurrencyFormatter**

Read: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyFormatter.kt`

- [ ] **Step 2: Add formatWithOptions to CurrencyFormat interface**

In `CurrencyFormat.kt`, add:
```kotlin
fun formatWithOptions(amount: String, currencyCode: String, options: CurrencyFormatOptions): String
```

- [ ] **Step 3: Implement formatWithOptions on each platform CurrencyFormatterImpl**

Each platform configures its native formatter with the options. Example for **JVM** (`kurrency-core/src/jvmMain/.../CurrencyFormatterImpl.kt`):

```kotlin
override fun formatWithOptions(amount: String, currencyCode: String, options: CurrencyFormatOptions): String {
    val nf = java.text.NumberFormat.getCurrencyInstance(kurrencyLocale.locale).apply {
        currency = java.util.Currency.getInstance(currencyCode)
        isGroupingUsed = options.grouping
        options.minFractionDigits?.let { minimumFractionDigits = it }
        options.maxFractionDigits?.let { maximumFractionDigits = it }
    }
    val value = amount.toDoubleOrNull() ?: return amount
    var result = nf.format(value)

    // Post-processing for options not expressible in native formatter
    val metadata = CurrencyMetadata.parse(currencyCode).getOrNull()

    // Symbol display
    when (options.symbolDisplay) {
        SymbolDisplay.NONE -> {
            if (metadata != null) result = result.replace(metadata.symbol, "").trim()
            result = result.replace(currencyCode, "").trim()
        }
        SymbolDisplay.ISO_CODE -> {
            if (metadata != null) result = result.replace(metadata.symbol, currencyCode)
        }
        SymbolDisplay.NAME -> {
            val name = if (kotlin.math.abs(value) == 1.0) metadata?.displayName ?: currencyCode
                       else metadata?.displayNamePlural ?: currencyCode
            if (metadata != null) result = result.replace(metadata.symbol, "").trim()
            result = "$result $name"
        }
        SymbolDisplay.SYMBOL -> {} // default, no change
    }

    // Negative style (parentheses) — applied as post-processing like existing Accounting style
    if (value < 0 && options.negativeStyle == NegativeStyle.PARENTHESES) {
        result = result.replace("-", "")
        result = "($result)"
    }

    return result
}
```

Implement similarly for Android (using ICU NumberFormat), iOS (using NSNumberFormatter), and JS/WasmJs (using Intl.NumberFormat). Each platform maps `options.grouping`, `options.minFractionDigits`, `options.maxFractionDigits` to native formatter properties. Post-processing for `SymbolDisplay`, `NegativeStyle.PARENTHESES`, `SymbolPosition` override, and `ZeroDisplay` is identical across platforms.

- [ ] **Step 4: Add formatWithOptions to CurrencyFormatter with ZeroDisplay handling**

```kotlin
fun formatWithOptions(
    amount: String,
    currencyCode: String,
    options: CurrencyFormatOptions
): Result<String> = runCatching {
    val normalizedAmount = amount.normalizeAmount()
    val numericValue = normalizedAmount.toDoubleOrNull()
        ?: throw KurrencyError.InvalidAmount(amount)

    // Handle zero display before delegating to formatter
    if (numericValue == 0.0) {
        when (options.zeroDisplay) {
            ZeroDisplay.DASH -> return@runCatching "—"
            ZeroDisplay.EMPTY -> return@runCatching ""
            ZeroDisplay.SHOW -> { /* fall through to normal formatting */ }
        }
    }

    formatter.formatWithOptions(normalizedAmount, currencyCode, options)
}
```

- [ ] **Step 5: Add formatAmountWithOptions to Kurrency**

```kotlin
fun formatAmountWithOptions(
    amount: String,
    options: CurrencyFormatOptions,
    locale: KurrencyLocale = KurrencyLocale.systemLocale()
): Result<String> {
    val formatter = CurrencyFormatter(locale)
    return formatter.formatWithOptions(amount, code, options)
}

fun formatAmountWithOptions(
    amount: Double,
    options: CurrencyFormatOptions,
    locale: KurrencyLocale = KurrencyLocale.systemLocale()
): Result<String> = formatAmountWithOptions(amount.toString(), options, locale)
```

- [ ] **Step 6: Add options overload to CurrencyAmount**

```kotlin
fun format(options: CurrencyFormatOptions, locale: KurrencyLocale): Result<String> {
    val majorUnits = minorUnits.toDouble() / kotlin.math.pow(10.0, currency.fractionDigitsOrDefault.toDouble())
    return currency.formatAmountWithOptions(majorUnits, options, locale)
}
```

- [ ] **Step 7: Run tests**

Run: `./gradlew :kurrency-core:allTests --tests "org.kimplify.kurrency.CurrencyFormatOptionsTest" -i`
Expected: All tests PASS

- [ ] **Step 8: Run all tests for regression**

Run: `./gradlew :kurrency-core:allTests -i`
Expected: All tests PASS

- [ ] **Step 9: Commit**

```bash
git add kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyFormatter.kt \
       kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/Kurrency.kt \
       kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyAmount.kt \
       kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyFormat.kt \
       kurrency-core/src/androidMain/kotlin/org/kimplify/kurrency/CurrencyFormatterImpl.kt \
       kurrency-core/src/iosMain/kotlin/org/kimplify/kurrency/CurrencyFormatterImpl.kt \
       kurrency-core/src/jvmMain/kotlin/org/kimplify/kurrency/CurrencyFormatterImpl.kt \
       kurrency-core/src/webMain/kotlin/org/kimplify/kurrency/CurrencyFormatterImpl.kt
git commit -m "feat: Implement formatWithOptions across Kurrency, CurrencyFormatter, CurrencyAmount"
```

---

## Sprint 5 — kotlinx.serialization in kurrency-core

### Task 5.1: Add Serialization Dependencies

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `kurrency-core/build.gradle.kts`

- [ ] **Step 1: Add serialization version to libs.versions.toml**

Add under `[versions]`:
```toml
kotlinx-serialization = "1.8.1"
```

Add under `[libraries]`:
```toml
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
kotlinx-serialization-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-core", version.ref = "kotlinx-serialization" }
```

Add under `[plugins]`:
```toml
kotlinSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

- [ ] **Step 2: Apply plugin and dependency to kurrency-core**

In `kurrency-core/build.gradle.kts`, add the serialization plugin and dependency:

```kotlin
plugins {
    // ... existing plugins ...
    alias(libs.plugins.kotlinSerialization)
}

// In commonMain dependencies:
implementation(libs.kotlinx.serialization.json)
```

> **Note:** Do NOT add `kotlinx-serialization-json` to `commonTest` — it's automatically available from `commonMain`. Also remove the `kotlinx-serialization-core` line from `libs.versions.toml` additions (it's a transitive dependency of `json`).

- [ ] **Step 3: Verify build**

Run: `./gradlew :kurrency-core:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml kurrency-core/build.gradle.kts
git commit -m "build: Add kotlinx-serialization dependency to kurrency-core"
```

### Task 5.2: Add InvalidLocale Error Type

**Files:**
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/KurrencyError.kt`

- [ ] **Step 1: Read current KurrencyError**

Read: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/KurrencyError.kt`

- [ ] **Step 2: Add InvalidLocale subclass**

```kotlin
class InvalidLocale(val languageTag: String) : KurrencyError("Invalid locale: $languageTag")
```

- [ ] **Step 3: Update fromLanguageTag on all platforms to return InvalidLocale**

Update each platform's `KurrencyLocale.fromLanguageTag()` to wrap failures in `KurrencyError.InvalidLocale` instead of `IllegalArgumentException`.

- [ ] **Step 4: Run all tests**

Run: `./gradlew :kurrency-core:allTests -i`
Expected: All tests PASS

- [ ] **Step 5: Commit**

```bash
git add kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/KurrencyError.kt \
       kurrency-core/src/androidMain/kotlin/org/kimplify/kurrency/KurrencyLocale.kt \
       kurrency-core/src/iosMain/kotlin/org/kimplify/kurrency/KurrencyLocale.kt \
       kurrency-core/src/jvmMain/kotlin/org/kimplify/kurrency/KurrencyLocale.kt \
       kurrency-core/src/webMain/kotlin/org/kimplify/kurrency/KurrencyLocale.web.kt
git commit -m "feat: Add KurrencyError.InvalidLocale, update fromLanguageTag error type"
```

### Task 5.3: Add @Serializable to CurrencyFormatOptions Enums

**Files:**
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyFormatOptions.kt`

- [ ] **Step 1: Add @Serializable annotations**

Add `@Serializable` to: `CurrencyFormatOptions`, `SymbolPosition`, `NegativeStyle`, `SymbolDisplay`, `ZeroDisplay`. (Do NOT annotate `NumeralSystem` — it's not part of `CurrencyFormatOptions` and isn't specified for serialization.)

- [ ] **Step 2: Verify compilation**

Run: `./gradlew :kurrency-core:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyFormatOptions.kt
git commit -m "feat: Add @Serializable to CurrencyFormatOptions and supporting enums"
```

### Task 5.4: Implement Custom Serializers

**Files:**
- Create: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/serialization/KurrencySerializer.kt`
- Create: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/serialization/CurrencyAmountSerializer.kt`
- Create: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/serialization/KurrencyLocaleSerializer.kt`
- Create: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/serialization/KurrencyJson.kt`

- [ ] **Step 1: Create KurrencySerializer**

```kotlin
package org.kimplify.kurrency.serialization

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import org.kimplify.kurrency.Kurrency

object KurrencySerializer : KSerializer<Kurrency> {
    override val descriptor = PrimitiveSerialDescriptor("Kurrency", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Kurrency) {
        encoder.encodeString(value.code)
    }

    override fun deserialize(decoder: Decoder): Kurrency {
        val code = decoder.decodeString()
        return Kurrency.fromCode(code).getOrElse {
            throw SerializationException("Invalid currency code: $code", it)
        }
    }
}
```

- [ ] **Step 2: Create CurrencyAmountSerializer**

```kotlin
package org.kimplify.kurrency.serialization

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import org.kimplify.kurrency.CurrencyAmount
import org.kimplify.kurrency.Kurrency

object CurrencyAmountSerializer : KSerializer<CurrencyAmount> {
    override val descriptor = buildClassSerialDescriptor("CurrencyAmount") {
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
            val currency = Kurrency.fromCode(currencyCode ?: throw SerializationException("Missing currency"))
                .getOrElse { throw SerializationException("Invalid currency: $currencyCode", it) }
            CurrencyAmount.of(
                minorUnits ?: throw SerializationException("Missing minorUnits"),
                currency
            )
        }
    }
}
```

- [ ] **Step 3: Create KurrencyLocaleSerializer**

```kotlin
package org.kimplify.kurrency.serialization

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import org.kimplify.kurrency.KurrencyLocale

object KurrencyLocaleSerializer : KSerializer<KurrencyLocale> {
    override val descriptor = PrimitiveSerialDescriptor("KurrencyLocale", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: KurrencyLocale) {
        encoder.encodeString(value.languageTag)
    }

    override fun deserialize(decoder: Decoder): KurrencyLocale {
        val tag = decoder.decodeString()
        return KurrencyLocale.fromLanguageTag(tag).getOrElse {
            throw SerializationException("Invalid locale tag: $tag", it)
        }
    }
}
```

- [ ] **Step 4: Create KurrencyJson configuration**

```kotlin
package org.kimplify.kurrency.serialization

import kotlinx.serialization.json.Json

/**
 * Pre-configured Json instance for Kurrency types.
 * - Omits null fields and fields matching their default values
 * - Ignores unknown keys for forward compatibility
 */
val KurrencyJson = Json {
    explicitNulls = false
    encodeDefaults = false
    ignoreUnknownKeys = true
}
```

- [ ] **Step 5: Commit**

```bash
git add kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/serialization/
git commit -m "feat: Add KurrencySerializer, CurrencyAmountSerializer, KurrencyLocaleSerializer, KurrencyJson"
```

### Task 5.5: Write Serialization Tests

**Files:**
- Create: `kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/serialization/KurrencySerializerTest.kt`
- Create: `kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/serialization/CurrencyAmountSerializerTest.kt`
- Create: `kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/serialization/KurrencyLocaleSerializerTest.kt`
- Create: `kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/serialization/CurrencyFormatOptionsSerializerTest.kt`

- [ ] **Step 1: Write KurrencySerializerTest**

```kotlin
package org.kimplify.kurrency.serialization

import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import org.kimplify.kurrency.Kurrency
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.serialization.SerializationException

class KurrencySerializerTest {

    @Test
    fun roundTrip_usd() {
        val usd = Kurrency.fromCode("USD").getOrThrow()
        val json = KurrencyJson.encodeToString(KurrencySerializer, usd)
        assertEquals("\"USD\"", json)
        val decoded = KurrencyJson.decodeFromString(KurrencySerializer, json)
        assertEquals(usd.code, decoded.code)
    }

    @Test
    fun roundTrip_jpy() {
        val jpy = Kurrency.fromCode("JPY").getOrThrow()
        val json = KurrencyJson.encodeToString(KurrencySerializer, jpy)
        val decoded = KurrencyJson.decodeFromString(KurrencySerializer, json)
        assertEquals("JPY", decoded.code)
    }

    @Test
    fun invalidCode_throwsSerializationException() {
        assertFailsWith<SerializationException> {
            KurrencyJson.decodeFromString(KurrencySerializer, "\"INVALID\"")
        }
    }
}
```

- [ ] **Step 2: Write CurrencyAmountSerializerTest**

```kotlin
package org.kimplify.kurrency.serialization

import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import org.kimplify.kurrency.CurrencyAmount
import org.kimplify.kurrency.Kurrency
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CurrencyAmountSerializerTest {

    @Test
    fun roundTrip_usdAmount() {
        val usd = Kurrency.fromCode("USD").getOrThrow()
        val amount = CurrencyAmount.of(123456L, usd)
        val json = KurrencyJson.encodeToString(CurrencyAmountSerializer, amount)
        assertTrue(json.contains("\"minorUnits\":123456"))
        assertTrue(json.contains("\"currency\":\"USD\""))
        val decoded = KurrencyJson.decodeFromString(CurrencyAmountSerializer, json)
        assertEquals(123456L, decoded.minorUnits)
        assertEquals("USD", decoded.currency.code)
    }

    @Test
    fun minorUnits_notFloat_inJson() {
        val usd = Kurrency.fromCode("USD").getOrThrow()
        val amount = CurrencyAmount.of(123456L, usd)
        val json = KurrencyJson.encodeToString(CurrencyAmountSerializer, amount)
        assertTrue(!json.contains("1234.56"), "JSON should use minor units, not major: $json")
    }
}
```

- [ ] **Step 3: Write KurrencyLocaleSerializerTest**

```kotlin
package org.kimplify.kurrency.serialization

import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import org.kimplify.kurrency.KurrencyLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.serialization.SerializationException

class KurrencyLocaleSerializerTest {

    @Test
    fun roundTrip_usLocale() {
        val locale = KurrencyLocale.US
        val json = KurrencyJson.encodeToString(KurrencyLocaleSerializer, locale)
        assertEquals("\"en-US\"", json)
        val decoded = KurrencyJson.decodeFromString(KurrencyLocaleSerializer, json)
        assertEquals("en-US", decoded.languageTag)
    }

    @Test
    fun roundTrip_germanyLocale() {
        val locale = KurrencyLocale.GERMANY
        val json = KurrencyJson.encodeToString(KurrencyLocaleSerializer, locale)
        val decoded = KurrencyJson.decodeFromString(KurrencyLocaleSerializer, json)
        assertEquals(locale.languageTag, decoded.languageTag)
    }
}
```

- [ ] **Step 4: Write CurrencyFormatOptionsSerializerTest**

```kotlin
package org.kimplify.kurrency.serialization

import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import org.kimplify.kurrency.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CurrencyFormatOptionsSerializerTest {

    @Test
    fun defaultOptions_serializesToEmptyObject() {
        val options = CurrencyFormatOptions()
        val json = KurrencyJson.encodeToString(options)
        assertEquals("{}", json)
    }

    @Test
    fun customOptions_roundTrip() {
        val options = CurrencyFormatOptions(
            symbolPosition = SymbolPosition.TRAILING,
            grouping = false,
            minFractionDigits = 2,
            maxFractionDigits = 4,
            negativeStyle = NegativeStyle.PARENTHESES,
            symbolDisplay = SymbolDisplay.ISO_CODE,
            zeroDisplay = ZeroDisplay.DASH
        )
        val json = KurrencyJson.encodeToString(options)
        val decoded = KurrencyJson.decodeFromString<CurrencyFormatOptions>(json)
        assertEquals(options, decoded)
    }

    @Test
    fun missingOptionalFields_usesDefaults() {
        val json = """{"grouping":false}"""
        val decoded = KurrencyJson.decodeFromString<CurrencyFormatOptions>(json)
        assertEquals(false, decoded.grouping)
        assertEquals(SymbolPosition.LOCALE_DEFAULT, decoded.symbolPosition)
        assertEquals(ZeroDisplay.SHOW, decoded.zeroDisplay)
    }
}
```

- [ ] **Step 5: Run all serialization tests**

Run: `./gradlew :kurrency-core:allTests --tests "org.kimplify.kurrency.serialization.*" -i`
Expected: All tests PASS

- [ ] **Step 6: Commit**

```bash
git add kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/serialization/
git commit -m "test: Add comprehensive serialization tests for all Kurrency types"
```

---

## Sprint 6 — Currency Conversion & Range Formatting

### Task 6.1: Add ConversionFailure Error Type

**Files:**
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/KurrencyError.kt`

- [ ] **Step 1: Add ConversionFailure to KurrencyError**

```kotlin
class ConversionFailure(
    val from: String,
    val to: String,
    override val cause: Throwable? = null
) : KurrencyError("Conversion failed from $from to $to")
```

- [ ] **Step 2: Commit**

```bash
git add kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/KurrencyError.kt
git commit -m "feat: Add KurrencyError.ConversionFailure"
```

### Task 6.2: Create RateProvider Interface

**Files:**
- Create: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/RateProvider.kt`

- [ ] **Step 1: Create RateProvider**

```kotlin
package org.kimplify.kurrency

/**
 * Provides exchange rates between currencies.
 * Implement this interface to plug in your own rate source.
 */
fun interface RateProvider {
    suspend fun getRate(from: Kurrency, to: Kurrency): Result<Double>
}
```

- [ ] **Step 2: Commit**

```bash
git add kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/RateProvider.kt
git commit -m "feat: Add RateProvider fun interface"
```

### Task 6.3: Implement CurrencyConverter (TDD)

**Files:**
- Create: `kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/CurrencyConverterTest.kt`
- Create: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyConverter.kt`

- [ ] **Step 1: Write converter tests**

```kotlin
package org.kimplify.kurrency

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CurrencyConverterTest {

    private val usd = Kurrency.fromCode("USD").getOrThrow()
    private val eur = Kurrency.fromCode("EUR").getOrThrow()
    private val jpy = Kurrency.fromCode("JPY").getOrThrow()
    private val kwd = Kurrency.fromCode("KWD").getOrThrow()

    // -- Pure conversion --

    @Test
    fun convert_usdToEur_knownRate() {
        val result = CurrencyConverter.convert(100.0, usd, eur, 0.85)
        assertTrue(result.isSuccess)
        val amount = result.getOrThrow()
        assertEquals("EUR", amount.currency.code)
        assertEquals(8500L, amount.minorUnits) // 85.00 EUR = 8500 minor units
    }

    @Test
    fun convert_usdToJpy_zeroFractionDigits() {
        val result = CurrencyConverter.convert(100.0, usd, jpy, 150.456)
        assertTrue(result.isSuccess)
        // JPY has 0 fraction digits: 15045.6 rounds to 15046 (HALF_EVEN)
        assertEquals(15046L, result.getOrThrow().minorUnits)
    }

    @Test
    fun convert_usdToKwd_threeFractionDigits() {
        val result = CurrencyConverter.convert(100.0, usd, kwd, 0.30712)
        assertTrue(result.isSuccess)
        assertEquals(30712L, result.getOrThrow().minorUnits)
    }

    @Test
    fun convert_halfEven_rounding() {
        // 100.0 * 0.307125 = 30.7125, HALF_EVEN rounds to 30.712 (even digit)
        val result = CurrencyConverter.convert(100.0, usd, kwd, 0.307125)
        assertTrue(result.isSuccess)
        assertEquals(30712L, result.getOrThrow().minorUnits)
    }

    @Test
    fun convert_zeroAmount() {
        val result = CurrencyConverter.convert(0.0, usd, eur, 0.85)
        assertTrue(result.isSuccess)
        assertEquals(0L, result.getOrThrow().minorUnits)
    }

    @Test
    fun convert_negativeAmount() {
        val result = CurrencyConverter.convert(-100.0, usd, eur, 0.85)
        assertTrue(result.isSuccess)
        assertEquals(-8500L, result.getOrThrow().minorUnits)
    }

    @Test
    fun convert_invalidRate_zero() {
        val result = CurrencyConverter.convert(100.0, usd, eur, 0.0)
        assertTrue(result.isFailure)
    }

    @Test
    fun convert_invalidRate_negative() {
        val result = CurrencyConverter.convert(100.0, usd, eur, -1.0)
        assertTrue(result.isFailure)
    }

    @Test
    fun convert_invalidRate_nan() {
        val result = CurrencyConverter.convert(100.0, usd, eur, Double.NaN)
        assertTrue(result.isFailure)
    }

    @Test
    fun convert_invalidRate_infinity() {
        val result = CurrencyConverter.convert(100.0, usd, eur, Double.POSITIVE_INFINITY)
        assertTrue(result.isFailure)
    }

    // -- Provider-based conversion --

    @Test
    fun convert_withProvider() = runTest {
        val provider = RateProvider { _, _ -> Result.success(0.85) }
        val result = CurrencyConverter.convert(100.0, usd, eur, provider)
        assertTrue(result.isSuccess)
        assertEquals(8500L, result.getOrThrow().minorUnits)
    }

    @Test
    fun convert_providerFailure_propagates() = runTest {
        val provider = RateProvider { _, _ ->
            Result.failure(RuntimeException("Network error"))
        }
        val result = CurrencyConverter.convert(100.0, usd, eur, provider)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is KurrencyError.ConversionFailure)
    }

    // -- CurrencyAmount convenience --

    @Test
    fun convert_currencyAmount() = runTest {
        val amount = CurrencyAmount.of(10000L, usd) // $100.00
        val provider = RateProvider { _, _ -> Result.success(0.85) }
        val result = CurrencyConverter.convert(amount, eur, provider)
        assertTrue(result.isSuccess)
        assertEquals(8500L, result.getOrThrow().minorUnits)
    }
}
```

- [ ] **Step 2: Run tests — they should FAIL**

Run: `./gradlew :kurrency-core:allTests --tests "org.kimplify.kurrency.CurrencyConverterTest" -i`
Expected: FAIL

- [ ] **Step 3: Implement CurrencyConverter**

```kotlin
package org.kimplify.kurrency

import kotlin.math.pow
import kotlin.math.roundToLong

object CurrencyConverter {

    fun convert(
        amount: Double,
        from: Kurrency,
        to: Kurrency,
        rate: Double
    ): Result<CurrencyAmount> = runCatching {
        validateRate(rate, from.code, to.code)
        val converted = amount * rate
        val fractionDigits = to.fractionDigitsOrDefault
        val minorUnits = toMinorUnitsHalfEven(converted, fractionDigits)
        CurrencyAmount.of(minorUnits, to)
    }

    suspend fun convert(
        amount: Double,
        from: Kurrency,
        to: Kurrency,
        provider: RateProvider
    ): Result<CurrencyAmount> {
        val rateResult = provider.getRate(from, to)
        val rate = rateResult.getOrElse { cause ->
            return Result.failure(KurrencyError.ConversionFailure(from.code, to.code, cause))
        }
        return convert(amount, from, to, rate)
    }

    suspend fun convert(
        amount: CurrencyAmount,
        to: Kurrency,
        provider: RateProvider
    ): Result<CurrencyAmount> {
        val from = amount.currency
        val majorUnits = amount.minorUnits.toDouble() / 10.0.pow(from.fractionDigitsOrDefault)
        return convert(majorUnits, from, to, provider)
    }

    private fun validateRate(rate: Double, from: String, to: String) {
        if (rate <= 0 || !rate.isFinite()) {
            throw KurrencyError.ConversionFailure(from, to)
        }
    }

    private fun toMinorUnitsHalfEven(majorUnits: Double, fractionDigits: Int): Long {
        // Use BigDecimal-style string arithmetic for precision
        val multiplier = 10.0.pow(fractionDigits)
        val scaled = majorUnits * multiplier
        // HALF_EVEN: round to nearest, ties to even
        return halfEvenRound(scaled)
    }

    private fun halfEvenRound(value: Double): Long {
        val rounded = value.roundToLong()
        val diff = value - rounded
        // Check if exactly at 0.5 boundary
        if (kotlin.math.abs(kotlin.math.abs(diff) - 0.5) < 1e-10) {
            // Tie — round to even
            return if (rounded % 2 == 0L) rounded else rounded - if (value > 0) 1 else -1
        }
        return rounded
    }
}
```

- [ ] **Step 4: Run tests**

Run: `./gradlew :kurrency-core:allTests --tests "org.kimplify.kurrency.CurrencyConverterTest" -i`
Expected: All tests PASS

- [ ] **Step 5: Commit**

```bash
git add kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyConverter.kt \
       kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/CurrencyConverterTest.kt
git commit -m "feat: Implement CurrencyConverter with HALF_EVEN rounding and RateProvider support"
```

### Task 6.4: Implement Range Formatting (TDD)

**Files:**
- Create: `kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/RangeFormattingTest.kt`
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyFormatter.kt`
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/Kurrency.kt`

- [ ] **Step 1: Write range formatting tests**

```kotlin
package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class RangeFormattingTest {

    @Test
    fun formatRange_usd_usLocale() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatRange("10", "50", "USD")
        assertTrue(result.isSuccess)
        val formatted = result.getOrThrow()
        assertTrue(formatted.contains("10"), "Should contain min: $formatted")
        assertTrue(formatted.contains("50"), "Should contain max: $formatted")
        assertTrue(formatted.contains("–") || formatted.contains("-"), "Should contain separator: $formatted")
    }

    @Test
    fun formatRange_minEqualsMax_valid() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatRange("10", "10", "USD")
        assertTrue(result.isSuccess)
    }

    @Test
    fun formatRange_minGreaterThanMax_returnsError() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatRange("50", "10", "USD")
        assertTrue(result.isFailure)
    }

    @Test
    fun formatRange_jpy_noDecimals() {
        val formatter = CurrencyFormatter(KurrencyLocale.JAPAN)
        val result = formatter.formatRange("1000", "5000", "JPY")
        assertTrue(result.isSuccess)
        val formatted = result.getOrThrow()
        assertTrue(formatted.contains("1,000") || formatted.contains("1000"))
        assertTrue(formatted.contains("5,000") || formatted.contains("5000"))
    }

    @Test
    fun formatRange_onKurrency() {
        val usd = Kurrency.fromCode("USD").getOrThrow()
        val result = usd.formatRange("10", "50", KurrencyLocale.US)
        assertTrue(result.isSuccess)
    }
}
```

- [ ] **Step 2: Implement formatRange**

In `CurrencyFormatter`:

```kotlin
fun formatRange(
    min: String,
    max: String,
    currencyCode: String,
    locale: KurrencyLocale = this.locale
): Result<String> = runCatching {
    val minNorm = min.normalizeAmount()
    val maxNorm = max.normalizeAmount()
    val minVal = minNorm.toDoubleOrNull() ?: throw KurrencyError.InvalidAmount(min)
    val maxVal = maxNorm.toDoubleOrNull() ?: throw KurrencyError.InvalidAmount(max)
    if (minVal > maxVal) throw KurrencyError.InvalidAmount("min ($min) > max ($max)")

    val formattedMin = formatCurrencyStyle(minNorm, currencyCode)
    val formattedMax = formatCurrencyStyle(maxNorm, currencyCode)

    "$formattedMin \u2013 $formattedMax"
}
```

In `Kurrency`:

```kotlin
fun formatRange(min: String, max: String, locale: KurrencyLocale): Result<String> {
    val formatter = CurrencyFormatter(locale)
    return formatter.formatRange(min, max, code)
}
```

- [ ] **Step 3: Run tests**

Run: `./gradlew :kurrency-core:allTests --tests "org.kimplify.kurrency.RangeFormattingTest" -i`
Expected: All tests PASS

- [ ] **Step 4: Commit**

```bash
git add kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/RangeFormattingTest.kt \
       kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyFormatter.kt \
       kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/Kurrency.kt
git commit -m "feat: Add range formatting to CurrencyFormatter and Kurrency"
```

---

## Sprint 7 — Accessibility & Plural Currency Names

### Task 7.1: Add Sub-Unit Names to CurrencyMetadata

**Files:**
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyMetadata.kt`

- [ ] **Step 1: Read current CurrencyMetadata**

Read: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyMetadata.kt`

- [ ] **Step 2: Add subUnitName and subUnitNamePlural**

Add two new constructor parameters to the enum and update every entry. Examples:

```kotlin
USD("USD", "US Dollar", "US Dollars", "$", "US", "🇺🇸", 2, "cent", "cents"),
EUR("EUR", "Euro", "Euros", "€", "EU", "🇪🇺", 2, "cent", "cents"),
GBP("GBP", "British Pound", "British Pounds", "£", "GB", "🇬🇧", 2, "penny", "pence"),
JPY("JPY", "Japanese Yen", "Japanese Yen", "¥", "JP", "🇯🇵", 0, "", ""),
BHD("BHD", "Bahraini Dinar", "Bahraini Dinars", "BD", "BH", "🇧🇭", 3, "fils", "fils"),
// ... etc for all 47+ entries
```

- [ ] **Step 3: Run existing metadata tests**

Run: `./gradlew :kurrency-core:allTests --tests "org.kimplify.kurrency.CurrencyMetadataTest" -i`
Expected: PASS (may need to update test assertions for new constructor params)

- [ ] **Step 4: Commit**

```bash
git add kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyMetadata.kt
git commit -m "feat: Add subUnitName and subUnitNamePlural to CurrencyMetadata"
```

### Task 7.2: Implement NumberToWords Engine

**Files:**
- Create: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/NumberToWords.kt`

- [ ] **Step 1: Implement pure Kotlin number-to-words for English**

```kotlin
package org.kimplify.kurrency

/**
 * Converts numbers to English words.
 * Pure Kotlin implementation — no platform dependencies.
 */
internal object NumberToWords {

    private val ones = arrayOf(
        "", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
        "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen",
        "seventeen", "eighteen", "nineteen"
    )

    private val tens = arrayOf(
        "", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"
    )

    private val thousands = arrayOf("", "thousand", "million", "billion", "trillion")

    fun toWords(value: Long): String {
        if (value == 0L) return "zero"

        val isNegative = value < 0
        var num = if (isNegative) -value else value
        val parts = mutableListOf<String>()
        var groupIndex = 0

        while (num > 0) {
            val group = (num % 1000).toInt()
            if (group != 0) {
                val groupWords = convertGroup(group)
                val suffix = thousands[groupIndex]
                parts.add(0, if (suffix.isNotEmpty()) "$groupWords $suffix" else groupWords)
            }
            num /= 1000
            groupIndex++
        }

        val result = parts.joinToString(" ")
        return if (isNegative) "negative $result" else result
    }

    private fun convertGroup(num: Int): String {
        val parts = mutableListOf<String>()

        if (num >= 100) {
            parts.add("${ones[num / 100]} hundred")
        }

        val remainder = num % 100
        if (remainder >= 20) {
            val tenPart = tens[remainder / 10]
            val onePart = ones[remainder % 10]
            parts.add(if (onePart.isNotEmpty()) "$tenPart-$onePart" else tenPart)
        } else if (remainder > 0) {
            parts.add(ones[remainder])
        }

        return parts.joinToString(" ")
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/NumberToWords.kt
git commit -m "feat: Add NumberToWords pure Kotlin engine for English"
```

### Task 7.3: Implement formatSpoken and formatWithName (TDD)

**Files:**
- Create: `kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/SpokenFormattingTest.kt`
- Modify: `kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyFormatter.kt`

- [ ] **Step 1: Write spoken formatting tests**

```kotlin
package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SpokenFormattingTest {

    private val formatter = CurrencyFormatter(KurrencyLocale.US)

    @Test
    fun formatSpoken_usd_standard() {
        val result = formatter.formatSpoken("1234.56", "USD")
        assertTrue(result.isSuccess)
        assertEquals(
            "one thousand two hundred thirty-four US dollars and fifty-six cents",
            result.getOrThrow()
        )
    }

    @Test
    fun formatSpoken_usd_one() {
        val result = formatter.formatSpoken("1.00", "USD")
        assertTrue(result.isSuccess)
        assertEquals("one US dollar and zero cents", result.getOrThrow())
    }

    @Test
    fun formatSpoken_gbp() {
        val result = formatter.formatSpoken("0.50", "GBP")
        assertTrue(result.isSuccess)
        assertEquals("zero British pounds and fifty pence", result.getOrThrow())
    }

    @Test
    fun formatSpoken_jpy_noSubUnits() {
        val result = formatter.formatSpoken("1000", "JPY")
        assertTrue(result.isSuccess)
        assertEquals("one thousand Japanese yen", result.getOrThrow())
    }

    @Test
    fun formatSpoken_negative() {
        val result = formatter.formatSpoken("-42.10", "USD")
        assertTrue(result.isSuccess)
        assertEquals(
            "negative forty-two US dollars and ten cents",
            result.getOrThrow()
        )
    }

    @Test
    fun formatSpoken_zero() {
        val result = formatter.formatSpoken("0.00", "USD")
        assertTrue(result.isSuccess)
        assertEquals("zero US dollars and zero cents", result.getOrThrow())
    }

    // -- formatWithName --

    @Test
    fun formatWithName_plural() {
        val result = formatter.formatWithName("1234.56", "USD")
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().contains("US Dollars"))
    }

    @Test
    fun formatWithName_singular() {
        val result = formatter.formatWithName("1.00", "USD")
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().contains("US Dollar"))
        assertTrue(!result.getOrThrow().contains("Dollars"))
    }
}
```

- [ ] **Step 2: Implement formatSpoken and formatWithName in CurrencyFormatter**

```kotlin
fun formatSpoken(
    amount: String,
    currencyCode: String,
    locale: KurrencyLocale = this.locale
): Result<String> = runCatching {
    val normalized = amount.normalizeAmount()
    val value = normalized.toDoubleOrNull() ?: throw KurrencyError.InvalidAmount(amount)
    val metadata = CurrencyMetadata.parse(currencyCode).getOrElse {
        throw KurrencyError.InvalidCurrencyCode(currencyCode)
    }

    val isNegative = value < 0
    val absValue = kotlin.math.abs(value)
    val fractionDigits = metadata.fractionDigits
    val majorPart = absValue.toLong()
    val minorPart = ((absValue - majorPart) * kotlin.math.pow(10.0, fractionDigits.toDouble())).roundToLong()

    val majorWords = NumberToWords.toWords(majorPart)
    val currencyName = if (majorPart == 1L) metadata.displayName else metadata.displayNamePlural

    val result = StringBuilder()
    if (isNegative) result.append("negative ")
    result.append("$majorWords $currencyName")

    if (fractionDigits > 0 && metadata.subUnitName.isNotEmpty()) {
        val minorWords = NumberToWords.toWords(minorPart)
        val subUnitName = if (minorPart == 1L) metadata.subUnitName else metadata.subUnitNamePlural
        result.append(" and $minorWords $subUnitName")
    }

    result.toString()
}

fun formatWithName(
    amount: String,
    currencyCode: String,
    locale: KurrencyLocale = this.locale
): Result<String> = runCatching {
    val normalized = amount.normalizeAmount()
    val value = normalized.toDoubleOrNull() ?: throw KurrencyError.InvalidAmount(amount)
    val metadata = CurrencyMetadata.parse(currencyCode).getOrElse {
        throw KurrencyError.InvalidCurrencyCode(currencyCode)
    }

    val formatted = formatCurrencyStyle(normalized, currencyCode)
    // Replace symbol with name
    val name = if (kotlin.math.abs(value) == 1.0) metadata.displayName else metadata.displayNamePlural
    val symbolStripped = formatted
        .replace(metadata.symbol, "")
        .replace(currencyCode, "")
        .trim()
    "$symbolStripped $name"
}
```

- [ ] **Step 3: Run tests**

Run: `./gradlew :kurrency-core:allTests --tests "org.kimplify.kurrency.SpokenFormattingTest" -i`
Expected: All tests PASS

- [ ] **Step 4: Commit**

```bash
git add kurrency-core/src/commonTest/kotlin/org/kimplify/kurrency/SpokenFormattingTest.kt \
       kurrency-core/src/commonMain/kotlin/org/kimplify/kurrency/CurrencyFormatter.kt
git commit -m "feat: Implement formatSpoken and formatWithName with NumberToWords engine"
```

### Task 7.4: Compose Accessibility Helpers

**Files:**
- Create: `kurrency-compose/src/commonMain/kotlin/org/kimplify/kurrency/compose/CurrencyAccessibility.kt`
- Create: `kurrency-compose/src/commonMain/kotlin/org/kimplify/kurrency/compose/CurrencyText.kt`

- [ ] **Step 1: Create CurrencyAccessibility (semantics modifier)**

```kotlin
package org.kimplify.kurrency.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import org.kimplify.kurrency.CurrencyFormatter
import org.kimplify.kurrency.KurrencyLocale

@Composable
fun Modifier.currencySemantics(
    amount: String,
    currencyCode: String,
    locale: KurrencyLocale = KurrencyLocale.current()
): Modifier {
    val formatter = CurrencyFormatter(locale)
    val spoken = formatter.formatSpoken(amount, currencyCode).getOrElse { amount }
    return this.semantics { contentDescription = spoken }
}
```

- [ ] **Step 2: Create CurrencyText composable**

```kotlin
package org.kimplify.kurrency.compose

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import org.kimplify.kurrency.CurrencyFormatter
import org.kimplify.kurrency.CurrencyStyle
import org.kimplify.kurrency.KurrencyLocale

/**
 * Displays a formatted currency amount with accessibility (spoken) content description.
 * Uses BasicText (not material3.Text) to avoid forcing a material3 dependency on consumers.
 */
@Composable
fun CurrencyText(
    amount: String,
    currencyCode: String,
    modifier: Modifier = Modifier,
    locale: KurrencyLocale = KurrencyLocale.current(),
    style: TextStyle = TextStyle.Default,
    currencyStyle: CurrencyStyle = CurrencyStyle.Standard
) {
    val formatter = CurrencyFormatter(locale)
    val displayText = when (currencyStyle) {
        CurrencyStyle.Standard -> formatter.formatCurrencyStyle(amount, currencyCode)
        CurrencyStyle.Iso -> formatter.formatIsoCurrencyStyle(amount, currencyCode)
        CurrencyStyle.Accounting -> formatter.formatCurrencyStyle(amount, currencyCode)
    }

    BasicText(
        text = displayText,
        modifier = modifier.currencySemantics(amount, currencyCode, locale),
        style = style
    )
}
```

- [ ] **Step 3: Verify compilation**

Run: `./gradlew :kurrency-compose:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add kurrency-compose/src/commonMain/kotlin/org/kimplify/kurrency/compose/CurrencyAccessibility.kt \
       kurrency-compose/src/commonMain/kotlin/org/kimplify/kurrency/compose/CurrencyText.kt
git commit -m "feat: Add CurrencyText composable and currencySemantics modifier"
```

---

## Sprint 8 — Performance Benchmarks & Golden Tests

### Task 8.1: Golden Tests for CurrencyVisualTransformation

**Files:**
- Create: `kurrency-compose/src/commonTest/kotlin/org/kimplify/kurrency/compose/golden/VisualTransformationGoldenTest.kt`

- [ ] **Step 1: Write golden tests**

```kotlin
package org.kimplify.kurrency.compose.golden

import org.kimplify.kurrency.CurrencyFormatter
import org.kimplify.kurrency.KurrencyLocale
import org.kimplify.kurrency.compose.CurrencyVisualTransformation
import androidx.compose.ui.text.AnnotatedString
import kotlin.test.Test
import kotlin.test.assertEquals

class VisualTransformationGoldenTest {

    // -- USD / en-US --

    @Test
    fun golden_usd_1234_usLocale() {
        val transformation = CurrencyVisualTransformation("USD", KurrencyLocale.US)
        val result = transformation.filter(AnnotatedString("1234"))
        assertEquals("$1,234", result.text.text)
    }

    @Test
    fun golden_usd_1234_56_usLocale() {
        val transformation = CurrencyVisualTransformation("USD", KurrencyLocale.US)
        val result = transformation.filter(AnnotatedString("1234.56"))
        assertEquals("$1,234.56", result.text.text)
    }

    @Test
    fun golden_usd_zero_usLocale() {
        val transformation = CurrencyVisualTransformation("USD", KurrencyLocale.US)
        val result = transformation.filter(AnnotatedString("0"))
        val text = result.text.text
        // Accept either "$0" or "$0.00" depending on implementation
        assertEquals(true, text.startsWith("$") && text.contains("0"))
    }

    // -- EUR / de-DE --

    @Test
    fun golden_eur_1234_56_germanyLocale() {
        val transformation = CurrencyVisualTransformation("EUR", KurrencyLocale.GERMANY)
        val result = transformation.filter(AnnotatedString("1234.56"))
        val text = result.text.text
        // German format uses comma as decimal, dot as grouping
        assertEquals(true, text.contains("1.234,56") || text.contains("1234,56"))
    }

    // -- JPY / ja-JP --

    @Test
    fun golden_jpy_1000_japanLocale() {
        val transformation = CurrencyVisualTransformation("JPY", KurrencyLocale.JAPAN)
        val result = transformation.filter(AnnotatedString("1000"))
        val text = result.text.text
        assertEquals(true, text.contains("1,000") || text.contains("1000"))
    }

    // -- Offset mapping monotonicity --

    @Test
    fun offsetMapping_monotonicallyIncreasing_usd() {
        val transformation = CurrencyVisualTransformation("USD", KurrencyLocale.US)
        val result = transformation.filter(AnnotatedString("1234567"))
        val mapping = result.offsetMapping
        var prev = -1
        for (i in 0..7) {
            val mapped = mapping.originalToTransformed(i)
            assertEquals(true, mapped >= prev, "Offset at $i ($mapped) should be >= prev ($prev)")
            prev = mapped
        }
    }

    @Test
    fun offsetMapping_monotonicallyIncreasing_eur_de() {
        val transformation = CurrencyVisualTransformation("EUR", KurrencyLocale.GERMANY)
        val result = transformation.filter(AnnotatedString("1234567"))
        val mapping = result.offsetMapping
        var prev = -1
        for (i in 0..7) {
            val mapped = mapping.originalToTransformed(i)
            assertEquals(true, mapped >= prev, "Offset at $i ($mapped) should be >= prev ($prev)")
            prev = mapped
        }
    }
}
```

- [ ] **Step 2: Run golden tests**

Run: `./gradlew :kurrency-compose:allTests --tests "org.kimplify.kurrency.compose.golden.*" -i`
Expected: All PASS (these lock down current behavior)

- [ ] **Step 3: Commit**

```bash
git add kurrency-compose/src/commonTest/kotlin/org/kimplify/kurrency/compose/golden/VisualTransformationGoldenTest.kt
git commit -m "test: Add golden tests for CurrencyVisualTransformation output and offset mapping"
```

### Task 8.2: Performance Benchmarks Setup

**Files:**
- Modify: `gradle/libs.versions.toml` (add kotlinx-benchmark)
- Modify: `kurrency-core/build.gradle.kts` (add benchmark configuration)

- [ ] **Step 1: Add benchmark dependency**

In `libs.versions.toml`:
```toml
kotlinx-benchmark = "0.4.13"
```

```toml
kotlinx-benchmark = { group = "org.jetbrains.kotlinx", name = "kotlinx-benchmark-runtime", version.ref = "kotlinx-benchmark" }
```

- [ ] **Step 2: Configure benchmarks in build.gradle.kts**

Add the `kotlinx-benchmark` plugin and configure a JVM benchmark source set. The benchmark configuration depends on the kotlinx-benchmark setup for KMP — follow the library's official setup guide. At minimum, JVM benchmarks should work.

- [ ] **Step 3: Create initial benchmark file**

Create: `kurrency-core/src/jvmMain/kotlin/org/kimplify/kurrency/benchmark/FormattingBenchmark.kt`

> **Important:** Use `kotlinx.benchmark` annotations, NOT `org.openjdk.jmh.annotations`. kotlinx-benchmark is the KMP-compatible benchmark framework. Set up a `benchmark` source set per the [kotlinx-benchmark KMP setup guide](https://github.com/Kotlin/kotlinx-benchmark).

```kotlin
package org.kimplify.kurrency.benchmark

import kotlinx.benchmark.*
import org.kimplify.kurrency.*

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
open class FormattingBenchmark {

    private lateinit var formatter: CurrencyFormatter
    private lateinit var usd: Kurrency

    @Setup
    fun setup() {
        formatter = CurrencyFormatter(KurrencyLocale.US)
        usd = Kurrency.fromCode("USD").getOrThrow()
    }

    @Benchmark
    fun formatCurrencyStyle(): String = formatter.formatCurrencyStyle("1234.56", "USD")

    @Benchmark
    fun formatIsoCurrencyStyle(): String = formatter.formatIsoCurrencyStyle("1234.56", "USD")

    @Benchmark
    fun kurrencyFromCode(): Kurrency = Kurrency.fromCode("USD").getOrThrow()

    @Benchmark
    fun normalizeAmount(): String = "1,234.56".normalizeAmount()

    @Benchmark
    fun parseCurrencyAmount(): Double? = formatter.parseCurrencyAmount("$1,234.56", "USD")
}
```

- [ ] **Step 4: Verify benchmark compiles**

Run: `./gradlew :kurrency-core:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add gradle/libs.versions.toml kurrency-core/build.gradle.kts \
       kurrency-core/src/jvmTest/kotlin/org/kimplify/kurrency/benchmark/
git commit -m "feat: Add performance benchmark infrastructure with JVM formatting benchmarks"
```

### Task 8.3: Final Integration Test Run

- [ ] **Step 1: Run all tests across all modules**

Run: `./gradlew :kurrency-core:allTests :kurrency-compose:allTests :kurrency-deci:allTests -i`
Expected: All tests PASS

- [ ] **Step 2: Verify test count has increased substantially**

Check that the total test count is significantly higher than the original 92.

- [ ] **Step 3: Final commit and tag**

```bash
git tag v0.4.0-alpha01
git push origin v0.4.0-alpha01
```
