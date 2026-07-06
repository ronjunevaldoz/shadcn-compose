---
name: kotlin-multiplatform-proguard-r8
description: >
  ProGuard and R8 obfuscation for KMP Android builds — keep rules for KMP artifacts,
  Koin, Ktor, SQLDelight, kotlinx.serialization, and Compose. Covers: when to enable
  minification, writing keep rules without breaking KMP shared code, validating the
  release APK, and the most common shrinking crashes in KMP projects.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-29'
  keywords:
    - ProGuard
    - R8
    - obfuscation
    - minification
    - shrinking
    - keep rules
    - proguard-rules.pro
    - release build
    - APK size
    - KMP Android
    - Kotlin Multiplatform
    - kotlinx.serialization keep
    - Koin keep
    - Ktor keep
    - SQLDelight keep
---

## When to Use This Skill

Use when you need to:
- Enable R8 minification for a KMP Android release build
- Debug a release-only crash caused by ProGuard removing or renaming classes
- Write keep rules for KMP artifacts (shared code, Koin modules, Ktor, SQLDelight)
- Audit `proguard-rules.pro` for over-broad or missing rules
- Reduce APK size without breaking runtime behavior

**Trigger keywords:** ProGuard, R8, obfuscation, minification, shrinking, keep rules,
proguard-rules.pro, release build crash, ClassNotFoundException release, NoSuchMethodException
release, APK size, minifyEnabled, shrinkResources, R8 full mode, Koin keep, Ktor keep,
SQLDelight keep, kotlinx.serialization keep.

**Freshness rule:** R8 full mode and AGP default rule sets change between AGP versions —
recheck the AGP and R8 release notes before upgrading past a minor version.

---

## Recommendation First

Default to **enabling R8 with `minifyEnabled = true` only in `release` builds**.
Keep `debug` builds with `minifyEnabled = false` so crashes are stack-traceable during development.

Write **targeted keep rules** for each KMP library used — never use `-keep class ** { *; }`
as a blanket rule. That defeats the purpose of shrinking and hides real rule gaps.

---

## Build Configuration

Enable R8 in the Android app module only — shared KMP modules produce `.jar`/`.klib`,
which are consumed by the app module that controls shrinking:

```kotlin
// androidApp/build.gradle.kts
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true   // remove unused resources too
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            isMinifyEnabled = false    // never shrink debug — stack traces must be readable
        }
    }
}
```

---

## Core Keep Rules by Library

Add these to `androidApp/proguard-rules.pro`. Only include rules for libraries actually used.

### KMP shared module (always needed)

```proguard
# Keep all classes in the shared KMP module — R8 cannot trace expect/actual across boundaries
-keep class com.example.shared.** { *; }

# Keep data classes used in serialization or reflection
-keepclassmembers class com.example.** {
    public synthetic <methods>;
}
```

### kotlinx.serialization

```proguard
# Keep serializers generated at compile time
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class **$$serializer {
    static **$$serializer INSTANCE;
}
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
```

### Koin

```proguard
# Koin uses reflection to instantiate modules — keep module declarations
-keep class org.koin.** { *; }
-keepnames class * extends org.koin.core.module.Module
-keepclassmembers class * {
    @org.koin.core.annotation.* <fields>;
    @org.koin.core.annotation.* <methods>;
}
```

### Ktor

```proguard
# Ktor engines and SSL use reflection on Android
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn io.ktor.**
-dontwarn kotlinx.coroutines.**
```

### SQLDelight

```proguard
# SQLDelight generated Query classes and schema adapters
-keep class com.squareup.sqldelight.** { *; }
-keep class app.cash.sqldelight.** { *; }
-keepclassmembers class ** extends app.cash.sqldelight.Query { *; }
```

### Compose (handled by the Compose compiler plugin — usually no extra rules needed)

```proguard
# Compose stability annotations must survive for the runtime stability system
-keep class androidx.compose.runtime.Stable
-keep class androidx.compose.runtime.Immutable
```

