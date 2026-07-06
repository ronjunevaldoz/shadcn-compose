---
name: kotlin-multiplatform-datastore
description: >
  Sets up Multiplatform DataStore for KMP: Preferences (key-value) and Proto
  (typed, schema-driven) variants. Covers createDataStore {} expect/actual per
  platform, Flow-based reads, coroutine writes, migration from SharedPreferences,
  and Koin wiring so feature modules never construct DataStore directly.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-18'
  keywords:
    - DataStore
    - Preferences DataStore
    - Proto DataStore
    - KMP
    - Kotlin Multiplatform
    - SharedPreferences migration
    - Flow reads
    - persistent storage
    - key-value store
    - Koin
    - expect actual
---

## When to Use This Skill

Use when you need to:
- Store small, typed key-value data that survives app restart (user prefs, tokens, flags)
- Replace `SharedPreferences` (Android) or `NSUserDefaults` (iOS) with a single KMP API
- Store structured data with a Protobuf schema (Proto DataStore)
- Read persisted state as a `Flow` that recomposes automatically on change
- Wire DataStore into Koin so every feature accesses it without constructing it manually

**Trigger keywords:** DataStore, Preferences DataStore, Proto DataStore, SharedPreferences KMP,
NSUserDefaults KMP, persist settings, key-value KMP, store user preferences, DataStore migration,
DataStore Flow, DataStore Koin,
local storage KMP, app settings KMP, save user settings, persist app state,
user preferences KMP, settings persistence, save preferences, key value storage KMP.

**Freshness rule:** `androidx.datastore` KMP artifact names and the `createDataStore {}`
expect/actual API change between alpha versions — recheck the AndroidX releases page before
pinning a version. As of mid-2026 the KMP-stable artifact is `androidx.datastore:datastore-preferences-core`.

---

## Recommendation First

Default to **Preferences DataStore for key-value storage, Proto DataStore only when you
need schema enforcement or atomic multi-field updates**.

Why:
- Preferences DataStore requires zero schema setup — just define typed keys and read/write
- `Flow<Preferences>` integrates directly with `StateFlow` in the `:presenter` layer
- The expect/actual pattern is minimal — one `createDataStore {}` factory per platform,
  wired through Koin so nothing downstream knows about platform paths
- Proto DataStore adds Protobuf compilation to the build; only worth it for complex
  structured settings (e.g. user profile cache, feature flag bundles)

Migrate from `SharedPreferences` using `SharedPreferencesMigration` — one line, no data loss.

---

## Gradle Setup

### `libs.versions.toml`

```toml
[versions]
datastore = "1.1.1"

[libraries]
datastore-preferences = { module = "androidx.datastore:datastore-preferences-core", version.ref = "datastore" }
datastore-proto = { module = "androidx.datastore:datastore-core", version.ref = "datastore" }
```

### Convention plugin: add to `GROUP_ID.core.gradle.kts`

```kotlin
sourceSets {
    commonMain.dependencies {
        implementation(libs.datastore.preferences)
    }
}
```

DataStore lives in `:core:datastore` — feature modules consume it through a Koin-injected
interface, never by constructing DataStore directly.

---

## Module Structure

```
core/datastore/
├── build.gradle.kts
└── src/
    ├── commonMain/kotlin/GROUP_ID/core/datastore/
    │   ├── AppPreferences.kt          ← typed key definitions
    │   ├── PreferencesDataSource.kt   ← interface
    │   ├── PreferencesDataSourceImpl.kt
    │   └── di/DatastoreModule.kt
    ├── androidMain/kotlin/GROUP_ID/core/datastore/
    │   └── DataStoreFactory.android.kt
    └── iosMain/kotlin/GROUP_ID/core/datastore/
        └── DataStoreFactory.ios.kt
```

---

## Preferences DataStore

### Key definitions — `AppPreferences.kt`

