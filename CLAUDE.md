# CLAUDE.md — Kurrency Project Reference

Kotlin Multiplatform library for type-safe currency formatting across Android, iOS, JVM, JS, and WasmJs.

## Build Commands

```bash
./gradlew build                  # Full build (all modules, all targets)
./gradlew :kurrency-core:allTests   # All core tests across every platform
./gradlew :kurrency-compose:allTests
./gradlew :kurrency-deci:allTests
```

## Test Commands by Platform

| Platform | Command |
|----------|---------|
| JVM | `./gradlew :kurrency-core:jvmTest` |
| JS/Node | `./gradlew :kurrency-core:jsNodeTest` |
| WasmJs | `./gradlew :kurrency-core:wasmJsTest` |
| iOS Simulator | `./gradlew :kurrency-core:iosSimulatorArm64Test` (requires Xcode on macOS) |
| Android Instrumented | `./gradlew :kurrency-core:connectedAndroidTest` (requires running emulator/device) |

## Module Structure

| Module | Purpose | Dependencies |
|--------|---------|--------------|
| `kurrency-core` | Foundation — `CurrencyFormatter`, `Kurrency`, `CurrencyAmount`, `KurrencyLocale`, `KurrencyError`, expect/actual platform implementations | None (library root) |
| `kurrency-compose` | Compose Multiplatform UI integration (formatters, visual-transformation helpers) | Depends on `kurrency-core` |
| `kurrency-deci` | Deci decimal-arithmetic extensions for precise currency math | Depends on `kurrency-core` + Deci library |
| `sample` | Multiplatform sample app (Android, Desktop, iOS, Web) | Depends on all library modules |

Targets per library module: Android, JVM, iOS (arm64 + simulatorArm64), JS (IR), WasmJs.

## Key Conventions

- **Result-based error handling** — public formatting APIs return `Result<T>`. Errors are modeled as `KurrencyError` sealed types.
- **expect/actual pattern** — platform-specific formatting lives in `androidMain`, `iosMain`, `jvmMain`, `jsMain`, `wasmJsMain` source sets with a shared `commonMain` API surface.
- **KDoc on all public APIs** — include usage examples for non-trivial functions. Document `Result` failure conditions.
- **Conventional Commits** — `feat:`, `fix:`, `docs:`, `test:`, `refactor:`, `perf:`, `chore:`. Scope is optional (e.g., `fix(android): ...`).
- **Kotlin style** — 4-space indentation, 120-char line limit, `camelCase` functions/properties, `PascalCase` classes.

## Publishing

Published to Maven Central via `com.vanniktech.maven.publish` plugin.

```bash
./gradlew publishAllPublicationsToMavenCentralRepository
```

Coordinates: `org.kimplify:kurrency-core`, `org.kimplify:kurrency-compose`, `org.kimplify:kurrency-deci`.

Requires signing configuration and Sonatype credentials in `gradle.properties` or environment variables.

## Requirements

- JDK 17+
- Android SDK (API 24+)
- Xcode (macOS, for iOS targets)
- Kotlin 2.3.0+
