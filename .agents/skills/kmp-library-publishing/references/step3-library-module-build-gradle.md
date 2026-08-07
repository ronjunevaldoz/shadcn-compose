# Step 3 — Library module build.gradle.kts

Part of `kmp-library-publishing`. Load this file when working on: step 3 — library module build.gradle.kts.

---

```kotlin
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)       // only if targeting Android
    alias(libs.plugins.vanniktech.publish)
    alias(libs.plugins.dokka)
}

kotlin {
    explicitApi()   // library-only — forces every public declaration to state its
                    // visibility and return type explicitly, see below

    androidTarget {
        publishLibraryVariants("release")
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    jvm()
    js(IR) { browser(); nodejs() }
    wasmJs { browser() }
    linuxX64()

    sourceSets {
        commonMain.dependencies {
            // shared dependencies
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)  // use OSSRH for legacy accounts

    signAllPublications()   // requires GPG key in env (see Step 6)

    coordinates(
        groupId    = "io.github.yourhandle",
        artifactId = "my-library",
        version    = version.toString(),   // read from gradle.properties
    )

    pom {
        name = "My Library"
        description = "A concise description of what the library does."
        url = "https://github.com/yourhandle/my-library"
        inceptionYear = "2024"

        licenses {
            license {
                name = "Apache-2.0"
                url  = "https://www.apache.org/licenses/LICENSE-2.0"
            }
        }

        developers {
            developer {
                id   = "yourhandle"
                name = "Your Name"
                url  = "https://github.com/yourhandle"
            }
        }

        scm {
            url                 = "https://github.com/yourhandle/my-library"
            connection          = "scm:git:git://github.com/yourhandle/my-library.git"
            developerConnection = "scm:git:ssh://git@github.com/yourhandle/my-library.git"
        }
    }
}
```

`gradle.properties` (version managed here, not in build script):

```properties
GROUP=io.github.yourhandle
POM_ARTIFACT_ID=my-library
VERSION_NAME=0.1.0-SNAPSHOT
```

Start at `0.1.0`, not `1.0.0` — a fresh library has had zero real consumer usage yet, and
`1.0.0` is a stability promise this skill's own pre-1.0 policy (below) says to make
deliberately, not on the first commit. `Kotlin/multiplatform-library-template` (Step 1's
starting clone) hardcodes `version = "1.0.0"` in its own `build.gradle.kts` — change it
to `0.1.0` as part of the same configuration pass that sets `GROUP_ID`/coordinates, don't
leave the template's default in place.

### Per-file license headers (optional)

The POM `licenses { license { ... } }` block above is the legally required, project-level
license declaration for Maven Central — that's not optional. A per-file license header
comment repeated at the top of every `.kt` source file is a **separate, optional** choice,
worth it for a library specifically because each file may be vendored, copy-pasted, or
inspected independently of the repo it came from; a project-level `LICENSE` file alone
doesn't travel with an individual file once it's copied elsewhere. It's not needed for
app code (see `kmp-code-quality`'s Comment & KDoc Conventions).

Enforce it with Detekt's `AbsentOrWrongFileLicense` rule (off by default):

```yaml
# detekt.yml
comments:
  AbsentOrWrongFileLicense:
    active: true
    licenseTemplateFile: 'license.template.txt'
```

```
# license.template.txt
/*
 * Copyright 2026 Your Name or Org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
```

Keep the license identifier here consistent with the POM's `licenses { license { name = ... } }`
block — a per-file header claiming a different license than the POM declares is worse
than having no per-file header at all.

### `explicitApi()` — library-only, not for app code

Library code has a wider blast radius than app code: a `public` declaration nobody
intended to expose becomes part of the API surface the moment it ships, and removing it
later is a breaking change (exactly what `apiCheck`/binary-compatibility-validator in
Step 5 exists to catch after the fact). `explicitApi()` catches it *before* publishing
instead — it fails the build on any public declaration missing an explicit visibility
modifier or return type:

```kotlin
// ❌ fails to compile under explicitApi() — implicit public visibility, inferred return type
class UserRepository {
    fun getUser(id: String) = api.fetchUser(id)
}

// ✓ compiles — visibility and return type both explicit
public class UserRepository {
    public fun getUser(id: String): User = api.fetchUser(id)
}
```

**Don't add this to app code** — `kmp-clean-architecture`'s 6-layer
contract already controls what's exposed between modules via `internal`, and an app has
no external consumers to protect from an accidental public leak the way a published
library does. `explicitApi()` on app code is pure ceremony with no corresponding benefit.

Two modes: `explicitApi()` fails the build (`ExplicitApiMode.Strict`), `explicitApiWarning()`
only warns. Use the strict form for a library that's already past its first stable
release — a warning is easy to ignore and defeats the point of catching this before
publishing.

### No forced framework coupling in library internals

`kmp-dependency-injection` recommends Koin for **app code** — a
published library is a different situation. A consumer app might use Koin, Hilt, manual
DI, or nothing at all; a library that hard-imports `org.koin.*` inside its own public
classes forces that choice onto every consumer, or worse, silently requires Koin to be
on the consumer's classpath at all.

```kotlin
// ❌ forces Koin onto every consumer of this library
class UserRepository(scope: Scope) : KoinComponent {
    private val api: ApiClient by inject()
}

// ✓ plain constructor injection — the consumer wires it however they want
public class UserRepository(private val api: ApiClient) {
    // ...
}
```

If the library wants to *offer* Koin wiring as a convenience, ship it as a separate,
optional artifact (`my-library-koin`) with its own `module { }` — never bake the
dependency into the core artifact's own classes.

### KDoc coverage on the public API surface

`kmp-code-quality`'s Comment & KDoc Conventions section covers KDoc
*style* — this is about *coverage*. Once `explicitApi()` forces every public declaration
to be deliberate, an undocumented one is a real gap: a consumer sees the declaration in
autocomplete with no explanation of what it does or when to use it.

```kotlin
// ❌ compiles under explicitApi(), but a consumer has no idea what this does
public class RetryPolicy(public val maxAttempts: Int, public val backoffMs: Long)

// ✓ the public contract is documented, not just the visibility
/**
 * Controls retry behavior for transient network failures.
 * @property maxAttempts stop retrying after this many attempts, including the first
 * @property backoffMs delay between attempts, doubled after each failure
 */
public class RetryPolicy(public val maxAttempts: Int, public val backoffMs: Long)
```

`kmp-audit`'s `_detect_undocumented_public_api` flags a public
declaration with no preceding KDoc block, but only in a project that already uses
`explicitApi()` — without it, "public" isn't a deliberate signal worth checking.

---