```kotlin
object AppPreferences {
    val IS_ONBOARDED   = booleanPreferencesKey("is_onboarded")
    val AUTH_TOKEN     = stringPreferencesKey("auth_token")
    val SELECTED_THEME = stringPreferencesKey("selected_theme")
    val LAST_SYNC_MS   = longPreferencesKey("last_sync_ms")
}
```

### Interface — `PreferencesDataSource.kt`

```kotlin
interface PreferencesDataSource {
    val isOnboarded: Flow<Boolean>
    val authToken: Flow<String?>
    suspend fun setOnboarded(value: Boolean)
    suspend fun setAuthToken(token: String?)
    suspend fun clear()
}
```

### Implementation — `PreferencesDataSourceImpl.kt`

```kotlin
class PreferencesDataSourceImpl(
    private val dataStore: DataStore<Preferences>,
) : PreferencesDataSource {

    override val isOnboarded: Flow<Boolean> =
        dataStore.data
            .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
            .map { it[AppPreferences.IS_ONBOARDED] ?: false }

    override val authToken: Flow<String?> =
        dataStore.data
            .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
            .map { it[AppPreferences.AUTH_TOKEN] }

    override suspend fun setOnboarded(value: Boolean) {
        dataStore.edit { it[AppPreferences.IS_ONBOARDED] = value }
    }

    override suspend fun setAuthToken(token: String?) {
        dataStore.edit {
            if (token != null) it[AppPreferences.AUTH_TOKEN] = token
            else it.remove(AppPreferences.AUTH_TOKEN)
        }
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}
```

---

## expect/actual — Platform DataStore Factory

### `commonMain` — `DataStoreFactory.kt`

```kotlin
internal expect fun createDataStore(producePath: () -> String): DataStore<Preferences>
```

### `androidMain` — `DataStoreFactory.android.kt`

```kotlin
internal actual fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )
```

### `iosMain` — `DataStoreFactory.ios.kt`

```kotlin
internal actual fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { producePath().toPath() }
    )
```

### Platform path helpers

```kotlin
// androidMain — inject via Koin: get<Context>()
fun dataStorePath(context: Context): String =
    context.filesDir.resolve("app_preferences.preferences_pb").absolutePath

// iosMain
fun dataStorePath(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(documentDirectory).path + "/app_preferences.preferences_pb"
}
```

---

## Koin Wiring

### `core/datastore/di/DatastoreModule.kt`

```kotlin
val datastoreModule = module {
    single<DataStore<Preferences>> {
        createDataStore {
            // Android: androidContext().filesDir path
            // iOS: NSDocumentDirectory path
            // Both resolved at runtime via the platform factory
            platformDataStorePath(getOrNull())
        }
    }
    single<PreferencesDataSource> { PreferencesDataSourceImpl(get()) }
}

// Platform dispatch helper — commonMain
internal expect fun platformDataStorePath(context: Any?): String
```

Or use the simpler approach — declare the DataStore in platform-specific Koin modules:

```kotlin
// androidMain — AndroidDatastoreModule.kt
val androidDatastoreModule = module {
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { androidContext().filesDir.resolve("app_prefs.preferences_pb").absolutePath.toPath() }
        )
    }
}

// iosMain — IosDatastoreModule.kt
val iosDatastoreModule = module {
    single<DataStore<Preferences>> {
        createDataStore { iosDataStorePath() }
    }
}
```

Declare the common half in both app entry points:
```kotlin
// commonMain — shared module (no platform dep)
val datastoreModule = module {
    single<PreferencesDataSource> { PreferencesDataSourceImpl(get()) }
}
```

---

## Reading in a ViewModel

```kotlin
// :feature:settings:presenter
class SettingsViewModel(
    private val prefs: PreferencesDataSource,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.isOnboarded.collect { onboarded ->
                _uiState.update { it.copy(isOnboarded = onboarded) }
            }
        }
    }

    fun onIntent(intent: SettingsUiIntent) {
        when (intent) {
            is SettingsUiIntent.SetTheme -> viewModelScope.launch {
                prefs.setAuthToken(intent.theme)
            }
            SettingsUiIntent.SignOut -> viewModelScope.launch { prefs.clear() }
        }
    }
}
```

