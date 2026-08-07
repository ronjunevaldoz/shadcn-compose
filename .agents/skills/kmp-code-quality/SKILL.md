---
name: kmp-code-quality
description: >
  Sets up Ktlint (formatting) and Detekt (code smells + architecture rules) for a KMP project.
  Both run as CI gates. Ktlint is near-zero config. Detekt architecture rules enforce the
  6-layer module boundary contract from kmp-clean-architecture.
license: Apache-2.0
metadata:
  author: kmp-agent-skills
  last-updated: '2026-07-31'
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
    - KDoc
    - comments
    - comment style
    - documentation convention
    - kdoc vs comment
    - extension function documentation
    - documentation by architectural level
    - Android Kotlin style guide
    - naming conventions
    - acronym casing
    - composable naming
    - constant naming
    - backing property
---

## When to Use This Skill

Use when you need to:
- Enforce consistent Kotlin formatting across a KMP project
- Detect architecture violations (`:ui` importing from `:data`) via static analysis
- Wire Ktlint and Detekt as CI gates on every PR
- Configure Detekt architecture rules for the 6-layer module model

**Trigger keywords:** Ktlint, Detekt, code quality, formatting, architecture rules, CI gate,
static analysis, lint, import check, layer violation, code style, KDoc, comment convention,
when to use comments, documentation comment, comment style, kdoc vs line comment,
how to comment kotlin.

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

### Argument list wrapping — inline vs one-per-line

