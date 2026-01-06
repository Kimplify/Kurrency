# Contributing to Kurrency

Thank you for your interest in contributing to Kurrency! This document provides guidelines and information for contributors.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [How to Contribute](#how-to-contribute)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Commit Message Guidelines](#commit-message-guidelines)
- [Pull Request Process](#pull-request-process)
- [Reporting Bugs](#reporting-bugs)
- [Suggesting Features](#suggesting-features)

## Code of Conduct

By participating in this project, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md). Please read it before contributing.

## Getting Started

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone https://github.com/YOUR-USERNAME/Kurrency.git
   cd Kurrency
   ```
3. Add the upstream repository:
   ```bash
   git remote add upstream https://github.com/ChiliNoodles/Kurrency.git
   ```
4. Create a new branch for your changes:
   ```bash
   git checkout -b feature/your-feature-name
   ```

## Development Setup

### Requirements

- JDK 17 or higher
- Android SDK (API 24+)
- Xcode (for iOS development, macOS only)
- Kotlin 2.3.0+
- Gradle 8.0+

### Building the Project

```bash
./gradlew build
```

### Running Tests

```bash
# Run all tests
./gradlew allTests

# Run tests for specific module
./gradlew :kurrency-core:test
./gradlew :kurrency-compose:test
./gradlew :kurrency-deci:test

# Run Android instrumented tests
./gradlew :kurrency-core:connectedAndroidTest
```

### Running the Sample App

```bash
# Android
./gradlew :sample:installDebug

# Desktop (JVM)
./gradlew :sample:run

# iOS (requires macOS and Xcode)
# Open sample/iosApp in Xcode and run
```

## How to Contribute

### Types of Contributions

We welcome:

- **Bug fixes** - Fix issues in existing functionality
- **New features** - Add new currency formatting capabilities
- **Platform support** - Improve or extend platform-specific implementations
- **Documentation** - Improve README, KDoc, or examples
- **Tests** - Add or improve test coverage
- **Performance improvements** - Optimize formatting operations
- **Locale support** - Add support for additional locales

### Before You Start

1. Check existing [issues](https://github.com/ChiliNoodles/Kurrency/issues) and [pull requests](https://github.com/ChiliNoodles/Kurrency/pulls)
2. For major changes, open an issue first to discuss your approach
3. Make sure tests pass locally before submitting a PR

## Coding Standards

### Kotlin Style Guide

We follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html):

- Use 4 spaces for indentation (no tabs)
- Maximum line length: 120 characters
- Use camelCase for functions and properties
- Use PascalCase for classes and interfaces
- Avoid unnecessary comments - code should be self-documenting

### Documentation

- All public APIs must have KDoc documentation
- Include examples in KDoc for non-trivial APIs
- Document when functions throw exceptions or return Result types
- Explain platform-specific behavior in platform implementation files

### Example KDoc:

```kotlin
/**
 * Formats a currency amount using the specified locale.
 *
 * @param amount The amount to format as a string
 * @param currencyCode ISO 4217 currency code (e.g., "USD", "EUR")
 * @return Result containing formatted string, or error if formatting fails
 *
 * Example:
 * ```kotlin
 * val formatted = formatter.formatCurrencyStyleResult("1234.56", "USD")
 *     .getOrNull() // "$1,234.56" in en-US locale
 * ```
 */
fun formatCurrencyStyleResult(amount: String, currencyCode: String): Result<String>
```

## Testing Guidelines

### Test Requirements

- All new features must include tests
- Bug fixes should include regression tests
- Aim for high test coverage (minimum 80% for new code)
- Test edge cases: empty strings, invalid input, boundary values

### Test Structure

```kotlin
class CurrencyFormatterTest {
    @Test
    fun formatCurrencyStyle_withValidInput_returnsFormattedString() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyleResult("1234.56", "USD")

        assertTrue(result.isSuccess)
        assertEquals("$1,234.56", result.getOrNull())
    }

    @Test
    fun formatCurrencyStyle_withInvalidCurrency_returnsError() {
        val formatter = CurrencyFormatter(KurrencyLocale.US)
        val result = formatter.formatCurrencyStyleResult("1234.56", "INVALID")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is KurrencyError.InvalidCurrencyCode)
    }
}
```

### Platform-Specific Testing

- Add common tests in `commonTest/`
- Add platform-specific tests in `androidInstrumentedTest/`, `iosTest/`, etc.
- Verify behavior across all supported platforms

## Commit Message Guidelines

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `test`: Adding or updating tests
- `refactor`: Code refactoring
- `perf`: Performance improvements
- `chore`: Build process or tooling changes

### Examples

```
feat(compose): Add rememberSystemCurrencyFormatter composable

Implement new composable function that automatically uses system locale
for currency formatting. Includes reactive updates when system locale changes.

Closes #42
```

```
fix(android): Add infinity/NaN validation to formatter

Android formatter was missing finite number validation present in other
platforms. This could cause incorrect formatting of edge case values.
```

## Pull Request Process

### Before Submitting

1. Update your branch with the latest upstream changes:
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. Run all tests and ensure they pass:
   ```bash
   ./gradlew allTests
   ```

3. Run code formatting:
   ```bash
   ./gradlew ktlintFormat
   ```

4. Update documentation if needed:
   - Update README.md for new features
   - Add/update KDoc comments
   - Update CHANGELOG.md

### Submitting the PR

1. Push your branch to your fork:
   ```bash
   git push origin feature/your-feature-name
   ```

2. Open a Pull Request on GitHub

3. Fill out the PR template with:
   - Description of changes
   - Related issue numbers
   - Testing performed
   - Screenshots (if UI changes)

4. Ensure CI checks pass

5. Request review from maintainers

### Review Process

- Maintainers will review your PR within 1-2 weeks
- Address feedback by pushing new commits
- Once approved, maintainers will merge your PR
- Your contribution will be included in the next release

## Reporting Bugs

### Before Reporting

1. Check if the issue already exists
2. Verify you're using the latest version
3. Test on multiple platforms if possible

### Bug Report Template

```markdown
**Describe the bug**
A clear description of what the bug is.

**To Reproduce**
Steps to reproduce the behavior:
1. Create formatter with locale X
2. Format amount Y with currency Z
3. See error

**Expected behavior**
What you expected to happen.

**Actual behavior**
What actually happened.

**Environment:**
- Kurrency version: 0.2.3
- Platform: Android/iOS/JVM/JS/WasmJs
- OS version: Android 14, iOS 17, etc.
- Kotlin version: 2.3.0

**Code sample:**
```kotlin
val formatter = CurrencyFormatter(KurrencyLocale.US)
val result = formatter.formatCurrencyStyleResult("1234.56", "USD")
```
```

## Suggesting Features

### Feature Request Template

```markdown
**Is your feature request related to a problem?**
A clear description of the problem. Ex. I'm always frustrated when [...]

**Describe the solution you'd like**
A clear description of what you want to happen.

**Describe alternatives you've considered**
Other solutions or features you've considered.

**Additional context**
Any other context, mockups, or examples.
```

## Questions?

- Open a [GitHub Discussion](https://github.com/ChiliNoodles/Kurrency/discussions)
- Open an [issue](https://github.com/ChiliNoodles/Kurrency/issues) with the `question` label

## License

By contributing to Kurrency, you agree that your contributions will be licensed under the Apache License 2.0.

---

Thank you for contributing to Kurrency! 🎉
