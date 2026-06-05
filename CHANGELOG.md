# Changelog

All notable changes to the Kurrency library will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.4.0] - 2026-06-05

A feature and correctness release. **Contains one breaking change** — the Compose
state API moved out of `kurrency-core` (see Changed). Consumers of `kurrency-core`
that don't use Compose are unaffected.

### Added
- **`CurrencyFormatOptions.hideZeroFractionDigits`** — opt-in flag that omits the
  fraction digits entirely when they are all zero (`$34.00` → `$34`), while
  non-zero fractions are displayed unchanged (`$34.20`, `$34.88`). Takes priority
  over `minFractionDigits` and is a no-op for zero-fraction currencies such as JPY. (#5)
- **`CurrencyFormatOptions.roundingMode`** with a new `RoundingMode` enum
  (`HALF_EVEN` default, `HALF_UP`, `DOWN`, `UP`), letting callers choose how amounts
  with excess fraction digits are rounded. (#14)
- **`kurrency-deci` options support** — new `CurrencyFormatter.formatWithOptions(amount: Deci, …)`
  overloads (currency-code and `Kurrency` variants), now formatted exactly.

### Changed
- **BREAKING — Compose state moved to `kurrency-compose`.** `CurrencyState`,
  `rememberCurrencyState`, `FormattedAmountDelegate`, and
  `CurrencyState.formattedAmount()` moved from `kurrency-core` (package
  `org.kimplify.kurrency`) to `kurrency-compose` (package `org.kimplify.kurrency.compose`).
  As a result `kurrency-core` no longer depends on the Compose runtime and is again a
  dependency-free foundation module.

  Migration — add the `kurrency-compose` dependency (if not already present) and
  update imports:

  ```diff
  - import org.kimplify.kurrency.rememberCurrencyState
  + import org.kimplify.kurrency.compose.rememberCurrencyState
  ```

  These APIs are annotated `@ExperimentalKurrency`. Non-Compose consumers of
  `kurrency-core` need no changes.
- **`formatWithOptions` is now decimal-exact.** It no longer round-trips the amount
  through `Double`, so values beyond `Double` precision format exactly and prior
  rounding artifacts are gone (e.g. `2.675` now rounds to `2.68` under the default
  half-even mode). (#14)
- Toolchain bumps: Kotlin 2.4.0, AGP 9.2.1, Gradle 9.5.1, Compose Multiplatform
  1.11.1, kotlinx-serialization 1.11.0, kotlinx-coroutines 1.11.0.

### Fixed
- **Currency fraction digits followed the display locale instead of the currency.**
  Formatting JPY under a non-Japanese locale produced `¥1,235.00` instead of
  `¥1,235`. The digit count is now tied to the currency, so JPY (0), USD (2),
  BHD (3), etc. are correct under every locale. (#10)
- **Android currency validation was too lenient** — `Kurrency.isValid("XYZ")`
  returned `true` because ICU accepts any well-formed 3-letter code; validation now
  checks the known ISO 4217 set, matching JVM and iOS.
- **Lowercase and scientific-notation amounts in the options path** — codes like
  `"usd"` and inputs like `"1e3"` are now normalized before formatting instead of
  falling back to the raw amount.

### Internal
- Android instrumented tests are wired to the `androidDeviceTest` source set and now
  execute on device (previously zero ran). The host unit-test variant was dropped
  (it cannot run Android ICU formatting on a stub JVM). (#16)
- README corrected for API accuracy (`Kurrency` vs `Currency`, instance vs static
  formatting, Compose locale interop) and polished. (#9, #12)

## [0.3.1] - 2026-04-07

A precision and consistency release for the new minor units formatting API. No
breaking changes — fully backward compatible with `0.3.0`.

### Added
- **String-based minor units formatting API** with full parity across
  `CurrencyFormatter`, `CurrencyFormat`, `Kurrency`, and `CurrencyAmount`. New
  ISO, compact, and `CurrencyFormatOptions`-based variants accept `Long` minor
  units and produce output identical to their major-unit counterparts, without
  the precision loss inherent to a `Double` round-trip.
- Comprehensive minor units formatting test suite covering boundary values,
  precision-sensitive currencies, and locale-specific separators across all
  supported targets.

### Fixed
- **Precision loss in `minorUnitsToPlainString`.** The conversion no longer
  divides by a floating-point power of ten; it now uses string-based arithmetic
  so every minor unit value round-trips exactly, including currencies and
  magnitudes that `Double` cannot represent.
- **`Long.MIN_VALUE` handling in `minorUnitsToPlainString`.** Previously
  `kotlin.math.abs(Long.MIN_VALUE)` overflowed back to itself, producing
  malformed output such as `"--92233720368547758.08"`. The sign is now derived
  from the input and the magnitude is computed via string-based absolute value.
- **Double formatter initialization in `CurrencyAmount.format(style)` and
  `CurrencyAmount.format(options)`.** Both overloads previously constructed a
  `CurrencyFormatter` solely to compute the plain-string amount and then
  delegated to `Kurrency.formatAmount*`, which built a second formatter. Both
  paths now route through the formatter's minor-units helpers directly,
  halving allocation cost on the hot path.
- Guarded `fractionDigits <= 0` in minor units conversion to handle degenerate
  currency definitions safely.

### Changed
- Removed the `docs/superpowers` directory from version control and added a
  matching entry to `.gitignore`.

## [0.2.3] - 2025-01-06

### Added
- New `kurrency-deci` module for integration with Deci decimal arithmetic library
- Extension functions for formatting Deci numbers with currency formatting
- Comprehensive KDoc documentation for `KurrencyError` sealed class with error handling examples
- Comprehensive KDoc documentation for `CurrencyState` class and all properties
- Documentation explaining when to use `CurrencyState` vs direct formatting
- Infinity and NaN validation in Android formatter for safer formatting operations

### Fixed
- Repository URL consistency across all build.gradle.kts files (now using Kimplify/Kurrency)
- Android formatter now validates for infinity and NaN values before formatting
- README version numbers updated from 0.2.1 to 0.2.3
- Default formatted amount handling in CurrencyState
- Invalid currency test in CurrencyState

### Changed
- Improved currency handling in CurrencyState and related tests
- Excluded tests from MavenCentral publish command for streamlined deployment
- Extended exclusions in MavenCentral publish command to optimize deployment process

## [0.2.2] - 2024

### Added
- Enhanced KurrencyLocale with Compose support
- Locale change handling across all platforms
- System formatting support with locale-aware decimal and grouping separators
- Web-specific currency formatting and locale handling
- Platform-specific locale detection for web platforms (JS/WasmJs)

### Changed
- Refactored KurrencyLocale and Compose extensions for improved structure and clarity
- Improved build configuration for Kotlin Multiplatform
- Enhanced Android library support
- Replaced Cedar logging with KurrencyLog for improved consistency

### Removed
- ComposeHotReload plugin for cleaner build configuration

## [0.2.1] - 2024

### Added
- Initial public release
- Multi-platform support for Android (API 24+), iOS (13+), JVM (17+), JS, and WasmJs
- Type-safe currency formatting with Result-based error handling
- 15+ predefined locales (US, UK, Canada, Germany, France, Japan, etc.)
- Custom locale support via BCP 47 language tags
- System locale detection
- Jetpack Compose Multiplatform integration (`kurrency-compose` module)
- `rememberCurrencyFormatter()` and `LocalCurrencyFormatter` for reactive locale updates
- Standard and ISO currency format styles
- Currency metadata with ISO 4217 support
- Comprehensive test coverage for core functionality
- Platform-specific implementations using native formatting APIs

### Features
- Android: ICU-based formatting
- iOS: NSNumberFormatter integration
- JVM: java.text.NumberFormat
- JS/WasmJs: Intl.NumberFormat API
- Automatic fraction digit detection per currency (e.g., 2 for USD, 0 for JPY)
- Support for comma and dot decimal separators in input
- Locale-aware grouping and decimal separators in output

## [Unreleased]

### Planned
- Performance benchmarks
- Additional platform-specific tests for iOS, JVM, JS, and WasmJs
- Thread safety tests and documentation
- Enhanced Unicode and RTL locale support
- API naming consistency improvements

---

## Migration Guides

### Migrating from 0.2.1 to 0.2.3

No breaking changes. This release is fully backward compatible.

**New Features Available:**
- Optional `kurrency-deci` module for decimal arithmetic integration
- Enhanced documentation and error handling guidance

**Recommended Actions:**
- Update dependency versions in your build.gradle.kts:
  ```kotlin
  implementation("org.kimplify:kurrency-core:0.2.3")
  implementation("org.kimplify:kurrency-compose:0.2.3")  // if using Compose
  implementation("org.kimplify:kurrency-deci:0.2.3")    // optional
  ```
- Review new KDoc documentation for `KurrencyError` and `CurrencyState` for improved API usage patterns

---

## Version Support

| Version | Release Date | Support Status |
|---------|--------------|----------------|
| 0.2.3   | 2025-01-06   | ✅ Current     |
| 0.2.2   | 2024         | ⚠️ Deprecated  |
| 0.2.1   | 2024         | ⚠️ Deprecated  |

---

[0.3.1]: https://github.com/Kimplify/Kurrency/compare/v0.3.0...v0.3.1
[0.2.3]: https://github.com/Kimplify/Kurrency/compare/v0.2.2...v0.2.3
[0.2.2]: https://github.com/Kimplify/Kurrency/compare/v0.2.1...v0.2.2
[0.2.1]: https://github.com/Kimplify/Kurrency/releases/tag/v0.2.1
