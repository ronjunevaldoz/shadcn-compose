# Step 1 — Library project structure

Part of `kmp-library-publishing`. Load this file when working on: step 1 — library project structure.

---

A KMP library has **no** application plugin. The root module exposes multiplatform targets.

**Clone the official JetBrains starting point first — never hand-write this from
scratch.** `Kotlin/multiplatform-library-template` is the real, actively-maintained
equivalent of `kmp-wizard` for a library (verified against the live repo, not assumed —
"official project" badge, same GitHub org):

```bash
git clone --depth 1 https://github.com/Kotlin/multiplatform-library-template <PROJECT_NAME>
cd <PROJECT_NAME> && rm -rf .git && git init
```

What it gives you out of the box: `vanniktech-mavenPublish`, the AGP 9
`com.android.kotlin.multiplatform.library` plugin, and `jvm()`/`androidLibrary()`/
`iosArm64()`/`iosSimulatorArm64()`/`linuxX64()` targets already wired in one `:library`
module. Add `js()`/`wasmJs()` yourself if `PLATFORMS` includes Web — the template doesn't
include them by default. The template's own README says explicitly what it deliberately
leaves out: binary-compatibility tracking, `explicitApi()`, licensing, and a contribution
guideline — that's exactly what Steps 2/5/12/13 below add on top, the same way this
collection's own 6-layer conventions layer on top of kmp-wizard for an app.

Resulting structure, once this collection's own additions (`library-testing`, `bom`,
`sample`) are layered on. **No `build-logic/`** — the template doesn't ship one, and for
a single `:library` module it adds nothing: there's only one `build.gradle.kts` to
configure, so there's no duplication for a convention plugin to remove. It only earns
its keep once Step 1a's multi-module split is in play — see that section for the real
wiring, not asserted here as a default:

```
my-library/
├── library/                      # Main library module (from the template)
│   └── build.gradle.kts
├── library-testing/              # Test helpers for consumers (optional, added by this skill)
├── bom/                          # Bill of Materials (optional, added by this skill)
├── sample/                       # Sample app that consumes the library (added by this skill)
│   └── build.gradle.kts          # Has com.android.application — only here
├── gradle/
│   └── libs.versions.toml
├── settings.gradle.kts
└── build.gradle.kts              # Root: coordinates + publishing config
```

`settings.gradle.kts` for a library:

```kotlin
rootProject.name = "my-library"

include(":library")
include(":library-testing")    // optional
include(":bom")                // optional
include(":sample:androidApp")  // sample only — no maven-publish applied here
```

For a library small enough to fit in one `:library` module, that's the whole structure.
Once `:library` itself grows past a handful of files covering genuinely separate
concerns, `kmp-clean-architecture`'s 6-layer contract applies to a
library's own internals the same way it does to an app's — `:model`/`:api` split,
`internal` visibility between layers — the difference is only that the *outermost*
public surface is what `explicitApi()`/`apiCheck` above govern, not an app's UI layer.

### Step 1a — Splitting into multiple published modules

Split when a sub-feature has a genuinely independent consumer surface — some consumers
shouldn't have to pull another facet's transitive deps. Not the default, and not just
because the code is "big" (that's what Step 1's internal 6-layer split is for, inside
one module).

Prefix every module and artifact with the library's own `PROJECT_NAME` — never the
literal word "library." This matches the `<PROJECT_NAME>-bom` convention already used
in Step 4 below, and real published multi-module libraries (Coil's `coil-core` /
`coil-compose` / `coil-network`, not `library-core`):

```
<PROJECT_NAME>/
├── <PROJECT_NAME>-core/       # io.github.you:<PROJECT_NAME>-core      — no Compose dep
│   └── build.gradle.kts
├── <PROJECT_NAME>-compose/    # io.github.you:<PROJECT_NAME>-compose   — depends on -core + Compose
│   └── build.gradle.kts
├── <PROJECT_NAME>-testing/    # io.github.you:<PROJECT_NAME>-testing   — fakes/test doubles, depends on -core only
│   └── build.gradle.kts
├── bom/                        # io.github.you:<PROJECT_NAME>-bom       — version-aligns all three
├── sample/
└── settings.gradle.kts
```

```kotlin
// settings.gradle.kts
includeBuild("build-logic")
include(":<PROJECT_NAME>-core")
include(":<PROJECT_NAME>-compose")
include(":<PROJECT_NAME>-testing")
include(":bom")
include(":sample:androidApp")
```

**This is where `build-logic/` actually earns its keep** — three-plus modules that all
need the same `explicitApi()`/AGP/`apiCheck` configuration is real duplication a
convention plugin removes. Single-module libraries (Step 1 above) skip this entirely.

```kotlin
// build-logic/build.gradle.kts
plugins { `kotlin-dsl` }
dependencies {
    compileOnly(libs.plugins.kotlinMultiplatform.get().let { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" })
    compileOnly(libs.plugins.vanniktech.mavenPublish.get().let { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" })
}
gradlePlugin {
    plugins {
        register("libraryModule") {
            id = "<PROJECT_NAME>.library-module"
            implementationClass = "LibraryModuleConventionPlugin"
        }
    }
}
```

```kotlin
// build-logic/src/main/kotlin/LibraryModuleConventionPlugin.kt — the shared config,
// written once, applied to <PROJECT_NAME>-core/-compose/-testing's own build.gradle.kts
class LibraryModuleConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("com.vanniktech.maven.publish")
        extensions.configure<KotlinMultiplatformExtension> {
            explicitApi()
        }
    }
}
```

```kotlin
// <PROJECT_NAME>-core/build.gradle.kts — each module applies the convention plugin,
// then only its own module-specific bits (dependencies, its own coordinates())
plugins {
    id("<PROJECT_NAME>.library-module")
}
```

Dependency direction — one-way, never circular:

```
<PROJECT_NAME>-core  ←  <PROJECT_NAME>-compose
<PROJECT_NAME>-core  ←  <PROJECT_NAME>-testing
```

`-compose` and `-testing` may depend on `-core`; `-core` never depends on either.

Each module is its own `explicitApi()` surface with its own `.api` file — `apiCheck`
runs per module, not once for the whole repo:

```bash
./gradlew :<PROJECT_NAME>-core:apiCheck :<PROJECT_NAME>-compose:apiCheck :<PROJECT_NAME>-testing:apiCheck
```

Each gets its own `mavenPublishing { coordinates(...) }` block with its own artifactId
(`<PROJECT_NAME>-core`, `<PROJECT_NAME>-compose`, ...) — `bom/`'s `constraints` block
(Step 4 below) is what lets a consumer pin all of them to one version via a single BOM
import instead of separate version numbers per artifact.

**When to split vs keep one `:library`:** a genuinely separate consumer surface (core
logic vs a Compose UI layer vs test fakes) that some consumers want without the others'
dependencies. Splitting because it's "organized that way internally" isn't a reason —
that's Step 1's internal 6-layer split, inside one module, no extra published artifacts.

---

