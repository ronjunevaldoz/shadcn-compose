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

**Freshness rule:** pinned to the 1.23.x stable line below. Detekt's 2.0.0-alpha.x line is
in active development on [detekt.dev](https://detekt.dev) — recheck before migrating, since
rule-set names and config keys can change between the 1.x and 2.x major lines.

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

style:
  AbstractClassCanBeInterface:
    active: true

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
  LargeClass:
    active: true
    threshold: 400
  TooManyFunctions:
    active: true
    thresholdInClasses: 15
  NamedArguments:
    active: true
    allowedArguments: 3
    ignoreArgumentsMatchingNames: true

performance:
  ArrayPrimitive:
    active: true
  CouldBeSequence:
    active: true
  ForEachOnRange:
    active: true
  SpreadOperator:
    active: true
  UnnecessaryTemporaryInstantiation:
    active: true
  UnnecessaryPartOfBinaryExpression:
    active: true
  UnnecessaryTypeCasting:
    active: true
  UnnecessaryInitOnArray:
    active: true

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

`AbstractClassCanBeInterface` (**correction, 2026-08-04**: this section previously
named it `UnnecessaryAbstractClass` — verified directly against Detekt's own
`default-detekt-config.yml` on GitHub and that name doesn't exist; the real rule
covering this exact case is `AbstractClassCanBeInterface`, active by default) matters
more in KMP than in a single-platform codebase: an abstract class with only abstract
members in `commonMain` forces every consumer into an inheritance chain, which is
exactly the pattern `kmp-clean-architecture`'s "Composition Over Inheritance" section
explains how to avoid — see that section for the full rationale and fix.

`LargeClass`/`TooManyFunctions` were a real gap: this collection had god-object
detection scoped to only two file types (`kmp-audit`'s `_detect_viewmodel_size` for
ViewModels, `_detect_god_composable` for Compose screens) — nothing caught a
repository, use case, or manager class accumulating too many responsibilities. These
two are real, AST-based Detekt rules (verified against Detekt's own
`default-detekt-config.yml`, not assumed), so they catch it precisely instead of
approximately:
- `LargeClass` — a class over 400 lines, tuned above `LongMethod`'s 60-line function
  threshold since a class legitimately holding several medium methods is normal; the
  smell is the *class* growing unbounded, not any one method
- `TooManyFunctions` — 15+ functions in one class is usually multiple responsibilities
  that haven't been split yet

**Correction (2026-08-03):** this section previously also listed a `coupling:
CouplingBetweenObjects` config as a third "real, AST-based Detekt rule" — verified
directly against Detekt's own `default-detekt-config.yml` on GitHub and it does not
exist. There is no `coupling:` rule set and no `CouplingBetweenObjects` rule in Detekt
— that's a PMD (Java) rule concept, not a Detekt one, and was fabricated into this
skill in error. Removed the config block; there is currently no direct Detekt
equivalent for cross-class fan-out/coupling. `kmp-audit`'s `_detect_god_class` is the
only signal for that specific concern — a heuristic, not an AST-based rule; see that
skill's own docs for its thresholds.

### NamedArguments — requiring names, without requiring redundant ones

`NamedArguments` (complexity ruleset, `active: false` by default — verified against
Detekt's own source) requires named arguments once a call passes `allowedArguments`
(3 here, matching Detekt's own default). Without `ignoreArgumentsMatchingNames: true`,
this forces exactly the noise the rule is supposed to prevent:

```kotlin
// allowedArguments: 3, ignoreArgumentsMatchingNames: false (Detekt's default)
User(id = id, name = name, age = age, email = email)   // passes, but "id = id" adds nothing

// ignoreArgumentsMatchingNames: true (this repo's recommended config)
User(id, name, age, email)   // passes too — every value's identifier already IS its param name
```

The rule still requires a name wherever it would carry real information — i.e. the
argument's value expression differs from the parameter name:

```kotlin
val userId = "u1"
User(userId, name, age, email)          // flagged — userId != id, naming isn't redundant
User(id = userId, name = name, age = age, email = email)   // fix
```

Real, filed issue for this exact behavior:
[detekt#4591](https://github.com/detekt/detekt/issues/4591), resolved via
[#4613](https://github.com/detekt/detekt/pull/4613).

### Long Parameter List, and its worse variant: a regressed Parameter Object

`LongParameterList` (Fowler's *Refactoring* catalog name — Detekt's rule is named the
same thing) flags any function past the configured threshold. The standard fix is
**Introduce Parameter Object**: group related parameters into one data class.

There's a worse version of this smell than a plain long list: a function that *already
has* a parameter object one call away, and re-explodes it into individual primitives
anyway instead of accepting and forwarding the object directly.

```kotlin
// ❌ UiLayoutTracking already exists and is what layouts.createColumn actually wants —
// this wrapper flattens it into 4 primitives just to reconstruct it one line later
fun createColumn(
    x: Float, y: Float, width: Float, height: Float? = null,
    verticalArrangement: Arrangement = defaultArrangement(),
    testTag: String? = null,
    hasBoundedFillWidth: Boolean = true,
    hasBoundedFillHeight: Boolean = height != null,
    overlayOnly: Boolean = false,
    plannedSlots: List<UiSlot>? = null,
    horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
): ColumnScope = layouts.createColumn(
    x = x, y = y, width = width, height = height,
    verticalArrangement = verticalArrangement,
    tracking = UiLayoutTracking(testTag, hasBoundedFillWidth, hasBoundedFillHeight, overlayOnly),
    plannedSlots = plannedSlots,
    horizontalAlignment = horizontalAlignment,
)

// ✓ accept and forward the object that already exists — 11 params drop to 8,
// and a future field added to UiLayoutTracking can't drift out of sync with this
// wrapper's signature, because the wrapper no longer restates its shape
fun createColumn(
    x: Float, y: Float, width: Float, height: Float? = null,
    verticalArrangement: Arrangement = defaultArrangement(),
    tracking: UiLayoutTracking = UiLayoutTracking(),
    plannedSlots: List<UiSlot>? = null,
    horizontalAlignment: UiAlignment.Horizontal = UiAlignment.Horizontal.Start,
): ColumnScope = layouts.createColumn(
    x = x, y = y, width = width, height = height,
    verticalArrangement = verticalArrangement,
    tracking = tracking,
    plannedSlots = plannedSlots,
    horizontalAlignment = horizontalAlignment,
)
```

This compounds **Primitive Obsession** (Fowler again — using primitives instead of a
small object for something that already has one) onto Long Parameter List: the smell
isn't that nobody solved it, it's that the solution was undone one layer up. Not
mechanically detected here — distinguishing "this wrapper's params happen to overlap a
nearby class" from "this wrapper is literally reconstructing that exact class" needs more
context than a regex reasonably gets right without false positives. Catch it in review:
if a function's parameter list, minus 1-2 params, matches a data class used in its own
body, forward the object instead of restating its fields.

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

Kotlin-specific — for the C++/CPP side of a JNI or Kotlin/Native bridge (header
declaration vs `.cpp`/`.mm` implementation comments), see `kmp-native-authoring`'s
"Header vs implementation comments" section. Same underlying principle (declaration =
what a caller needs, implementation = what a maintainer needs), different syntax.

### Whether to write a comment at all

Ask in this order — stop at the first "yes":

1. **Does removing it lose zero information?** (the code/naming already says it) — don't
   write it. This is the single most common comment mistake: narrating WHAT instead of
   explaining WHY.
2. **Is it a public API contract another module or consumer relies on** (parameters,
   return value, thrown errors, a receiver precondition)? — KDoc `/** */`.
3. **Is it a non-obvious WHY** — a workaround, a constraint from outside this file, a
   reason simplifying this would break something? — single-line `//`.
4. Otherwise — don't write it. A comment that answers neither "what's the contract"
   nor "why is this not the obvious way" isn't pulling its weight.

Two comment types, two jobs — never mix them:

| | Single-line `//` | Multi-line `/** ... */` (KDoc) |
|---|---|---|
| Documents | Internal WHY — a workaround, a non-obvious constraint | Public API contract — `@param`/`@return`/`@throws`/`@sample` |
| Never used for | Restating WHAT the code does (good naming covers that) | Private members — rename instead (Detekt's `DocumentationOverPrivateFunction`/`Property` flags this) |
| Visible to | Nobody outside the source file | Dokka + IDE quick-docs |
| Grows past ~4 lines? | Split: keep the one-sentence WHY inline, move the rest to `docs/reference/` with a pointer comment (see below) — mechanically checked by `kmp-audit`'s `_detect_long_stacked_comment_block` (5+ consecutive `//` lines, no `docs/reference/` pointer, not a leading license header, not a block reading as genuine WHY — see that detector's own docs for the signal-word heuristic) | N/A — KDoc doesn't accumulate this way; if a class needs paragraphs, that's what `docs/reference/` is for too |
| Nests? | N/A | KDoc does **not** nest. Plain block comments (`/* */`) do, unlike Java/C |

### Formatting

Verified against Kotlin's own official coding conventions (kotlinlang.org), not invented:

- **`//`**: exactly one space after the slashes — `// like this`, not `//like this`.
- **KDoc, short**: a single line is fine when the whole comment fits — `/** This is a short documentation comment. */`. Don't force a one-sentence KDoc onto three lines for symmetry.
- **KDoc, long**: opening `/**` alone on its own line, every following line starts with a single space then `*`, closing `*/` alone on its own line:
  ```kotlin
  /**
   * This is a documentation comment
   * on multiple lines.
   */
  ```
- **`@param`/`@return`**: the official guidance is to *avoid* these tags generally —
  weave the parameter/return description into the main text instead, with `[paramName]`
  links wherever it's mentioned, and use `@param`/`@return` only when the description is
  long enough that it doesn't fit the flow of the prose. This repo's own tag table below
  lists them as available tags, not as the default shape every KDoc should take.
- **Coverage is all-or-nothing, never partial.** The choice above is about *form*
  (inline `[name]` vs an `@param` tag), never about which parameters get addressed at
  all. If a function has 3 parameters and the KDoc documents 1 of them, that's a real
  defect — either say something about all 3 (mixing inline mentions and `@param` tags on
  the same declaration is fine) or write a plain single-line summary with no parameter
  detail at all. A KDoc block that looks thorough but silently skips 2 of 3 parameters is
  worse than no KDoc — it reads as complete and isn't. `kmp-audit`'s
  `_detect_partial_param_documentation` catches this mechanically.
- **KDoc supports Markdown** — verified against kotlinlang.org, not assumed. Inline markup
  inside `/** */` is regular Markdown (bold/italic, lists, links), plus a KDoc-specific
  shorthand for linking to another declaration:
  ```kotlin
  /**
   * Wraps [HttpClient] with retry logic. Use [retryPipeline] instead of calling this
   * directly — see [this][GROUP_ID.core.network.NetworkResult] for the result shape.
   *
   * - Retries transient failures up to `times`
   * - Never retries a 4xx response
   */
  ```
  `[declaration]` resolves the same way a reference inside the documented element would —
  no full qualification needed if it's already imported in the file. A fenced code block
  (` ``` `) works too, for a short usage snippet that doesn't warrant a full `@sample`.

### By architectural level

The table above sorts by comment *type*; this sorts by *where in the code* it lives —
use both together when reviewing or refactoring documentation.

| Level | Rule |
|---|---|
| Classes & interfaces | KDoc states the class's responsibility and architectural role only. Skip trivial openers ("Represents a X") — say what it owns and why it exists as a separate type, not what its name already tells you. |
| Functions & methods | KDoc only for complex public members, using the tag table below. Document inputs, outputs, and edge cases — never mechanics. `UndocumentedPublicFunction` requires *something*, so trivial one-liners (a getter, a pure delegate) get a single sentence, not a full `@param` breakdown. |
| Extension functions | State the receiver scope and calling context — *when* to reach for this extension, not just what it returns. Use `@receiver` for any precondition the receiver must satisfy (e.g. "must be called from inside an active `viewModelScope`"). This is the one KDoc case where "when to use it" outranks "what it does," because the same signature can exist as a member on an unrelated type. |
| Inline blocks (loops, conditionals) | No `//` that explains WHAT a block does — extract a named function or variable so the code reads as its own explanation. Keep `//` only for a non-obvious workaround or a business-logic WHY. |

```kotlin
/**
 * Retries [block] with exponential backoff, but only while this scope's job is active.
 * @receiver Must be a scope tied to a UI lifecycle (e.g. `viewModelScope`) — cancels
 * in-flight retries when the receiver is cancelled instead of leaking a delay loop.
 */
suspend fun <T> CoroutineScope.retryWhileActive(times: Int, block: suspend () -> T): T { ... }
```

### Two real mistakes this caught

**A `//` on the same line as code can swallow what follows it** — it runs to end-of-line,
including a needed closing `)`/`{`. Shipped in `kmp-imagevector-generator`'s
own codegen until a test caught it:

```kotlin
// ❌ WRONG — the // comments out the rest of the line, including `) {`
path(fill = SolidColor(Color.Black)  // tint at call site) {

// ✅ CORRECT — the call is syntactically complete before the comment starts
path(fill = SolidColor(Color.Black)) {  // tint at call site
```

**A `//` block that keeps growing is a sign two audiences got merged into one comment.**
Keep only the sentence that answers "why would someone break this by simplifying it?" —
move everything else (mechanism detail, rejected alternatives, exact version numbers) to
`docs/reference/` (the lane `kmp-project-docs-maintainer` already
defines for deep references), with a one-line pointer left behind:

```kotlin
// Composite build (not include()): root's apply false on org.jetbrains.compose locks
// that plugin ID to 1.11.1 build-wide. This module needs 1.12.0-beta01 for an
// experimental Compose Foundation Style API not available in the stable line.
// Full rationale: docs/reference/composite-build-style-experimental.md
includeBuild("tailwind/style-experimental")
```

### KDoc: code definition, params, samples

| Tag | Purpose |
|---|---|
| `@param` | Official guidance: avoid — describe the parameter inline in the main text with a `[name]` link instead. Reach for `@param` only when that description is too long to weave into the flow |
| `@return` | Same as `@param` — inline by default, tag only for a lengthy description. Skip entirely for `Unit` |
| `@throws` | A failure mode that's part of the contract, not every possible exception |
| `@see` | Cross-reference to a related declaration |
| `@sample` | Points at an actual, compiled function elsewhere as the usage example |
| `@property` / `@receiver` / `@constructor` | Constructor property / extension receiver / primary constructor, documented separately from the class summary |
| `@suppress` | Hides a technically-public declaration from generated docs |

**An example is warranted only when usage isn't obvious from the signature** (a builder, a
DSL) — never required per function or per file, same "why not what" rule as `//`. When one
is warranted, use `@sample`, not a pasted code block: it points at a real compiled
function, so it's type-checked and can't silently drift stale.

**When more than one tag is used on the same declaration, the required order is**
`@constructor`, `@receiver`, `@param`, `@property`, `@return`, `@throws`, `@see` — per the
[Android Kotlin style guide](https://developer.android.com/kotlin/style-guide)'s Block
tags rule. A tag never appears with an empty description; skip it entirely instead.

```kotlin
/**
 * Builds a [Result] pipeline that retries on transient failures.
 * @sample GROUP_ID.samples.retryPipelineSample
 */
fun <T> retryPipeline(times: Int, block: suspend () -> T): Flow<T> { ... }
```

Module/package-level docs (describing a whole module, not one declaration) are a separate
Dokka mechanism — `Module.md`/`Package.md` — not a KDoc tag.

### Detekt enforcement

```yaml
comments:
  UndocumentedPublicClass:
    active: true
    excludes: ['**/test/**', '**/*Test.kt', '**/*Preview*']
  UndocumentedPublicFunction:
    active: true
    excludes: ['**/test/**', '**/*Test.kt', '**/*Preview*']
  DocumentationOverPrivateFunction:
    active: true
  DocumentationOverPrivateProperty:
    active: true
  OutdatedDocumentation:
    active: true
```

`UndocumentedPublic*` requires KDoc on every public declaration; `DocumentationOverPrivate*`
forbids it on private ones; `OutdatedDocumentation` catches KDoc whose `@param`/signature
no longer matches the declaration after a refactor.

### License headers — situational, not a default

Per-file license headers were standard in the AOSP/Apache-Software-Foundation era and are
still worth it for **libraries redistributed externally** (Detekt ships
`AbsentOrWrongFileLicense`, off by default). Skip them for app code — redundant with the
root `LICENSE` file. See `kmp-library-publishing`'s "Per-file license
headers" for the rule config and template.

---

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

### `kotlin-reflect` — avoid in shared code

`kotlin-reflect` is a JVM-primary API — limited or absent on Kotlin/Native and Kotlin/JS,
and a real runtime/startup cost even on JVM. Never add it to `commonMain`'s dependencies;
if a `commonMain` file imports `kotlin.reflect.*` beyond the always-available `KClass`/
`::class` literal (full reflection: `memberProperties`, `KFunction.call`, etc.), that's a
signal the platform split was skipped, not a genuine cross-platform need.

- **Fine**: JVM-only modules (a Ktor server, a desktop-only feature) that already accept
  JVM as their sole target
- **Not fine**: reaching for reflection-based serialization or object inspection in
  shared code — use `kotlinx.serialization` instead, which code-generates via a compiler
  plugin and needs no runtime reflection on any platform
- `kmp-audit`'s `_detect_kotlin_reflect_in_common` catches full-reflection
  imports in `commonMain`

### Util/extension file organization

A single `Utils.kt`/`Helpers.kt`/`Extensions.kt` file accumulating unrelated top-level
functions across different domains (string formatting next to date math next to network
retry logic) is a real smell — the file has no single responsibility, and nothing about
its name tells a reader what's actually inside. Split by what the functions are *for*:
`StringExtensions.kt`, `DateExtensions.kt`, or move the function into the module that
owns the domain it touches. A file of extension functions all sharing the same receiver
type is fine and not what this flags — the smell is unrelated functions sharing only a
generic filename. `_detect_god_utils_file` flags a `*Utils.kt`/`*Helpers.kt` file with
10+ top-level functions spanning 3+ distinct (or no) receiver types.

### Regex readability

A regex used more than once, or complex enough to need explaining, must be bound to a
well-named constant — never inlined as a raw literal inside a function call:

```kotlin
// ❌ — unreadable inline, no name to signal intent, recompiled if hit in a hot path
if (Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matches(input)) { ... }

// ✓ — named, compiled once, self-documenting call site
private val EMAIL_RE = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
if (EMAIL_RE.matches(input)) { ... }
```

For a pattern with 2+ capture groups, prefer named groups over positional ones — a caller
reading `match.groups["year"]` doesn't need to cross-reference the pattern to know what
`match.groupValues[2]` means. Add a one-line WHY comment above any pattern using
lookaheads/lookbehinds or non-obvious escaping — what it matches should not require
mentally executing the regex. `_detect_inline_unnamed_regex` flags a `Regex(...)`/
`toRegex()` call inlined directly as a function-call argument instead of bound to a
`val`.

### Performance killers — Detekt's Performance ruleset, plus two real gaps it doesn't cover

Verified against Detekt's own docs before writing this: enable its **Performance**
ruleset (8 real rules) in `detekt.yml` alongside the sections in Detekt Setup above —
`ArrayPrimitive`, `CouldBeSequence`, `ForEachOnRange`, `SpreadOperator`,
`UnnecessaryInitOnArray`, `UnnecessaryPartOfBinaryExpression`,
`UnnecessaryTemporaryInstantiation`, `UnnecessaryTypeCasting`. None of these cover an
object constructed inside a loop with no dependency on the loop variable — a real,
common killer Detekt's own ruleset doesn't check:

```kotlin
// ❌ — SimpleDateFormat rebuilt every iteration
for (item in items) {
    val fmt = SimpleDateFormat("yyyy-MM-dd")
    results.add(fmt.format(item))
}

// ✓ — built once, before the loop
val fmt = SimpleDateFormat("yyyy-MM-dd")
for (item in items) {
    results.add(fmt.format(item))
}
```

`kmp-audit`'s `_detect_object_creation_in_loop` flags a known-expensive
constructor (`SimpleDateFormat`, `DateTimeFormatter`, `HttpClient`, `MessageDigest`,
`Gson`, `ObjectMapper`) built inside a `for`/`while` body whose arguments don't
reference the loop variable — a legitimate per-item construction (the constructor
genuinely uses the loop variable) is not flagged.

### Public mutable collection exposure

Distinct from the Compose-only unstable-collection-param check above — this is an
encapsulation concern, not a recomposition one. A public `MutableList`/`MutableMap`/
`MutableSet` property or return type lets any caller mutate your internal state through
the reference, regardless of whether Compose is involved at all:

```kotlin
// ❌ — a caller can add/remove/clear through this reference
class ItemStore {
    val items: MutableList<Item> = mutableListOf()
}

// ✓ — read-only surface, backed by a private mutable copy
class ItemStore {
    private val _items = mutableListOf<Item>()
    val items: List<Item> get() = _items
}
```

Especially relevant on an `explicitApi()` library's public surface, where this becomes
a permanent part of the contract. `_detect_public_mutable_collection` flags a
non-`private`/non-`internal` declaration exposing a `Mutable*` type directly.

### Android Context/Activity leak in a singleton

The classic Android memory leak: a `companion object` or singleton `object` caching a
`Context`/`Activity` reference. The singleton outlives the Activity, so the reference
prevents garbage collection for the process's whole lifetime — a real leak, not a style
nit. `applicationContext` (or an `Application` type) is the one safe exception — it
already lives for the process, so caching it long-term is fine:

```kotlin
// ❌ — leaks the Activity every time a new one is created
class SessionManager {
    companion object {
        var activity: Activity? = null
    }
}

// ✓ — application context is safe to hold long-term
class SessionManager {
    companion object {
        lateinit var appContext: Context  // set once, from Application.onCreate() with applicationContext
    }
}
```

Applies equally to a KMP library's Android `actual` implementation and an app —
`_detect_context_leak_in_singleton` scans both, no project-type gating.
`_detect_context_leak_in_singleton` flags a `Context`/`Activity`/`FragmentActivity`/
`AppCompatActivity`/`ComponentActivity`-typed property inside a `companion object`/
singleton scope.

### `TODO`/`FIXME` — already flagged, verify it's not silently off

Detekt's own `ForbiddenComment` rule (Style ruleset) flags `TODO:`/`FIXME:`/`STOPSHIP:`
comments **by default, active since Detekt 1.0.0** — verified against Detekt's own
docs, not assumed. Because this skill's `detekt.yml` uses `buildUponDefaultConfig =
true`, that default stays active automatically; nothing extra to enable. Worth stating
explicitly here since it was otherwise invisible — a project could have this running
the whole time with no one aware of it. Only touch it if you want to customize the
prefix list or add `allowedPatterns` exceptions.

### Patch-up fix instead of root-cause fix (hints)

"Is this a real fix or a band-aid" is a judgment call — but two specific, well-known
shapes of the pattern are mechanically detectable, as non-blocking hints (same tier as
the naming hints above):

- **Empty or log-only catch block** — silences the symptom without addressing why the
  exception was thrown. A deliberate best-effort no-op is sometimes genuinely correct
  (rare), so this stays a hint, not a blocker.
- **`@Suppress("Rule")` with no nearby comment** explaining why it's a false positive
  vs. silencing a real finding — legitimate suppressions are common, the missing
  justification is the actual signal, not the suppression itself.

```kotlin
// ❌ — hint fires: swallows the exception, no comment explaining why
try {
    risky()
} catch (e: Exception) {
}

// ✓ — real recovery, not flagged
try {
    risky()
} catch (e: IOException) {
    retryWithBackoff()
    logFailure(e)
}
```

A `TODO`/`FIXME` found in or near the flagged block is corroborating evidence, not a
requirement to fire — `_detect_empty_catch_block` and `_detect_unjustified_suppress`
both note it in the finding text when present, since a "TODO: fix properly" sitting
right next to the patch is a strong tell it's a known gap, not a considered decision.

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
| 2026-08-03 | **Correction**: removed the `coupling: CouplingBetweenObjects` Detekt config block and its "real, AST-based Detekt rule" claim (the 2026-07-20 entry below) — verified directly against Detekt's own `default-detekt-config.yml` on GitHub and confirmed no `coupling:` rule set and no `CouplingBetweenObjects` rule exist in Detekt at all. It's a PMD (Java) rule concept that was fabricated into this skill in error. A user asking "is our coupling detector concrete enough?" prompted the check. No direct Detekt replacement exists currently; `kmp-audit`'s `_detect_god_class` heuristic remains the only signal for cross-class coupling/fan-out. `LargeClass`/`TooManyFunctions` (the other two rules from that same entry) are real and unaffected. |
| 2026-08-03 | Added a one-line cross-reference to `kmp-native-authoring`'s new "Header vs implementation comments" section — the WHY-vs-WHAT split this section already documents for Kotlin KDoc/`//` applies identically to C++ header/implementation comments in a JNI or Kotlin/Native bridge, just with different syntax. |
| 2026-08-02 | Added Detekt's `NamedArguments` rule (complexity ruleset) with `ignoreArgumentsMatchingNames: true` — a user asked whether `Foo(id = id, name = name)` (redundant naming, value already equals param name) is checked; verified the real rule and its exact opt-out flag via a filed Detekt issue (detekt#4591 -> #4613). Also added argument-list wrapping guidance (kotlinlang.org's inline-vs-one-per-line convention, ktlint's `standard:argument-list-wrapping` and its `ktlint_code_style` gating caveat) and a note that Android Studio's IDE formatter and ktlint are separate tools with separate config — setting one does not configure the other. |
| 2026-08-02 | Added "Compiler Warnings" — a user reported seeing real Kotlin compiler warnings in Android Studio (deprecated calls, unchecked casts) that neither Ktlint nor Detekt catch, since neither invokes the actual compiler. Documented `allWarningsAsErrors` as an opt-in gate (never a day-one default — a big-bang change on a codebase with existing warnings) and cross-referenced `ci-github-actions`'s new CI step that surfaces warnings in the PR check before that gate is ready. |
| 2026-08-02 | Documented Detekt's own `ForbiddenComment` rule (Style ruleset, active by default since 1.0.0, flags `TODO:`/`FIXME:`/`STOPSHIP:`) — already effectively running via `buildUponDefaultConfig = true`, but never stated anywhere, so no one knew it was there. Added "Patch-up fix instead of root-cause fix" — empty/log-only catch blocks and unjustified `@Suppress`, both non-blocking hints backed by `kmp-audit`'s two new detectors, with `TODO`/`FIXME` adjacency woven in as corroborating evidence rather than a separate check. |
| 2026-08-02 | Added "Android Context/Activity leak in a singleton" — the classic Android memory leak, uncovered until now. Applies to both App and Library projects (a KMP library's Android `actual` code is exactly as leak-prone). Backed by `kmp-audit`'s new `_detect_context_leak_in_singleton`. |
| 2026-08-01 | Enabled Detekt's own Performance ruleset (verified against detekt.dev — 8 real rules: `ArrayPrimitive`, `CouldBeSequence`, `ForEachOnRange`, `SpreadOperator`, `UnnecessaryInitOnArray`, `UnnecessaryPartOfBinaryExpression`, `UnnecessaryTemporaryInstantiation`, `UnnecessaryTypeCasting`), previously off entirely. Added "Performance killers" (object constructed inside a loop, the one real gap Detekt's ruleset doesn't cover) and "Public mutable collection exposure" (an encapsulation concern distinct from the existing Compose-only unstable-collection-param check). Backed by `kmp-audit`'s two new detectors. |
| 2026-07-31 | Added "Kotlin Library & Pattern Choices" — `kotlin-reflect` (avoid in `commonMain`, JVM-primary and limited/absent on Native/JS), util/extension file organization (a god `Utils.kt` grab-bag is a real smell distinct from a single-receiver extension file), and regex readability (bind to a named `val`, never inline; named capture groups over positional for 2+ groups). Backed by `kmp-audit`'s three new detectors. Also added the Alpha-stability caveat kotlinx.collections.immutable was missing wherever this skill referenced it. |
| 2026-07-31 | Added a completeness rule to `@param`/`@return` guidance: the existing "avoid these tags, weave into prose" advice was about *form*, never about whether every parameter gets addressed — a user reported seeing generated KDoc that documented 1 of several parameters. Coverage is now explicit: all parameters or none, never partial. Backed by `kmp-audit`'s new `_detect_partial_param_documentation`. |
| 2026-07-31 | Added "Naming Conventions (Android Kotlin Style Guide)" — real gap: this skill covered formatting (Ktlint/Detekt, mechanical) and comment/KDoc conventions, but never naming *semantics* — verified against the real, current [Android Kotlin style guide](https://developer.android.com/kotlin/style-guide). Covers file/package naming, the type/function/constant naming table (including the `@Composable`-returning-`Unit`-must-be-PascalCase rule this repo's own generated components already followed by convention but never stated explicitly), acronym casing (`XmlHttpRequest` not `XMLHTTPRequest`), backing property `_x` convention, type variable naming, and the required KDoc block-tag order. Also flagged a real, deliberate conflict: the guide sets a 100-char line limit, this repo's `.editorconfig` sets 120 (matching kotlinlang.org's own convention instead) — documented as an acknowledged deviation, not silently changed. Added `kmp-audit`'s `_detect_lowercase_unit_composable` as the mechanical enforcement for the Composable-naming rule. |
| 2026-07-25 | Added "Long Parameter List, and its worse variant: a regressed Parameter Object" — named from a real example (an 11-param wrapper that re-exploded an existing `UiLayoutTracking` parameter object into 4 primitives just to reconstruct it one line later, even though the function it delegates to already accepts the object directly). Not mechanically detected — flagged in review guidance instead, since distinguishing "params happen to overlap a class" from "this is literally that class flattened" needs more context than a safe regex gets. 1 new anti-pattern. |
| 2026-07-20 | Enabled Detekt's `LargeClass`, `TooManyFunctions`, and `coupling.CouplingBetweenObjects` — real gap: god-object detection existed only for ViewModels and Composables (`kmp-audit`'s `_detect_viewmodel_size`/`_detect_god_composable`), nothing caught a repository/use-case/manager class doing too much. These are real AST-based Detekt rules, not a hand-rolled heuristic. `_detect_god_class` added as the non-Detekt backstop, cross-referenced here. |
| 2026-07-19 | New "Side-Effect-Free Accessors (Destructive Reads)" section — a real gap found while diagnosing a skill-vs-model-capability question against a separate KMP game project's commit history: a `consume*()` accessor that clears the field it just read before returning silently drops data for a second caller in the same tick/request (real bug: `Input.consumeTypedText()`/`consumeEditActions()`, fixed by moving the clear into one owned `snapshot()`). Rule generalized past input handling with a repository/`StateFlow` example. New `kmp-audit` detector `_detect_destructive_read_accessor` (heuristic 3-line "read into local, clear same field, return local" shape), 1 new anti-pattern, cross-referenced in Related Skills. |
| 2026-07-14 | Two additions to Comment & KDoc Conventions: (1) "Whether to write a comment at all" — a 4-step decision order that was previously scattered across prose rather than stated as one procedure. (2) "Formatting" — real, verified rules from Kotlin's own official coding conventions (kotlinlang.org): one space after `//`, KDoc's `/**`-alone-then-`*`-prefixed-lines-then-`*/`-alone shape for long comments vs single-line `/** ... */` for short ones, and the official guidance to *avoid* `@param`/`@return` in favor of inline prose — which contradicted this skill's own tag table until now (fixed both rows to state the real guidance instead of presenting the tags as the default shape). |
| 2026-07-14 | Real gap closed: the "grows past ~4 lines, split to docs/reference/" rule was documented but never mechanically checked anywhere — a user reported still seeing long stacked `//` blocks in their project after this skill shipped. Added `kmp-audit`'s `_detect_long_stacked_comment_block` (5+ consecutive `//` lines, no `docs/reference/` pointer) and cross-referenced it inline in the Comment & KDoc Conventions table. Excludes a leading license/copyright header (consistent with this skill's own existing license-header note) — verified against a real false-positive case before shipping. |
| 2026-07-10 | Added "By architectural level" — a second cut through the same rules organized by Classes/Functions/Extension functions/Inline blocks instead of by comment type, closing a real gap: extension functions had no documentation guidance at all beyond a passing `@receiver` mention. New rule: extension KDoc must state receiver scope/precondition, since "when to use it" outranks "what it does" for a function that could otherwise be mistaken for a member. 1 new anti-pattern, 1 new example. Wired into automation: `kmp-audit` gained a `what-comment in control flow` heuristic detector for the inline-block rule, and a new `/kmp-clean-comments` command applies the fix across all four levels (the convention was previously knowledge-only — nothing scanned or refactored comments automatically). |
| 2026-07-09 | Restructured "Comment & KDoc Conventions" around an explicit single-line (`//`) vs multi-line (KDoc `/** */`) split — a single decision table up front instead of scattered prose, so the rule is unambiguous for any agent to follow. Trimmed ~55 net lines (7 subsections → 5) while keeping every rule, both real-bug examples, the KDoc tag table, and the license-header note. |
| 2026-07-09 | Added a "Code comment vs. development notes" split, from a real 9-line inline comment that crammed a build-topology explanation, rejected alternatives, and exact version numbers into one `includeBuild()` call site. Rule: an inline comment survives only if it answers a question that would make someone break the code by "simplifying" it; the exhaustive rationale moves to `docs/reference/` (per `kmp-project-docs-maintainer`'s existing convention) with a one-line pointer left in the comment. Before/after example, 1 new anti-pattern, 2 new Related Skills cross-references. |
| 2026-07-09 | Extended the "Comment & KDoc Conventions" section: a full KDoc tag reference (`@param`/`@return`/`@throws`/`@see`/`@sample`/`@property`/`@receiver`/`@constructor`/`@suppress`), guidance that an example is warranted only for non-obvious public API (never required per function/file — same "why not what" principle), `@sample`'s advantage over a raw code block (references an actual compiled function, can't silently drift stale), and a "License headers" note (situational, not a default — cross-referenced to `kmp-library-publishing`). |
| 2026-07-08 | Added a "Comment & KDoc Conventions" section — KDoc for public API contracts, `//` for internal WHY notes, private members should be renamed rather than commented (backed by Detekt's `DocumentationOverPrivateFunction`/`DocumentationOverPrivateProperty`), and a real bug example (a `//` comment inside a function call's argument list silently commenting out the rest of the line, which actually shipped in `kmp-imagevector-generator`'s codegen). New `comments:` Detekt rule block and 3 anti-patterns. |
| 2026-06-18 | Initial release. |
