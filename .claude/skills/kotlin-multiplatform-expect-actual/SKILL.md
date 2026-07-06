---
name: kotlin-multiplatform-expect-actual
description: >
  The expect/actual mechanism in Kotlin Multiplatform — when to use it, when NOT
  to, and how to do it correctly. Covers: the four categories that genuinely warrant
  expect/actual (platform APIs, platform types, performance-critical code, SDK
  integration), the common-first rule that prefers a pure `commonMain` implementation
  before abstractions, the interface-injection alternative that handles most cases better,
  the "actual everywhere" anti-pattern, typealias actual for platform types, @ObjCName
  for clean Swift API surfaces, and Kotlin/Native memory considerations. Zero new
  dependencies.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-25'
  keywords:
    - expect actual
    - expect class
    - expect fun
    - actual typealias
    - platform API
    - ObjCName
    - Swift interop
    - Kotlin Native
    - KMP platform code
    - iOS interop
    - platform-specific
    - Kotlin Multiplatform
    - KMP
    - interface injection
    - platform type
---

## When to Use This Skill

Use when you need to:
- Write platform-specific code in a KMP project
- Decide between `expect/actual` and an interface with Koin injection
- Expose Kotlin code cleanly to Swift / Objective-C
- Avoid the common "actual everywhere" trap
- Understand why a KMP module isn't compiling on one target

**Trigger keywords:** expect actual, platform-specific code, iOS implementation, actual class,
expect fun, platform API, ObjCName, Swift interop, typealias actual, Kotlin Native memory,
platform dispatcher, platform UUID, FileSystem KMP,
platform-specific implementation, iOS only code, Android only code, platform bridge,
native implementation, different per platform, platform abstraction.

**Freshness rule:** K2 and Kotlin Native change `expect/actual` rules with each Kotlin release —
recheck the Kotlin Multiplatform docs before upgrading past a minor version.

---

## Recommendation First

Default to **implementing the behavior in `commonMain` first**.

If `commonMain` can express it cleanly and portably, keep it there. Do not add an
abstraction just because the code feels reusable; add one only when shared logic still
cannot model the behavior across targets.

When `commonMain` cannot express the behavior, default to **interface in `commonMain` +
Koin injection of platform implementations**.

Why:
- the simplest KMP solution is often a pure shared utility
- interfaces are easier to mock in tests than `actual` declarations
- Koin injection means platform code stays in platform source sets without compiler tricks
- `expect/actual` adds K2 compiler surface area that breaks more often across Kotlin upgrades

Use `expect/actual` only when the platform difference is about the **type** itself (typealias),
a platform **annotation** (`@ObjCName`), or when injection is not available (e.g., top-level
functions needed by Swift interop before Koin is initialized).

---

## The Core Question: expect/actual vs Interface Injection?

Most of the time, **don't use expect/actual**. Use an interface in `commonMain` and inject
the platform implementation via Koin.

```
Should I use expect/actual?

Does the platform difference require a different TYPE signature (not just behavior)?
  YES → expect/actual is likely correct
  NO  → use an interface + Koin injection

Does it wrap a platform SDK that cannot be abstracted at the interface level?
  YES → expect/actual is likely correct
  NO  → use an interface + Koin injection
```

The interface approach is more flexible, testable, and doesn't require a compiler mechanism.
Reserve `expect/actual` for the cases below where interfaces genuinely can't work.

For formatting and other utility work, ask first: "Can this be a pure Kotlin helper in
`commonMain`?" If yes, keep it there. If the helper needs platform-specific behavior, then
split the behavior behind an interface or `expect/actual`.

---

## The Four Categories That Warrant expect/actual

### Category 1: Platform types as construction arguments

When a platform API requires a platform-specific type that cannot exist in `commonMain`:

```kotlin
// commonMain — expect, because Context (Android) and NSApplicationContext (iOS) cannot be in commonMain
expect class PlatformContext

// androidMain
actual typealias PlatformContext = android.content.Context

// iosMain
actual typealias PlatformContext = platform.UIKit.UIViewController

// jvmMain (Desktop)
actual class PlatformContext   // empty — Desktop has no equivalent; stub it
```

The `typealias actual` pattern is the cleanest form: no duplication, the platform type IS
the actual. Use it whenever the commonMain type maps 1:1 to an existing platform type.

### Category 2: Dispatchers and coroutine infrastructure

