---
name: kotlin-multiplatform-code-quality
description: >
  Sets up Ktlint (formatting) and Detekt (code smells + architecture rules) for a KMP project.
  Both run as CI gates. Ktlint is near-zero config. Detekt architecture rules enforce the
  6-layer module boundary contract from kotlin-multiplatform-clean-architecture.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-18'
  keywords:
    - Ktlint
    - Detekt
    - code quality
    - formatting
    - architecture rules
    - CI gate
    - KMP
    - Kotlin Multiplatform
    - lint
    - static analysis
---

## When to Use This Skill

Use when you need to:
- Enforce consistent Kotlin formatting across a KMP project
- Detect architecture violations (`:ui` importing from `:data`) via static analysis
- Wire Ktlint and Detekt as CI gates on every PR
- Configure Detekt architecture rules for the 6-layer module model

**Trigger keywords:** Ktlint, Detekt, code quality, formatting, architecture rules, CI gate,
static analysis, lint, import check, layer violation, code style.

**Freshness rule:** Ktlint and Detekt versions change frequently — recheck the latest releases
before pinning. Detekt architecture rule DSL changes between minor versions.

---

## Recommendation First

**Install both. They solve different problems.**

| Tool | Enforces | Config effort | When to run |
|---|---|---|---|
| Ktlint | Formatting — indentation, imports, line length | Near-zero | `ktlintFormat` before commit; `ktlintCheck` in CI |
| Detekt | Code smells + architecture rules | Medium | `detekt` in CI |

Ktlint is the easier win — add it first. Detekt's architecture rule set is the more powerful
tool for catching layer violations that Gradle dependency declarations miss (import-level coupling).

---

## Ktlint Setup

### `libs.versions.toml`

```toml
[versions]
ktlint = "12.1.1"

[plugins]
ktlint = { id = "org.jlleitschuh.gradle.ktlint", version.ref = "ktlint" }
```

### Root `build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.ktlint) apply false
}
```

### `build-logic/convention/build.gradle.kts`

```kotlin
dependencies {
    implementation(libs.plugins.ktlint.get().let { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" })
}
```

### Convention plugin — add to `GROUP_ID.core.gradle.kts` and feature plugins

```kotlin
plugins {
    // ... existing plugins ...
    id("org.jlleitschuh.gradle.ktlint")
}

ktlint {
    version = "1.3.1"         // Ktlint engine version (separate from Gradle plugin version)
    android = false           // KMP modules are not Android-only
    outputToConsole = true
    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
    }
}
```

### `.editorconfig` (project root)

```ini
[*.{kt,kts}]
max_line_length = 120
ktlint_standard_no-wildcard-imports = disabled
ktlint_standard_import-ordering = disabled
```

### Usage

```bash
# Format all files
./gradlew ktlintFormat

# Check (CI — fails on violations)
./gradlew ktlintCheck
```

---

## Detekt Setup

### `libs.versions.toml`

```toml
[versions]
detekt = "1.23.7"

[libraries]
detekt-formatting = { module = "io.gitlab.arturbosch.detekt:detekt-formatting", version.ref = "detekt" }

[plugins]
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
```

### Root `build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.detekt) apply false
}
```

### Convention plugin — add to all feature plugins

```kotlin
plugins {
    // ... existing plugins ...
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    config.setFrom(rootProject.file("detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
}

dependencies {
    detektPlugins(libs.detekt.formatting)
}
```

### Root `detekt.yml`

```yaml
build:
  maxIssues: 0

complexity:
  LongMethod:
    active: true
    threshold: 60
  LongParameterList:
    active: true
    functionThreshold: 6
    constructorThreshold: 7
  CyclomaticComplexMethod:
    active: true
    threshold: 15

naming:
  FunctionNaming:
    active: true
    excludes: ['**/test/**', '**/*Test.kt', '**/*Preview*']

libraries:
  rules:
    - name: 'NoComposeInPresenter'
      active: true
      includes: ['**/presenter/**']
      forbidden:
        - 'androidx.compose.*'
        - 'org.jetbrains.compose.*'

    - name: 'NoDataInUi'
      active: true
      includes: ['**/ui/**']
      forbidden:
        - '*.data.*'
        - 'io.ktor.*'
        - 'app.cash.sqldelight.*'

    - name: 'NoDomainInUi'
      active: true
      includes: ['**/ui/**']
      forbidden:
        - '*.domain.*'
```

### Usage

```bash
# Run Detekt (fails on violations)
./gradlew detekt

# Generate HTML report
./gradlew detekt --report html:build/reports/detekt/detekt.html

# Fix auto-fixable issues (formatting only)
./gradlew detektFormat
```

---

## CI Integration

Add to `.github/workflows/ci.yml` lint job:

```yaml
lint:
  name: Lint
  runs-on: ubuntu-latest
  steps:
    - uses: actions/checkout@v4

    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'zulu'

    - name: Setup Gradle
      uses: gradle/actions/setup-gradle@v4
      with:
        cache-encryption-key: ${{ secrets.GRADLE_ENCRYPTION_KEY }}

    - name: Ktlint check
      run: ./gradlew ktlintCheck

    - name: Detekt
      run: ./gradlew detekt

    - name: Upload Detekt report
      if: failure()
      uses: actions/upload-artifact@v4
      with:
        name: detekt-report
        path: '**/build/reports/detekt/**'
```

---

## Related Skills

- `kotlin-multiplatform-clean-architecture` — defines the layer rules that Detekt enforces
- `kotlin-multiplatform-ci-github-actions` — the CI workflow where these gates run
- `kotlin-multiplatform-feature-scaffold` — convention plugins are where Ktlint/Detekt are applied

---

## Common Anti-Patterns

- applying Detekt only to the root project — violations in submodules go undetected; apply via convention plugins
- setting `maxIssues > 0` — a non-zero threshold lets violations accumulate silently
- using Ktlint without `.editorconfig` — line length defaults to 80; too short for Kotlin
- running `ktlintFormat` in CI instead of `ktlintCheck` — CI should fail, not silently reformat
- excluding the `:presenter` module from `NoComposeInPresenter` — the rule only matters if applied to presenter

If Detekt reports false positives, use `@Suppress("RuleName")` at the call site, not a global exclude.

---

## Output Style

When asked about code quality, linting, or formatting for KMP, respond in this order:
1. Ktlint setup (plugin version, `.editorconfig`, `ktlintCheck` command)
2. Detekt setup (plugin, `detekt.yml`, architecture rules for the 6-layer model)
3. CI job snippet
4. which tool enforces what (table)
5. how to fix violations locally before pushing

---

## Changelog

| Date | Change |
|---|---|
| 2026-06-18 | Initial release. |
