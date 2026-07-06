---
name: kotlin-multiplatform-crash-reporting
description: >
  Crash reporting for Kotlin Multiplatform apps — Firebase Crashlytics on Android/iOS,
  Sentry as a cross-platform alternative, custom non-fatal event recording, and breadcrumb
  logging. Covers: logger breadcrumb bridges, symbolication setup for Kotlin/Native
  dSYMs, and a CrashReporter expect/actual interface that keeps commonMain free of
  platform SDKs. Does NOT cover general structured logging (see logging skill) or app
  analytics/events (see analytics skill).
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-24'
  keywords:
    - crash reporting
    - crashlytics
    - firebase crashlytics
    - sentry
    - non-fatal
    - symbolication
    - dsym
    - breadcrumb bridge
    - crash handler
    - breadcrumb
    - crash analytics
---

## When to Use This Skill

Use this skill when:
- You need crash reports from production Android and iOS builds
- You want non-fatal exception tracking (e.g. caught errors that degrade the experience)
- You need breadcrumb logs attached to crash reports for context
- You are setting up dSYM upload for Kotlin/Native crash symbolication

Do NOT use this skill when:
- You only need local debug logging — use `kotlin-multiplatform-logging`
- You need user behaviour analytics — use `kotlin-multiplatform-analytics`

**Trigger keywords:** crash reporting, crashlytics, firebase crashes, sentry, non-fatal error,
symbolication, dSYM, breadcrumb bridge, crash handler, crash analytics, crash tracking,
crash, exception handling, error reporting, track errors, exception tracking, app crash,
error tracking, report errors, debug crashes, crash diagnostics.

