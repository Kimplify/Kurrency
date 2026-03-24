---
name: clean-kotlin
description: >
  Use this skill when writing or modifying any Kotlin code in this project. Enforces clean code
  conventions: no comments, self-documenting names, small functions, single responsibility. Triggers
  on any code writing, editing, or review task. This skill should be checked against ALL code output
  before committing. If you are generating Kotlin code of any kind, apply these rules.
---

# Clean Kotlin Code Style

## No Comments

Never add comments to code. This is non-negotiable.

- No inline comments (`// this does X`)
- No block comments (`/* ... */`)
- No TODO comments
- No KDoc on internal code
- No commented-out code
- No "explanatory" comments

If code needs a comment to be understood, the code is wrong. Fix the code:
- Rename the variable or function
- Extract a well-named helper function
- Simplify the logic

## Self-Documenting Names

Names must communicate intent without any supporting comments.

```kotlin
// Bad
val d: Long // duration in ms
fun process(b: ByteArray) // process image bytes

// Good
val processingTimeMs: Long
fun optimizeImageBytes(imageBytes: ByteArray)
```

**Functions:** verb + noun. `fetchItems()`, `calculateTotal()`, `mapToTextBlocks()`.

**Booleans:** `is`/`has`/`can` prefix. `isProcessing`, `hasError`, `canRetry`.

**Collections:** plural nouns. `items`, `taxes`, `discounts`.

## Small Focused Functions

Each function does one thing. If you're tempted to add a comment saying "Step 1: ..., Step 2: ...", extract each step into its own function.

```kotlin
// Bad: one big function with comment sections
fun processReceipt(bytes: ByteArray) {
    // Optimize image
    ...20 lines...
    // Run OCR
    ...15 lines...
    // Parse result
    ...10 lines...
}

// Good: composed of named steps
fun processReceipt(bytes: ByteArray) {
    val optimized = optimizeImage(bytes)
    val ocrResult = recognizeText(optimized)
    val receipt = parseReceipt(ocrResult)
}
```

**Max function length:** If a function exceeds ~20 lines, consider extracting helpers.

## Single Responsibility Per File

- One primary class/interface per file
- Small related sealed classes can share a file (e.g., State + Intent + Action in a Contract file)
- Helper/extension functions stay in the file of the class they extend

## No Magic Values

```kotlin
// Bad
if (imageBytes.size > 20971520) throw ...
compress(bitmap, 85)

// Good
private const val MAX_INPUT_SIZE_BYTES = 20 * 1024 * 1024
private const val JPEG_QUALITY = 85
```

Extract constants to companion objects or top-level vals.

## Formatting

- No wildcard imports (`import com.maytes.ocr.*`)
- Imports grouped: stdlib, third-party, project
- Trailing commas on multi-line parameter lists
- `when` exhaustive — always handle all branches

## Error Handling

- Never swallow exceptions silently
- Use `runCatching` (from StoreScope) in MVI Containers
- Use `recoverToState` for global error handling
- Throw with descriptive messages: `throw IllegalArgumentException("Cannot decode image: dimensions ${width}x${height}")`

## safeCall Over Try/Catch

In repositories and data layer code, use `safeCall` from `core/common` instead of raw try/catch:

```kotlin
val result = safeCall(tag = "MyFeature") {
    someRiskyOperation()
}
return result.getOrDefault(fallbackValue)
```

`safeCall` handles:
- Wrapping in AppResult (Success/Failure)
- Logging failures via Cedar
- Re-throwing CancellationException (never swallowed)

Use `AppResult` extensions for control flow: `getOrNull()`, `getOrDefault()`, `map()`, `onSuccess()`, `onFailure()`, `isSuccess`, `isFailure`.

Raw try/catch is only acceptable in framework/infrastructure code (Store, HTTP client). Never in repositories, use cases, or data layer.

## Data Classes

- All optional fields default to `null`
- Use `copy()` for state updates
- Keep data classes small and focused
- `@Serializable` on anything that crosses a boundary (network, navigation, storage)
