# /fix-design

Scans an existing KMP project for design-system usage violations, fixes them
file-by-file with per-file confirmation, then re-runs Roborazzi screenshot tests
and uses Claude vision to verify each fix looks correct before moving on.

**What it fixes:**
- `hardcoded_color` — `Color(0xFFRRGGBB)` → `appTheme.colors.*` token
- `hardcoded_dp` — `padding(16.dp)` → `appTheme.spacing.*` token
- `material_theme` — `MaterialTheme.colors.*` → `appTheme.colors.*`
- `direct_textstyle` — `TextStyle(...)` construction → `AppTextStyle.*` enum
- `nested_container` — `Card { Card {` redundant nesting → flat layout

**What it never touches:**
- `tokens/` — `AppColors.kt`, `AppTypography.kt`, `AppSpacing.kt`, `AppShapes.kt`
- `theme/` — `AppTheme.kt`, `StyleScopeExtensions.kt`
- Any file ending in `Styles.kt`, `Theme.kt`, `Tokens.kt`

---

## Step 1 — Locate the project root

If the user ran `/fix-design` without a path, ask:

```
Which project should I scan for design violations?
(Provide the path to the KMP project root, e.g. ~/projects/MyApp)
```

Set `PROJECT_ROOT` from the answer. Set `SKILLS_ROOT` to the directory containing
this skills collection (parent of `commands/`).

---

## Step 2 — Run the scanner

**Primary (PSI-based — recommended when detekt is wired into the project):**
```bash
./gradlew detekt --rerun-tasks \
  --config "$SKILLS_ROOT/skills/kotlin-multiplatform-design-system/detekt-rules/config/detekt-design-system.yml"
```

Parse the detekt XML/SARIF output. Violations map to rule IDs:
`HardcodedColor`, `HardcodedDp`, `MaterialThemeUsage`, `DirectTextStyle`,
`NestedContainer`, `ComponentRegistryViolation`, `DesignTokenImportBoundary`.

**Fallback (quick CLI, no JVM warm-up, no Gradle required):**
```bash
python3 "$SKILLS_ROOT/skills/kotlin-multiplatform-design-system/scripts/scan_design_violations.py" \
  "$PROJECT_ROOT" --json
```

Use the fallback when detekt is not yet set up in the project's Gradle build.
The fallback catches violations 1–5 (color, dp, MaterialTheme, TextStyle, nested
containers) but not the PSI-only rules (ComponentRegistry, ImportBoundary).

If the scanner finds no violations:
```
✅ No design violations found. Nothing to fix.
```
Stop here.

Group findings by file. Within each file, sort by line number.

**Print a summary before starting fixes:**
```
Found N violations across M files:
  ❌  hardcoded_color    X  (use appTheme.colors.*)
  ❌  material_theme     X  (use appTheme.*)
  ❌  direct_textstyle   X  (use AppTextStyle enum)
  ⚠️  hardcoded_dp       X  (use appTheme.spacing.*)
  ⚠️  nested_container   X  (remove outer wrapper)

Processing files one at a time. You'll confirm each before I apply changes.
```

---

## Step 3 — Fix each file

For each file that has violations, in order of error count (highest first):

### 3a. Show the violations

```
── feature/auth/ui/src/commonMain/kotlin/AuthContent.kt  (3 issues) ──
  ❌ L 42  [hardcoded_color]   Color(0xFF1A73E8)
  ❌ L 67  [material_theme]    MaterialTheme.typography.headlineMedium
  ⚠️ L 89  [hardcoded_dp]     Modifier.padding(24.dp)
```

### 3b. Read the file and generate fixes

Read the full file. For each violation, determine the correct replacement:

| Violation | Replacement rule |
|---|---|
| `Color(0xFF...)` | Map to nearest semantic token: brand blue → `appTheme.colors.primary`, red → `appTheme.colors.error`, grey → `appTheme.colors.onSurface`, etc. If ambiguous, choose the closest semantic meaning and note the assumption. |
| `MaterialTheme.colors.X` | Replace with `appTheme.colors.X` (same property name, different accessor) |
| `MaterialTheme.typography.X` | Replace with `appTheme.typography.X` |
| `MaterialTheme.shapes.X` | Replace with `appTheme.shapes.X` |
| `TextStyle(fontSize = X, ...)` | Replace with the matching `AppTextStyle` variant passed through `appTheme.typography.*` |
| `Modifier.padding(N.dp)` | Map N to token: 4→xs, 8→sm, 12→md, 16→lg, 20→xl, 24→xxl, 32→xxxl. Note assumption if N is not in the table. |
| `Modifier.height(N.dp)` | Same token mapping as above. |
| `Modifier.size(N.dp)` | Same token mapping. |
| `Spacer(Modifier.height(N.dp))` | Same token mapping. |
| Nested `Card { Card {` | Remove the outer `Card` and any duplicate `modifier`, `elevation`, `shape` props. Keep only the innermost. |
| Nested `Surface { Surface {` | Same — keep innermost, remove outer. |

