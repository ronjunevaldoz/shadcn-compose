# Detekt Setup

Part of `kmp-code-quality`. Load this file when working on: detekt setup.

---

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

