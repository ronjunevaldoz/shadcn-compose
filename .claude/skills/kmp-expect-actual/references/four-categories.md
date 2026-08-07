# The Four Categories That Warrant expect/actual

Part of `kmp-expect-actual`.

---

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