---

## Validating the Release Build

Always test the release build before shipping. R8 crashes are release-only — debug builds
with `isMinifyEnabled = false` will never surface them.

```bash
# Build a release APK
./gradlew :androidApp:assembleRelease

# Install on a device/emulator and smoke-test all screens
# Pay special attention to: serialization, Koin DI startup, Ktor calls, SQLDelight queries

# Check APK size
ls -lh androidApp/build/outputs/apk/release/*.apk

# Deobfuscate a crash from a release stack trace
java -jar retrace.jar \
  androidApp/build/outputs/mapping/release/mapping.txt \
  crash-stacktrace.txt
```

Keep the `mapping.txt` file for every release — it is the only way to deobfuscate
production crash reports. Commit it to a private artifact store (never the public repo).

---

## Diagnosing Release-Only Crashes

The most common causes:

| Symptom | Likely cause | Fix |
|---|---|---|
| `ClassNotFoundException` for a domain class | R8 removed an unreferenced class | Add `-keep class com.example.your.Class` |
| `NoSuchMethodException` | R8 renamed or removed a method accessed by reflection | Add `-keepclassmembers` for that method |
| Koin `NoBeanDefFoundException` in release | Module class was shrunk away | Add `-keep class YourModule` |
| kotlinx.serialization `SerializerNotFoundException` | Serializer companion was removed | Add the serialization keep rules above |
| Ktor `SSLHandshakeException` in release | SSL provider class removed | Add `-keep class io.ktor.**` |
| App crashes immediately on launch | `application` class removed | Verify `android.name` in `AndroidManifest.xml` matches a kept class |

**Debug workflow:**
1. Enable `-printusage usage.txt` and `-printseeds seeds.txt` in the release ProGuard config
2. Build release — check `usage.txt` to see what was removed
3. Add keep rules for anything that shouldn't have been removed
4. Rebuild and re-test

---

## Testing

```kotlin
// Verify serialization survives R8 with an instrumented release test
@RunWith(AndroidJUnit4::class)
class SerializationReleaseTest {

    @Test
    fun userSerializesAndDeserializes() {
        val user = User(id = "1", name = "Alice", email = "alice@example.com")
        val json = Json.encodeToString(user)
        val decoded = Json.decodeFromString<User>(json)
        assertEquals(user, decoded)
    }
}
```

Run instrumented tests against the `release` build type:
```bash
./gradlew :androidApp:connectedReleaseAndroidTest
```

---

## Common Anti-Patterns

- using `-keep class ** { *; }` as a blanket rule — disables all shrinking; fix missing rules per-library instead
- enabling `isMinifyEnabled = true` in debug builds — stack traces become unreadable; always keep debug unobfuscated
- not keeping the `mapping.txt` artifact — production crash reports become undeobfuscatable
- writing keep rules for library internals instead of consuming the library's own consumer rules — most libraries ship `consumer-rules.pro` that AGP applies automatically; check before adding manual rules
- testing only the debug build — release-only crashes caused by R8 are the most common pre-ship bug in KMP Android projects
- not enabling `isShrinkResources` alongside `isMinifyEnabled` — leaves unused resources in the APK

---

## Related Skills

- `kotlin-multiplatform-network-layer` — Ktor rules are required when the network layer is present
- `kotlin-multiplatform-sqldelight-setup` — SQLDelight rules are required when local DB is present
- `kotlin-multiplatform-dependency-injection` — Koin rules are required for all projects using Koin
- `kotlin-multiplatform-release` — R8 config is part of the release checklist

---

## Output Style

When asked about ProGuard or R8, respond in this order:
1. Confirm which libraries are in use (Koin, Ktor, SQLDelight, serialization)
2. Provide the targeted keep rules for each
3. Show how to verify the release build
4. Explain how to deobfuscate a crash if one occurs

Keep rules as targeted as possible — never suggest blanket keep rules.

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-29 | Initial release. |
