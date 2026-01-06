# Changelog

All notable changes to the Kurrency library will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.3] - 2025-01-06

### Added
- New `kurrency-deci` module for integration with Deci decimal arithmetic library
- Extension functions for formatting Deci numbers with currency formatting
- Comprehensive KDoc documentation for `KurrencyError` sealed class with error handling examples
- Comprehensive KDoc documentation for `CurrencyState` class and all properties
- Documentation explaining when to use `CurrencyState` vs direct formatting
- Infinity and NaN validation in Android formatter for safer formatting operations

### Fixed
- Repository URL consistency across all build.gradle.kts files (now using ChiliNoodles/Kurrency)
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

[0.2.3]: https://github.com/ChiliNoodles/Kurrency/compare/v0.2.2...v0.2.3
[0.2.2]: https://github.com/ChiliNoodles/Kurrency/compare/v0.2.1...v0.2.2
[0.2.1]: https://github.com/ChiliNoodles/Kurrency/releases/tag/v0.2.1