Ensure `val t = appTheme` is present at the top of any `@Composable` that now uses
`appTheme.*`. If the function already calls `appTheme` via the `@StyleScope` receiver,
no change needed.

### 3c. Confirm before writing

Show a unified diff of the proposed changes:

```diff
- val primaryColor = Color(0xFF1A73E8)
+ val primaryColor = appTheme.colors.primary

- style = MaterialTheme.typography.headlineMedium
+ style = appTheme.typography.displayMedium

- .padding(24.dp)
+ .padding(appTheme.spacing.xxl)
```

Ask:
```
Apply these changes to AuthContent.kt? [yes / skip / show full file]
```

- **yes** → write the changes
- **skip** → move to next file
- **show full file** → print the full proposed file, then ask again

---

## Step 4 — Verify with Roborazzi screenshot

After writing each file, determine the composable name(s) in that file (look for
`@Composable fun XxxContent` or `@Composable fun XxxScreen`). Then:

### 4a. Regenerate screenshots

**Component-level (always run first):**
If any tokens, styles, or design system components were changed, regenerate
the design system component screenshots before the feature screenshots:

```bash
./gradlew :core:designsystem:jvmTest
```

This runs all `previews/AppXxxPreview.kt` tests and produces one PNG per
`@Preview` function in `core/designsystem/build/...`. Read these PNGs first —
they verify the component itself is correct in isolation, independent of any
feature-specific layout.

**Feature-level:**
After the design system tests pass, regenerate the feature screen screenshots:

```bash
./gradlew :<module>:jvmTest \
  --tests "*<ComposableName>*" \
  -Pcompose.desktop.verbose=false
```

Where `<module>` is the Gradle module path for the fixed file (e.g.
`:feature:auth:ui`). If the module path is unclear, derive it from the file's
position under `feature/<name>/ui/`.

If the test run fails (compilation error, missing token), show the error and ask
the user to resolve before continuing.

### 4b. Locate the generated PNGs

After a successful test run, search for new/changed PNG files:

```bash
find "$PROJECT_ROOT" -name "*.png" -newer "$FIXED_FILE_PATH" \
  | grep -i "snapshot\|golden\|screenshot"
```

Prefer light+dark pairs: `*_light.png` and `*_dark.png`. If only one PNG exists,
use it.

### 4c. Read and verify with vision

Read each PNG using the Read tool. Check for the following against the
design system rules:

**Pass criteria (all must be true):**
- [ ] Primary action buttons use a consistent, non-arbitrary brand color
      (not stark red, not pure `#000000` or `#FFFFFF` as a brand color)
- [ ] Spacing between elements looks rhythmically consistent — not cramped
      at one edge and loose at another
- [ ] No visible double-shadow or double-elevation artifact that would indicate
      remaining nested Card/Surface
- [ ] Dark mode PNG (if present) has a dark background — not a white card on white
- [ ] Typography hierarchy is visually clear: headline > body > caption in size

**Report the result:**
```
📸 AuthContent_light.png — PASS
   ✅ Brand color on button matches design system primary
   ✅ Spacing visually consistent
   ✅ No nested card artifacts
   ✅ Typography hierarchy clear

📸 AuthContent_dark.png — PASS
   ✅ Dark background confirmed
   ✅ Primary color visible against dark surface
```

If any check fails:
```
📸 AuthContent_light.png — REVIEW NEEDED
   ✅ Spacing looks consistent
   ⚠️ Button color appears to be raw red — confirm Color.Red was intentional
      or the wrong semantic token was chosen
```

When a check fails, offer:
```
Options:
  [1] Revert AuthContent.kt to original
  [2] Edit — show me the diff again so I can adjust
  [3] Accept anyway — the visual is intentional
```

---

## Step 5 — Summary

After all files are processed:

```
/fix-design complete

  Files fixed:    X
  Files skipped:  X
  Screenshots verified: X passed, X need review

Next steps:
  1. Run ./gradlew :androidApp:assembleDebug  (full build check)
  2. Run ./gradlew jvmTest                    (re-run all screenshot tests)
  3. Review any ⚠️ REVIEW NEEDED screenshots above
  4. Commit: git add -p  (stage only the design fixes)
  5. Run /record-design-baselines             (update golden PNGs to match fixed state)
  6. Run /audit-design-visual                 (visual pass — catches what code analysis misses)
```

---

## Rules

- **Never** bulk-apply all fixes without per-file confirmation.
- **Never** modify `tokens/`, `theme/`, or any `*Styles.kt` file.
- **Always** compile after all fixes are applied.
- When mapping a hardcoded color to a token, state the assumption explicitly —
  e.g., "I'm mapping `0xFF1A73E8` to `colors.primary` because it is the brand blue.
  If this is a one-off accent, you may want a different token."
- When Roborazzi is not set up in the project, skip Steps 4a–4c and note:
  "Roborazzi not detected — visual verification skipped. Consider adding
  `kotlin-multiplatform-roborazzi` to enable automated screenshot checks."
