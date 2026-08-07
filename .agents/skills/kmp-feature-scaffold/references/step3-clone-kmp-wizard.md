# Step 3: New Project — Clone kmp-wizard (MANDATORY)

Part of `kmp-feature-scaffold`. Load this file when working on: step 3: new project — clone kmp-wizard (mandatory).

---

> **Never create build infrastructure by hand.** Always start from the official
> `Kotlin/kmp-wizard` repository. Hand-writing `build-logic`, convention plugins, or
> `settings.gradle.kts` from scratch leads to misconfigured Gradle included builds,
> broken precompiled script plugin accessor generation, and missing platform targets.
> The wizard gives you a known-good baseline; your job is to configure and extend it.

### 3a. Clone the baseline

```bash
# Default: all platforms (Android + iOS + Desktop + Web + Server)
git clone --depth 1 --branch all-targets \
  https://github.com/Kotlin/kmp-wizard <PROJECT_NAME>

# Frontend-only (no server module):
git clone --depth 1 --branch all-frontends-shared \
  https://github.com/Kotlin/kmp-wizard <PROJECT_NAME>

cd <PROJECT_NAME>
rm -rf .git          # detach from kmp-wizard history
git init             # start fresh project history
```

Choose `all-targets` by default. Use `all-frontends-shared` only when the project
explicitly excludes a server module.

### 3b. Configure the clone

After cloning, make these targeted edits — do not rewrite the files:

**`settings.gradle.kts`** — update the root project name:
```kotlin
rootProject.name = "PROJECT_NAME"
```

**`gradle/libs.versions.toml`** — update to the target versions from Step 2:
```toml
agp                   = "9.2.0"
kotlin                = "2.4.0"
compose-multiplatform = "1.11.1"
# … update all version entries to match Step 2 table
```

**`build-logic/convention/src/main/kotlin/`** — rename every convention plugin file
by substituting the wizard's placeholder group ID with `GROUP_ID`:
```bash
# Example: if kmp-wizard uses "org.example" as placeholder
for f in build-logic/convention/src/main/kotlin/*.kt; do
  mv "$f" "${f/org.example/GROUP_ID}"
done
# Then update the group ID string inside each file
find build-logic/convention/src/main/kotlin -name "*.kt" \
  -exec sed -i '' 's/org\.example/GROUP_ID/g' {} +
```

**`app/androidApp/build.gradle.kts`** and any `applicationId` occurrences — replace
the wizard placeholder with `GROUP_ID`.

### 3c. Verify the base builds

Run this before adding any modules:

```bash
./gradlew help
```

`BUILD SUCCESSFUL` means the base is sound. Fix any version resolution errors
before proceeding. Do not add feature modules to a broken base.

### 3d. Fix kmp-wizard's known scaffold gaps before adding anything

The `all-targets` clone's real module map (verified against the live template):

```
:app:androidApp / :app:desktopApp / :app:webApp  — thin per-platform entry points
app/iosApp/                                       — native Xcode project, not a Gradle module
:app:shared                                       — CMP composition root (depends on :core)
:core                                             — ships as ONE bare module
:server                                           — present only on the all-targets branch
```

Two things must be fixed before Step 4's convention plugins are added, or the project
fails this collection's own audit on its first run:

1. **`:core` ships bare.** Rename it to `:core:common` — update its
   `settings.gradle.kts` include and `app/shared/build.gradle.kts`'s
   `implementation(project(":core"))` to `project(":core:common")`. This is exactly the
   `bare core module [HIGH]` finding `kmp-audit`'s
   `_detect_bare_core_module` flags — fix it now, don't scaffold on top of it. Add
   `:core:network`/`:core:database`/`:core:ui`/`:core:testing` later, on demand, not all
   at scaffold time.
2. **`:app:shared` ships with demo placeholder content.** Delete kmp-wizard's default
   Greeting/counter sample. Keep `:app:shared` as a **composition root only** — the
   top-level `App()` composable, theme wrapper, `startKoin {}`, and a `NavHost`
   referencing `:feature:*:ui` screens. Feature logic, data access, and feature-specific
   UI never live in `:app:shared` — see "The `:app:*` Module Boundary" below.

---

