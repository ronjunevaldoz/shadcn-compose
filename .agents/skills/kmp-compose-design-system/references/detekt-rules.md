# Detekt Rules (PSI-based scanner)

Part of `kmp-compose-design-system`. Load this file when working on: detekt rules (psi-based scanner).

---

The design system ships a custom detekt rule set that replaces regex-based violation
detection with full Kotlin PSI analysis. PSI traversal resolves variable aliases,
handles trailing-lambda syntax correctly, and enables two rules that regex cannot
express: component reimplementation detection and import boundary enforcement.

### Module location

Copy `detekt-rules/` from this skill into your project's `:core:designsystem` module:

```
core/designsystem/
├── detekt-rules/
│   ├── build.gradle.kts
│   ├── config/
│   │   └── detekt-design-system.yml
│   └── src/
│       ├── main/kotlin/GROUP_ID/designsystem/detekt/
│       │   ├── DesignSystemRuleSetProvider.kt
│       │   ├── HardcodedColorRule.kt
│       │   ├── HardcodedDpRule.kt
│       │   ├── MaterialThemeUsageRule.kt
│       │   ├── DirectTextStyleRule.kt
│       │   ├── NestedContainerRule.kt
│       │   ├── ComponentRegistryRule.kt
│       │   ├── ImportBoundaryRule.kt
│       │   ├── RedundantScreenTitleRule.kt
│       │   └── HardcodedGridColumnsRule.kt
│       └── test/kotlin/GROUP_ID/designsystem/detekt/
│           ├── HardcodedColorRuleTest.kt
│           ├── ComponentRegistryRuleTest.kt
│           ├── ImportBoundaryRuleTest.kt
│           ├── RedundantScreenTitleRuleTest.kt
│           └── HardcodedGridColumnsRuleTest.kt
```

Replace `GROUP_ID` with your actual group ID (e.g. `com.example.myapp`) — same as your convention plugin names in `build-logic/`.

### Wire into the Gradle build

In `core/designsystem/build.gradle.kts`:

```kotlin
plugins {
    id("io.gitlab.arturbosch.detekt")
}

detekt {
    config.setFrom("detekt-rules/config/detekt-design-system.yml")
    buildUponDefaultConfig = true
}

dependencies {
    detektPlugins(project(":core:designsystem:detekt-rules"))
}
```

Add to `settings.gradle.kts`:

```kotlin
include(":core:designsystem:detekt-rules")
```

### Run

```bash
# Check violations (CI mode)
./gradlew detekt

# Fix session (re-scan after each edit)
./gradlew detekt --rerun-tasks --continue
```

### Rules summary

| Rule ID | Severity | What it catches | What regex missed |
|---|---|---|---|
| `HardcodedColor` | Error | `Color(0xFF…)`, `Color(r,g,b)` | Variable aliases in local scope |
| `HardcodedDp` | Warning | `.dp` literals in layout modifiers | Modifier chains deeper than 1 level |
| `MaterialThemeUsage` | Error | `MaterialTheme.colors.*`, `MaterialTheme.colorScheme.*` | — |
| `DirectTextStyle` | Error | `TextStyle(…)` construction | — |
| `NestedContainer` | Warning | `Card { Card {` and `Surface { Surface {` | Trailing-lambda form `Card { }` |
| `ComponentRegistryViolation` | Warning | `@Composable fun MyButton(label: String) { Button(...) { Text(...) } }` outside `core/designsystem/` — name matches a DS component suffix AND the body never calls the matching DS component (so `fun ProductCard(title: String) { AppCard { AppText(title) } }` is correctly NOT flagged — that's composing the design system, not reimplementing it) | Entire class — regex can't see function definitions |
| `DesignTokenImportBoundary` | Error | `import …tokens.AppColors` in `feature/*/ui/` | Entire class — regex can't check import context |
| `RedundantScreenTitle` | Warning | `Text("…")` / `AppText("…")` with a string literal inside `*Content` / `*Screen` composables | Cannot infer that the composable is a screen or that a TopAppBar already shows the same string |
| `HardcodedGridColumns` | Warning | `GridCells.Fixed(N≥2)` — fixed column count ignores screen width | Cannot count GridCells arguments or distinguish `Fixed` from `Adaptive` |

### Configuration

Customize `config/detekt-design-system.yml`:

```yaml
design-system:
  ComponentRegistryRule:
    active: true
    # Must match the prefix resolved in Step 0 (Acme, GuildBase, ...) — not the literal
    # word "App" unless that's genuinely what Step 0 resolved to for this project.
    componentPrefix: 'Acme'
  HardcodedDp:
    active: true
    # To disable dp warnings while keeping color/MaterialTheme errors:
    # active: false
```

### Quick CLI fallback

When detekt is not yet wired into the project, use the Python scanner for a fast check:

```bash
python3 skills/kmp-compose-design-system/scripts/scan_design_violations.py \
  /path/to/project --json
```

The Python scanner covers rules 1–5 (`HardcodedColor` through `NestedContainer`) but
not `ComponentRegistryViolation` or `DesignTokenImportBoundary`.

---