**Freshness rule:** Firebase Crashlytics and Sentry SDKs update frequently. Recheck the
[Firebase BoM](https://firebase.google.com/support/release-notes/android) and the
[Sentry KMP SDK changelog](https://github.com/getsentry/sentry-kotlin-multiplatform/releases)
before adding or updating dependencies. Keep breadcrumb bridges aligned with the logger
facade the project actually uses.

---

## Recommendation First

Default to **Firebase Crashlytics + a breadcrumb bridge** for apps already using Firebase.
Use **Sentry KMP** if you need a single SDK on Android, iOS, and Desktop without Firebase.

The pattern is the same for both providers:
1. Define a `CrashReporter` interface in `commonMain`
2. Implement it per platform using the native SDK
3. Wire through Koin — inject into the root `App()` and your error boundaries
4. Route your logger wrapper to the crash reporter so logs appear as breadcrumbs

```kotlin
// commonMain — :api
interface CrashReporter {
    fun recordException(throwable: Throwable, context: Map<String, String> = emptyMap())
    fun recordNonFatal(message: String, context: Map<String, String> = emptyMap())
    fun setUserId(id: String)
    fun clearUserId()
    fun addBreadcrumb(message: String, category: String = "app")
}
```

---

## Firebase Crashlytics Setup

### `libs.versions.toml`

```toml
[versions]
firebase-bom = "33.1.0"

[libraries]
firebase-crashlytics    = { module = "com.google.firebase:firebase-crashlytics" }
firebase-crashlytics-ndk = { module = "com.google.firebase:firebase-crashlytics-ndk" }
kermit-crashlytics      = { module = "co.touchlab:kermit-crashlytics", version.ref = "kermit" }

[plugins]
google-services    = { id = "com.google.gms.google-services", version = "4.4.2" }
firebase-crashlytics = { id = "com.google.firebase.crashlytics", version = "3.0.2" }
```

### Android `build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.crashlytics.ndk)   // for Kotlin/Native crashes
    implementation(libs.kermit.crashlytics)
}
```

### Android actual

```kotlin
// androidMain
class FirebaseCrashReporterImpl : CrashReporter {
    override fun recordException(throwable: Throwable, context: Map<String, String>) {
        context.forEach { (k, v) -> FirebaseCrashlytics.getInstance().setCustomKey(k, v) }
        FirebaseCrashlytics.getInstance().recordException(throwable)
    }

    override fun recordNonFatal(message: String, context: Map<String, String>) {
        context.forEach { (k, v) -> FirebaseCrashlytics.getInstance().setCustomKey(k, v) }
        FirebaseCrashlytics.getInstance().log(message)
    }

    override fun setUserId(id: String)  { FirebaseCrashlytics.getInstance().setUserId(id) }
    override fun clearUserId()          { FirebaseCrashlytics.getInstance().setUserId("") }
    override fun addBreadcrumb(message: String, category: String) {
        FirebaseCrashlytics.getInstance().log("[$category] $message")
    }
}
```

### iOS actual (via `FirebaseCrashlytics` CocoaPods / SPM)

```kotlin
// iosMain
class FirebaseCrashReporterImpl : CrashReporter {
    override fun recordException(throwable: Throwable, context: Map<String, String>) {
        context.forEach { (k, v) -> Crashlytics.crashlytics().setCustomValue(v, forKey = k) }
        // Wrap KotlinException in NSError for Crashlytics
        val userInfo = mapOf(NSLocalizedDescriptionKey to (throwable.message ?: "unknown"))
        val nsError = NSError.errorWithDomain("KotlinException", code = 0, userInfo = userInfo)
        Crashlytics.crashlytics().recordError(nsError)
    }

    override fun recordNonFatal(message: String, context: Map<String, String>) {
        context.forEach { (k, v) -> Crashlytics.crashlytics().setCustomValue(v, forKey = k) }
        Crashlytics.crashlytics().log(message)
    }

    override fun setUserId(id: String)  { Crashlytics.crashlytics().setUserID(id) }
    override fun clearUserId()          { Crashlytics.crashlytics().setUserID("") }
    override fun addBreadcrumb(message: String, category: String) {
        Crashlytics.crashlytics().log("[$category] $message")
    }
}
```

---

## Sentry KMP Alternative

For cross-platform including Desktop, use `io.sentry:sentry-kotlin-multiplatform`:

```toml
[versions]
sentry-kmp = "0.10.0"

[libraries]
sentry-kmp = { module = "io.sentry:sentry-kotlin-multiplatform", version.ref = "sentry-kmp" }
```

```kotlin
// commonMain actual — Sentry provides a single KMP API
class SentryCrashReporterImpl : CrashReporter {
    override fun recordException(throwable: Throwable, context: Map<String, String>) {
        Sentry.captureException(throwable) { scope ->
            context.forEach { (k, v) -> scope.setExtra(k, v) }
        }
    }
    override fun recordNonFatal(message: String, context: Map<String, String>) {
        Sentry.captureMessage(message, SentryLevel.WARNING) { scope ->
            context.forEach { (k, v) -> scope.setExtra(k, v) }
        }
    }
    override fun setUserId(id: String)  { Sentry.setUser(User().apply { this.id = id }) }
    override fun clearUserId()          { Sentry.setUser(null) }
    override fun addBreadcrumb(message: String, category: String) {
        Sentry.addBreadcrumb(Breadcrumb(message).apply { this.category = category })
    }
}
```

---

## Logger Breadcrumb Bridge

Route your logger wrapper to the crash reporter so every log becomes a breadcrumb:

```kotlin
class CrashReporterBreadcrumbSink(private val reporter: CrashReporter) {
    fun breadcrumb(level: String, tag: String, message: String, throwable: Throwable?) {
        reporter.addBreadcrumb(message = "[$tag] $message", category = level)
        if (throwable != null) {
            reporter.recordException(throwable, context = mapOf("tag" to tag))
        }
    }
}

// In your Koin module:
single { CrashReporterBreadcrumbSink(get<CrashReporter>()) }
```

---

## Symbolication (Kotlin/Native dSYMs)

Kotlin/Native crashes on iOS show mangled frames without dSYM upload.

**Crashlytics:** add the dSYM upload script to your Xcode build phases:
```
"${PODS_ROOT}/FirebaseCrashlytics/run"
```
And pass the Kotlin/Native dSYM directory:
```
"${PODS_ROOT}/FirebaseCrashlytics/upload-symbols" \
  -gsp "${SRCROOT}/GoogleService-Info.plist" \
  -p ios \
  "${BUILT_PRODUCTS_DIR}/${WRAPPER_NAME}.dSYM" \
  "${KOTLIN_NATIVE_DSYM_PATH}"
```

**Sentry:** use the Sentry Gradle plugin:
```kotlin
id("io.sentry.android.gradle") version "4.x.x"
// sentry { uploadNativeSymbols = true }
```

---

## Koin Wiring

```kotlin
// androidMain
single<CrashReporter> { FirebaseCrashReporterImpl() }

// iosMain
single<CrashReporter> { FirebaseCrashReporterImpl() }   // or SentryCrashReporterImpl()

// commonMain
single { CrashReporterBreadcrumbSink(get()) }
```

Initialize the crash SDK before Koin in `Application.onCreate()` / `application(_:didFinishLaunchingWithOptions:)`.

---

## Testing

```kotlin
class FakeCrashReporter : CrashReporter {
    val exceptions   = mutableListOf<Throwable>()
    val nonFatals    = mutableListOf<String>()
    val breadcrumbs  = mutableListOf<Pair<String, String>>()   // message to category
    var userId: String? = null

    override fun recordException(throwable: Throwable, context: Map<String, String>) { exceptions += throwable }
    override fun recordNonFatal(message: String, context: Map<String, String>)       { nonFatals += message }
    override fun setUserId(id: String)  { userId = id }
    override fun clearUserId()          { userId = null }
    override fun addBreadcrumb(message: String, category: String) { breadcrumbs += message to category }
}

@Test fun `recordException is called on domain error`() = runTest {
    val reporter = FakeCrashReporter()
    val vm = SomeViewModel(FakeSomeRepository().apply { throw RuntimeException("DB error") }, reporter)
    vm.load()
    assertEquals(1, reporter.exceptions.size)
    assertTrue(reporter.exceptions.first().message!!.contains("DB error"))
}

@Test fun `setUserId is called after login`() = runTest {
    val reporter = FakeCrashReporter()
    val vm = AuthViewModel(FakeAuthService(), reporter)
    vm.onIntent(AuthIntent.Login("user@x.com", "pass"))
    assertEquals("user-123", reporter.userId)
}

@Test fun `clearUserId is called on logout`() = runTest {
    val reporter = FakeCrashReporter().apply { userId = "user-123" }
    val vm = AuthViewModel(FakeAuthService(), reporter)
    vm.onIntent(AuthIntent.Logout)
    assertNull(reporter.userId)
}

@Test fun `logger error forwards to crash reporter`() = runTest {
    val reporter = FakeCrashReporter()
    val writer = CrashReporterBreadcrumbSink(reporter)
    val error = RuntimeException("test crash")
    writer.breadcrumb("error", "Network", "connection failed", error)
    assertEquals(1, reporter.exceptions.size)
    assertEquals(1, reporter.breadcrumbs.size)
}
```

---

## Common Anti-Patterns

- **Initializing Crashlytics/Sentry after Koin** — the crash SDK must be the first thing
  initialized in `Application.onCreate()`. A crash during Koin startup would be invisible otherwise.
- **Recording every caught exception as a crash** — this floods your crash dashboard. Only
  record exceptions that indicate real errors; use `recordNonFatal` for expected degraded states.
- **Leaking PII into crash context** — never pass email, name, phone, or tokens as `context`
  keys. Use opaque IDs (`userId`, `sessionId`) only.
- **Skipping dSYM upload** — Kotlin/Native crashes are unreadable without dSYM symbolication.
  Set up the upload script before shipping to production.
- **Using `FirebaseCrashlytics` directly in commonMain** — Firebase SDK is Android/iOS only.
  Always route through the `CrashReporter` interface.

---

## Related Skills

- `kotlin-multiplatform-logging` — structured logging; wire the logger wrapper into the breadcrumb bridge
- `kotlin-multiplatform-analytics` — event tracking; crash reporting is about exceptions, not behaviour
- `kotlin-multiplatform-expect-actual` — alternative approach if you prefer `expect class` over interface injection
- `kotlin-multiplatform-dependency-injection` — Koin wiring for `CrashReporter` binding per platform

---

## Output Style

When asked about crash reporting, respond in this order:
1. recommendation (Crashlytics vs Sentry — ask if Firebase is already in the project)
2. the `CrashReporter` interface in commonMain
3. Android and iOS actuals (shortest viable snippet)
4. logger breadcrumb bridge integration
5. dSYM symbolication reminder

Lead with the interface — never put SDK imports in commonMain code.

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-21 | Initial release. |