`PreferencesDataSource` is injected — no DataStore import in `:presenter`.

---

## Migration from SharedPreferences

```kotlin
// androidMain only — wrap in the DataStore factory call
PreferenceDataStoreFactory.createWithPath(
    migrations = listOf(
        SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = "legacy_prefs",
            keysToMigrate = setOf("auth_token", "is_onboarded"),
        )
    ),
    produceFile = { context.filesDir.resolve("app_prefs.preferences_pb").absolutePath.toPath() }
)
```

Migration runs once automatically on first DataStore access. The `SharedPreferences` file
is deleted after a successful migration.

---

## Proto DataStore (when you need schema enforcement)

### Add protobuf plugin to `libs.versions.toml`

```toml
[versions]
protobuf = "4.26.1"
protobuf-plugin = "0.9.4"

[libraries]
datastore-proto = { module = "androidx.datastore:datastore-core", version.ref = "datastore" }
protobuf-kotlin-lite = { module = "com.google.protobuf:protobuf-kotlin-lite", version.ref = "protobuf" }

[plugins]
protobuf = { id = "com.google.protobuf", version.ref = "protobuf-plugin" }
```

### Schema file — `src/commonMain/proto/UserSettings.proto`

```proto
syntax = "proto3";
option java_package = "GROUP_ID.core.datastore";
option java_multiple_files = true;

message UserSettings {
  bool is_onboarded = 1;
  string selected_theme = 2;
  int64 last_sync_ms = 3;
}
```

### Serializer

```kotlin
object UserSettingsSerializer : Serializer<UserSettings> {
    override val defaultValue: UserSettings = UserSettings.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): UserSettings =
        UserSettings.parseFrom(input)
    override suspend fun writeTo(t: UserSettings, output: OutputStream) =
        t.writeTo(output)
}
```

Use Proto DataStore when settings have more than ~5 fields or require atomic multi-field
updates. For simpler needs, Preferences DataStore is always sufficient.

---

## Related Skills

- `kotlin-multiplatform-feature-scaffold` — `:core:datastore` follows the same convention plugin pattern
- `kotlin-multiplatform-dependency-injection` — Koin platform-specific modules for Android vs iOS DataStore factory
- `kotlin-multiplatform-presenter-module` — ViewModels consume `PreferencesDataSource` via constructor injection
- `kotlin-multiplatform-unit-testing` — fake `PreferencesDataSource` for ViewModel tests; no DataStore needed in tests
- `kotlin-multiplatform-expect-actual` — `createDataStore {}` is a canonical expect/actual use case

---

## Common Anti-Patterns

- constructing `DataStore` in a feature module — always create it once in `:core:datastore` and inject via Koin
- calling `dataStore.data.first()` in a ViewModel — blocks the coroutine; use `collect {}` or `stateIn()` instead
- storing large blobs (images, long lists) in DataStore — it is not a database; use SQLDelight for relational or list data
- using `runBlocking` to read DataStore synchronously — always use coroutines; wrap in `runTest` in tests
- one DataStore instance per feature — a single shared DataStore file per app avoids file-lock conflicts
- not handling `IOException` in the `.catch {}` — DataStore can throw on corrupted files; always emit `emptyPreferences()` as fallback

If DataStore writes are silently lost, check that `edit {}` is called inside a coroutine scope
that is not cancelled before the write completes (use `viewModelScope` or a `SupervisorJob` scope).

---

## Output Style

When asked about persisting settings or replacing SharedPreferences in KMP, respond in this order:
1. Preferences vs Proto decision (key-value → Preferences; schema/multi-field → Proto)
2. `AppPreferences` key object + `PreferencesDataSource` interface
3. expect/actual `createDataStore {}` platform factory
4. Koin wiring (platform-specific modules for the factory, common module for the interface binding)
5. ViewModel consumption pattern (inject interface, collect as StateFlow)

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-18 | Initial release. |