`Dispatchers.Main` is not available in all targets without an engine. The standard pattern:

```kotlin
// commonMain
expect val MainDispatcher: CoroutineDispatcher

// androidMain
actual val MainDispatcher: CoroutineDispatcher = Dispatchers.Main

// iosMain — Dispatchers.Main requires kotlinx-coroutines-core on iOS
actual val MainDispatcher: CoroutineDispatcher = Dispatchers.Main

// jvmMain (Desktop)
actual val MainDispatcher: CoroutineDispatcher = Dispatchers.Main

// jsMain / wasmJsMain
actual val MainDispatcher: CoroutineDispatcher = Dispatchers.Main
```

> In practice, `Dispatchers.Main` works on all targets since coroutines 1.7+.
> Only use this pattern if you need a custom dispatcher (e.g., a test dispatcher injected globally).

### Category 3: Platform SDK wrappers

When the entire API surface differs per platform and an interface would just duplicate it:

```kotlin
// commonMain
expect class SecureStorage {
    fun save(key: String, value: String)
    fun get(key: String): String?
    fun delete(key: String)
}

// androidMain — uses EncryptedSharedPreferences
actual class SecureStorage(private val context: Context) {
    private val prefs by lazy {
        EncryptedSharedPreferences.create(context, "secure_prefs", ...)
    }
    actual fun save(key: String, value: String) { prefs.edit().putString(key, value).apply() }
    actual fun get(key: String): String? = prefs.getString(key, null)
    actual fun delete(key: String) { prefs.edit().remove(key).apply() }
}

// iosMain — uses Keychain
actual class SecureStorage {
    actual fun save(key: String, value: String) {
        // KeychainWrapper.set(value, forKey: key)
    }
    actual fun get(key: String): String? {
        // return KeychainWrapper.string(forKey: key)
        return null
    }
    actual fun delete(key: String) {
        // KeychainWrapper.removeObject(forKey: key)
    }
}

// jvmMain (Desktop) — uses java.util.prefs
actual class SecureStorage {
    private val prefs = java.util.prefs.Preferences.userRoot().node("app_secure")
    actual fun save(key: String, value: String) { prefs.put(key, value) }
    actual fun get(key: String): String? = prefs.get(key, null).ifEmpty { null }
    actual fun delete(key: String) { prefs.remove(key) }
}
```

### Category 4: Performance-critical primitives

When you want the platform's native implementation (not a Kotlin one) for correctness
or performance:

```kotlin
// commonMain
expect fun randomUUID(): String

// androidMain
actual fun randomUUID(): String = java.util.UUID.randomUUID().toString()

// iosMain
actual fun randomUUID(): String = platform.Foundation.NSUUID().UUIDString

// jvmMain
actual fun randomUUID(): String = java.util.UUID.randomUUID().toString()

// jsMain
actual fun randomUUID(): String = js("crypto.randomUUID()") as String

// wasmJsMain
actual fun randomUUID(): String = js("globalThis.crypto.randomUUID()")
```

---

## When to Use Interface + Injection Instead

For most platform differences, an interface in `:feature:x:api` (or `:core:common`) with
Koin-injected platform implementations is cleaner and more testable:

```kotlin
// commonMain — in :core:common or :feature:x:api
interface Logger {
    fun debug(tag: String, message: String)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}

// androidMain implementation
class AndroidLogger : Logger {
    override fun debug(tag: String, message: String) { Log.d(tag, message) }
    override fun error(tag: String, message: String, throwable: Throwable?) {
        Log.e(tag, message, throwable)
    }
}

// iosMain implementation
class IosLogger : Logger {
    override fun debug(tag: String, message: String) { NSLog("[$tag] $message") }
    override fun error(tag: String, message: String, throwable: Throwable?) {
        NSLog("[ERROR][$tag] $message ${throwable?.message ?: ""}")
    }
}

// Koin wiring — in platform-specific modules
// androidMain
val androidModule = module { single<Logger> { AndroidLogger() } }

// iosMain
val iosModule = module { single<Logger> { IosLogger() } }
```

**Advantages over expect/actual:**
- The interface can be faked in tests without `expect/actual` test implementations
- Platform implementations can have different constructor parameters
- New platforms can be added without touching `commonMain`
- The contract is visible in one place

Use **expect/actual** when the thing cannot be constructed without a platform type (Category 1),
when it wraps a platform SDK with no clean interface abstraction (Category 3), or when it's
a primitive function with no state (Category 4).