Per [kotlinlang.org's coding conventions](https://kotlinlang.org/docs/coding-conventions.html):
keep a call on one line if it fits; once it doesn't (or the list is long), break after the
opening parenthesis and put every argument on its own line, indented 4 spaces — not a
partial wrap:

```kotlin
// Fits — inline
shift(25, 20)

// Doesn't fit / long list — every argument on its own line
drawSquare(
    x = 10,
    y = 10,
    width = 100,
    height = 100,
    fill = true,
)
```

Ktlint's `standard:argument-list-wrapping` rule enforces this mechanically (all arguments
on one line, or all on separate lines — never a mix), but it **only runs when
`ktlint_code_style = ktlint_official`** is set in `.editorconfig` (or the rule is enabled
explicitly) — this repo's own baseline `.editorconfig` above deliberately doesn't set
`ktlint_code_style`, since switching to `ktlint_official` changes several other rule
defaults too, not just this one. Add `ktlint_code_style = ktlint_official` only if you've
reviewed what else that style turns on; otherwise enable just this rule directly:
`ktlint_standard_argument-list-wrapping = enabled`. It has also had reliability issues in
some ktlint 1.x releases (see [ktlint#2368](https://github.com/pinterest/ktlint/issues/2368))
— verify it actually reformats a deliberately-long call before relying on it in CI.

### Android Studio's formatter is not ktlint

Android Studio's built-in Kotlin code-style scheme implements the same
[Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide) already
referenced above under Naming Conventions — but **Android Studio's IDE formatter and
ktlint are two separate tools with separate configuration**, and setting one does not
configure the other. A project can have Android Studio's "Reformat Code" produce output
that ktlint's `ktlintCheck` then fails, or vice versa, if only one side is configured.
Set both explicitly rather than assuming IDE-formatted code will pass CI:
- Android Studio: **Preferences > Editor > Code Style > Kotlin**, set the scheme
- ktlint: `.editorconfig`'s `ktlint_code_style` and `ktlint_standard_*` keys, as above

### Usage

```bash
# Format all files
./gradlew ktlintFormat

# Check (CI — fails on violations)
./gradlew ktlintCheck
```

---

## Detekt Setup

Full content: `references/detekt-setup.md`.

## Compiler Warnings

Ktlint and Detekt are both *static analysis* — neither invokes the real Kotlin
compiler. A deprecated API call, an unchecked generic cast, an unused parameter the
compiler itself flags, or a missing `@OptIn` only ever surfaces in `./gradlew build`
output — and nothing in the Ktlint/Detekt pipeline above sees it. Android Studio shows
these live while editing, which is why they're easy to assume are "covered" when
they're actually invisible to CI unless wired separately.

### Turn warnings into build failures — opt-in, not a day-one default

```kotlin
// build-logic convention plugin, or root build.gradle.kts
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    compilerOptions {
        allWarningsAsErrors.set(true)
    }
}
```

Gate this behind a real decision, not a blanket default: turning it on mid-project, on a
codebase that already has accumulated warnings, is a big-bang change that blocks every
build until every existing warning is fixed. Enable it once a project is warning-clean,
or scope it to new/actively-developed modules first via `subprojects` filtering rather
than the whole build at once.

### Surface warnings in CI even without the hard gate

If `allWarningsAsErrors` isn't turned on yet, still make warnings visible instead of
silently swallowed in build logs nobody reads — `kmp-ci-github-actions`'s
Step 3 wires `./gradlew build` to run with `--warning-mode all` and greps compiler
warning lines into the job summary, so warnings show up in the PR check even before
they're a hard gate.

---

## Naming Conventions (Android Kotlin Style Guide)

Ktlint/Detekt above enforce *formatting* mechanically. They do not enforce naming
*semantics* — whether an acronym is cased right, whether a `val` actually qualifies as a
constant, whether a `@Composable` reads as a type or a verb. Verified against the real,
current [Android Kotlin style guide](https://developer.android.com/kotlin/style-guide)
(Google's official doc, last updated 2023-09-06), not assumed:

### File and package naming

- A file with one top-level class is named exactly after that class, case-sensitive —
  `AuthViewModel.kt`, never `authviewmodel.kt` or `AuthVM.kt`
- A file with multiple top-level declarations (extension functions, several small types)
  gets a descriptive PascalCase name — `StringExtensions.kt`, `NetworkResult.kt`
- Package names are all lowercase, words concatenated with no underscores —
  `GROUP_ID.feature.auth.presenter`, never `GROUP_ID.feature.auth_flow`

### Type, function, and constant names

| Kind | Case | Notes |
|---|---|---|
| Class / interface / object | `PascalCase` | Nouns or noun phrases (`AuthRepository`); interfaces may be adjectives too (`Readable`) |
| Test class | `PascalCase` + `Test` | `AuthViewModelTest`, `AuthRepositoryIntegrationTest` |
| Function | `camelCase` | Verb or verb phrase — `sendMessage`, `refreshToken` |
| Test function | `camelCase`, underscores allowed | `` `pop_emptyStack`` — underscores separate logical components, test names only |
| **`@Composable` function returning `Unit`** | **`PascalCase`, noun** | Read as a type, not a verb — `AppButton`, `ProductListScreen`. **Not** `appButton`/`renderProductList` |
| `@Composable` function returning a value | `camelCase` | A factory, not a UI node — `rememberScrollState()`, not `RememberScrollState()` |
| Constant (`const val`, or a `val` with no custom getter and deeply immutable contents) | `UPPER_SNAKE_CASE` | Only legal in an `object` or at top level — a `class`'s own property can't be a "constant" by this definition, even if it never changes; use `camelCase` there instead |
| Backing property | `_` + real property name | `private var _table: Map<...>?` backing `val table: Map<...>` |
| Type variable | Single capital + optional numeral, or `NameT` | `T`, `E`, `T2`, or `RequestT` |

The `@Composable` PascalCase rule is the one most relevant to this collection's own
generated code — every `App*`/`Shadcn*` component already follows it by convention; this
is the first place it's stated as an explicit, checkable naming rule rather than an
implicit pattern. `kmp-audit`'s `_detect_lowercase_unit_composable`
mechanically enforces it.

### Acronym casing

The style guide's camelCase conversion process lowercases an acronym's letters except
the first, same as any other word — never keep an acronym fully capitalized:

| Prose | Correct | Incorrect |
|---|---|---|
| "XML Http Request" | `XmlHttpRequest` | `XMLHTTPRequest` |
| "new customer ID" | `newCustomerId` | `newCustomerID` |
| "supports IPv6 on iOS" | `supportsIpv6OnIos` | `supportsIPv6OnIOS` |

### A known, deliberate deviation: line length

The Android guide sets a 100-character column limit. This repo's own
`.editorconfig` (see Ktlint Setup above) sets `max_line_length = 120`, matching
[kotlinlang.org's own Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
recommendation instead. This is a real, acknowledged conflict between the two official
sources, not an oversight — 120 stays the default here; a project that wants strict
Android-guide alignment should set `max_line_length = 100` in its own `.editorconfig`.

---

## Comment & KDoc Conventions

Full content: `references/comment-kdoc-conventions.md`.

## Side-Effect-Free Accessors (Destructive Reads)

A getter/consume function must never mutate shared state as a side effect of being
called. If more than one caller can read it, whichever caller runs second silently sees
the already-cleared value — no exception thrown, no compile error, just a dropped event.

```kotlin
// ❌ destructive read — 2nd caller in the same tick/request gets the empty value
class Input {
    private val typedText = StringBuilder()
    fun consumeTypedText(): String {
        val value = typedText.toString()
        typedText.clear()      // side effect buried inside a read
        return value
    }
}
```

```kotlin
// ✓ single owned snapshot — one call site clears, every reader gets the same value
class Input {
    private val typedText = StringBuilder()
    fun snapshot(): InputSnapshot {
        val captured = InputSnapshot(typedText = typedText.toString(), /* ... */)
        typedText.clear()      // cleared once, at the frame/request boundary that owns it
        return captured
    }
}
```

The fix generalizes past input handling — any shared mutable state with more than one
reader has the same failure shape:

```kotlin
// ❌ two ViewModels (badge icon + notification screen) both call this — whichever
// runs second sees 0 instead of the real count
class NotificationRepository {
    private var unreadCount = 0
    fun consumeUnreadCount(): Int {
        val v = unreadCount
        unreadCount = 0
        return v
    }
}

// ✓ readers observe a StateFlow; clearing is an explicit, separately-named action
// called from exactly one place
class NotificationRepository {
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()
    fun markAllRead() { _unreadCount.value = 0 }
}
```

**Rule:** if state needs to be cleared or drained, expose that as a separate,
explicitly-named action (`markAllRead()`, `clear()`, `reset()`, or a single owned
`snapshot()`) called from exactly one place — never bury the clear inside a method every
consumer calls just to read the value. Mechanically checked by `kmp-audit`'s
`_detect_destructive_read_accessor` — a heuristic match on the "read field into a local,
clear that same field, return the local" 3-line shape.

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

## References

Full implementation content lives in `references/*.md`: `detekt-setup`,
`comment-kdoc-conventions`, `kotlin-library-pattern-choices`, `changelog`. Load the
specific file named in the pointer under its matching heading above, not all of them.

---

## Related Skills

- `kmp-clean-architecture` — defines the layer rules that Detekt enforces
- `kmp-ci-github-actions` — the CI workflow where these gates run
- `kmp-feature-scaffold` — convention plugins are where Ktlint/Detekt are applied
- `kmp-project-docs-maintainer` — `docs/reference/` is where development notes go when a code comment's rationale outgrows what belongs inline
- `kmp-library-publishing` — per-file license headers, a related but separate comment-placement decision
- `kmp-audit` — `_detect_what_comment_in_control_flow` checks the "Inline blocks" rule below automatically; `/kmp-clean-comments` applies the fix across all four documentation levels; `_detect_destructive_read_accessor` checks the "Side-Effect-Free Accessors" rule; `_detect_god_class` is the non-Detekt backstop for `LargeClass`/`TooManyFunctions` above
- `kmp-docs-site` — applies this skill's `@sample` principle (a real, compiled reference beats a pasted block that can drift stale) to public developer-guide code examples via snippet extraction

---

## Common Anti-Patterns

- applying Detekt only to the root project — violations in submodules go undetected; apply via convention plugins
- leaving `LargeClass`/`TooManyFunctions` unconfigured — god-object detection then only exists for ViewModels and Composables (via `kmp-audit`), not for a repository/use-case/manager class accumulating too many responsibilities
- a wrapper function re-exploding an existing parameter object into individual primitives instead of accepting and forwarding the object — Primitive Obsession compounding Long Parameter List; the fix already exists one call away and got undone
- setting `maxIssues > 0` — a non-zero threshold lets violations accumulate silently
- using Ktlint without `.editorconfig` — line length defaults to 80; too short for Kotlin
- running `ktlintFormat` in CI instead of `ktlintCheck` — CI should fail, not silently reformat
- excluding the `:presenter` module from `NoComposeInPresenter` — the rule only matters if applied to presenter
- using `//` to document a public API's contract instead of KDoc — Dokka and IDE quick-docs never see a `//` comment
- adding KDoc to a private member to explain unclear behavior — rename the member instead; flagged by Detekt's `DocumentationOverPrivateFunction`/`DocumentationOverPrivateProperty`
- placing a `//` comment inside a function call's argument list before its closing `)`/`{` — silently comments out the rest of the line; this exact bug shipped in `kmp-imagevector-generator`'s own codegen
- writing a multi-paragraph inline comment that mixes "why this code exists" with mechanism detail, rejected alternatives, and exact version numbers — split it: the short WHY stays inline, the exhaustive rationale goes in `docs/reference/` with a one-line pointer left in the comment
- documenting an extension function's return value without stating the receiver scope or precondition it assumes — callers can't tell when it's safe to call versus when to reach for the member function instead
- a `consume*()`/getter that clears the field it just read before returning — fine with one caller, silently drops data for every other caller reading the same accessor in the same tick/request; expose a single owned `snapshot()`/`markAllRead()` instead

If Detekt reports false positives, use `@Suppress("RuleName")` at the call site, not a global exclude.

---

## Kotlin Library & Pattern Choices

Full content: `references/kotlin-library-pattern-choices.md`.

## Output Style

When asked about code quality, linting, or formatting for KMP, respond in this order:
1. Ktlint setup (plugin version, `.editorconfig`, `ktlintCheck` command)
2. Detekt setup (plugin, `detekt.yml`, architecture rules for the 6-layer model)
3. CI job snippet
4. which tool enforces what (table)
5. how to fix violations locally before pushing

---

## Changelog

Full content: `references/changelog.md`.