---

## The "Actual Everywhere" Anti-Pattern

The most common expect/actual mistake: wrapping every piece of functionality in `expect/actual`
because "it might be platform-specific someday":

```kotlin
// ❌ Over-using expect/actual — this is pure business logic with no platform difference
expect class UserValidator {
    fun validate(email: String): ValidationResult
}

actual class UserValidator {   // Android
    actual fun validate(email: String) = ValidationResult(email.contains("@"))
}

actual class UserValidator {   // iOS — identical implementation!
    actual fun validate(email: String) = ValidationResult(email.contains("@"))
}
```

This adds noise, duplicates code, and requires maintaining N identical implementations.
If all actuals are the same, there's no platform difference — write it in `commonMain`.

**The rule:** if you can write the implementation once in `commonMain` without touching
platform APIs, do that. `expect/actual` is only for genuine platform differences.

---

## @ObjCName: Clean Swift API Surface

Kotlin names don't always translate well to Swift/Objective-C. Use `@ObjCName` to control
the exported name without changing the Kotlin name:

```kotlin
// Without @ObjCName — Swift sees `AuthRepositoryImpl`, Kotlin has sealed interface issues
sealed interface LoginResult {
    data class Success(val user: User) : LoginResult
    data class Error(val message: String) : LoginResult
}

// With @ObjCName — Swift sees clean names
@ObjCName("LoginResult", exact = true)
sealed interface LoginResult {
    @ObjCName("LoginSuccess")
    data class Success(val user: User) : LoginResult
    @ObjCName("LoginError")
    data class Error(val message: String) : LoginResult
}
```

Key `@ObjCName` uses:
- Rename sealed interface subclasses (Swift has no sealed type system)
- Rename companion object members that clash with Swift conventions
- Remove the `Companion` suffix from companion objects
- Clarify generic type names that become unreadable in Swift

```kotlin
// ✓ Companion object without Companion suffix in Swift
class UserRepository {
    @ObjCName("shared")
    companion object {
        val instance = UserRepository()
    }
}
// Swift: UserRepository.shared  (not UserRepository.Companion.instance)
```

---

## Kotlin/Native Memory: What You Actually Need to Know

Kotlin/Native (iOS/macOS target) uses a different garbage collector than the JVM.
The main practical implication for KMP in 2024+:

**The new memory model (default since Kotlin 1.7.20) removes most frozen-object restrictions.**
You no longer need to worry about `@Frozen` or `ensureNeverFrozen()` in modern KMP code.

What still matters:

**1. MainThread in Kotlin/Native**
iOS requires UI work on the main thread. `Dispatchers.Main` handles this, but if you
accidentally call a `@MainThread` API from a background coroutine you'll get a crash —
not a compile error.

```kotlin
// ✓ Always use withContext(Dispatchers.Main) for UI-touching work on iOS
suspend fun updateUI() = withContext(Dispatchers.Main) {
    // safe to call UIKit/AppKit here
}
```

**2. Coroutine cancellation across the Kotlin/Native boundary**
When Swift code holds a reference to a Kotlin coroutine and the Swift side is deallocated,
the coroutine is not automatically cancelled. Explicitly cancel scopes:

```kotlin
class SharedViewModel {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun onDestroy() { scope.cancel() }  // call this from Swift deinit
}
```

**3. `@Throws` for Swift error handling**
Kotlin exceptions are not automatically bridged to Swift `Error`. Annotate suspend functions
that throw with `@Throws` to make them Swift-callable:

```kotlin
@Throws(IOException::class, AuthException::class)
suspend fun login(email: String, password: String): User { ... }
```

Swift sees: `try await viewModel.login(email: email, password: password)`

---

## File Structure Convention

```
:core:platform (or inline in :core:common)
  src/
    commonMain/kotlin/GROUP_ID/core/platform/
      SecureStorage.kt          ← expect class
      PlatformContext.kt        ← expect class / typealias
      randomUUID.kt             ← expect fun
    androidMain/kotlin/GROUP_ID/core/platform/
      SecureStorage.android.kt  ← actual class (Android suffix in filename for clarity)
      PlatformContext.android.kt
      randomUUID.android.kt
    iosMain/kotlin/GROUP_ID/core/platform/
      SecureStorage.ios.kt
      PlatformContext.ios.kt
      randomUUID.ios.kt
    jvmMain/kotlin/GROUP_ID/core/platform/
      SecureStorage.jvm.kt
      randomUUID.jvm.kt
    jsMain/kotlin/GROUP_ID/core/platform/
      randomUUID.js.kt
    wasmJsMain/kotlin/GROUP_ID/core/platform/
      randomUUID.wasmJs.kt
```

Suffix convention (`*.android.kt`, `*.ios.kt`) is optional but strongly recommended —
it makes the platform of each file visible in search results and file trees without
opening the file.

---

## Verification

1. `./gradlew :core:platform:compileCommonMainKotlinMetadata` — expect declarations compile
2. `./gradlew :core:platform:compileDebugKotlinAndroid` — Android actuals resolve
3. `./gradlew :core:platform:compileKotlinIosSimulatorArm64` — iOS actuals resolve
4. `./gradlew :core:platform:compileKotlinJvm` — Desktop actuals resolve
5. Swift caller: import the framework and verify `@ObjCName`-annotated types appear with correct names in Xcode autocompletion
6. Run a coroutine that crosses the Kotlin/Native boundary and verify it cancels correctly on `deinit`

---

## Common Anti-Patterns

- **JVM-only utility in `commonMain`** — `String.format`, `DecimalFormat`, `SimpleDateFormat`,
  or any other JVM-only formatter in shared code; keep the shared API in `commonMain`, but
  move the JVM-specific implementation to `jvmMain` or hide it behind a platform adapter.
- **`expect` class with state** — if an `expect class` has mutable state, it cannot be tested in `commonTest` without a real platform target. Extract the state to a shared type and keep the `actual` stateless.
- **Wrapping a single function** — `expect fun getPlatformName()` is fine; `expect class PlatformNameProvider` to wrap it is overcomplicated. Use `expect fun` directly.
- **`expect`/`actual` for dependency injection** — inject the platform dependency through Koin instead; reserve `expect`/`actual` for platform capabilities that are not injectable objects.
- **`actual` in a shared module** — `actual` declarations must live in platform source sets (`androidMain`, `iosMain`); putting them in `commonMain` defeats the purpose.
- **Not annotating with `@ObjCName`** — every `expect` declaration that surfaces to Swift should carry `@ObjCName` to control the generated Swift name. Load `kotlin-multiplatform-xcframework-spm` for guidance.

---

## Testing

```kotlin
// commonTest — test pure logic that wraps platform capabilities via an interface
interface PlatformClock {
    fun nowMillis(): Long
}

class FakePlatformClock(private val now: Long = 1_000L) : PlatformClock {
    override fun nowMillis() = now
}

@Test fun `elapsed time calculation uses injected clock`() {
    val clock = FakePlatformClock(now = 5_000L)
    val elapsed = clock.nowMillis() - 1_000L
    assertEquals(4_000L, elapsed)
}

// For expect/actual functions with no side effects, test the actual directly:
@Test fun `platform name is non-empty`() {
    assertTrue(getPlatformName().isNotBlank())
}

// androidUnitTest — verify the Android actual behaves correctly
@Test fun `android platform name contains Android`() {
    assertTrue(getPlatformName().contains("Android", ignoreCase = true))
}

// If the actual wraps a platform API, extract logic behind an interface so it can
// be faked in commonTest — this is the preferred pattern over testing the actual directly.
```

> Platform-specific actuals that wrap impure APIs (camera, location, keychain) should be
> exercised via instrumented tests or XCTest on their respective platforms. Use
> `FakeXxx` in `commonTest` for all ViewModel and domain-layer coverage.

---

## Related Skills

- `kotlin-multiplatform-dependency-injection` — preferred alternative to expect/actual for most platform abstractions
- `kotlin-multiplatform-feature-scaffold` — module structure where `actual` declarations live in platform source sets
- `kotlin-multiplatform-xcframework-spm` — `@ObjCName` annotations from this skill affect the Swift API surface
- `kotlin-multiplatform-network-layer` — platform dispatcher and engine selection are common expect/actual use cases

---

## Output Style

When asked about expect/actual or platform-specific code, respond in this order:
1. recommendation (interface + injection vs expect/actual — pick the right tool)
2. code snippet (the smallest valid expect/actual or interface pair)
3. why that approach is preferred
4. main alternative

Lead with the decision rule. Keep snippets small — one `expect`/`actual` pair or one interface, not both.

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-06 | Initial release. |
