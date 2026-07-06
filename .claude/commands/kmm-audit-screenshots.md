# /audit-screenshots $ARGUMENTS

**KMM Agent Skills** — analyze Roborazzi golden screenshots for design consistency.
Uses Claude's vision capability to inspect committed PNG files against the design
system rules: color tokens, spacing, typography, TopAppBar structure, dark/light
mode parity, and accessibility contrast.

Search root: `$ARGUMENTS` (defaults to `.` — the current project root)

This is a visual audit — it catches design regressions that logic tests miss:
wrong colors, broken dark mode, inconsistent spacing, or missing TopAppBar chrome.

---

## Step 1 — Resolve the Roborazzi output directory

The screenshots location is project-configured, not fixed. Resolve it in this order:

**1a. Read the `build.gradle.kts` that applies the Roborazzi plugin.**

Search for the file that contains both `io.github.takahirom.roborazzi` and a `roborazzi {` block:

```bash
grep -rl "io.github.takahirom.roborazzi" "${ARGUMENTS:-.}" \
  --include="build.gradle.kts" \
  --include="build.gradle" \
  | head -5
```

For each candidate file, look for an explicit `outputDir`:

```bash
grep -A 5 "roborazzi {" <candidate file>
```

Example patterns to match:
```
roborazzi {
    outputDir = project.file("src/jvmTest/snapshots")   # most common
    outputDir = File(projectDir, "screenshots/golden")  # custom path
    outputDir = layout.projectDirectory.dir("goldens").asFile
}
```

Extract the path string. Resolve it relative to the directory containing the
`build.gradle.kts` file (not the project root) — `project.file(...)` anchors to the module.

**1b. No explicit `outputDir` found — use Roborazzi's default.**

Roborazzi writes goldens to `src/test/snapshots/` (Android JUnit4) or `src/jvmTest/snapshots/`
(CMP JVM). Detect which applies:

```bash
# Check for jvmTest (CMP / Desktop / JVM target)
find "${ARGUMENTS:-.}" -type d -name "jvmTest" | head -3

# Check for standard test (Android)
find "${ARGUMENTS:-.}" -type d -name "test" -path "*/src/test" | head -3
```

Use `src/jvmTest/snapshots/` if a `jvmTest` source set exists, else `src/test/snapshots/`.
Resolve relative to the module root (same directory as the `build.gradle.kts`).

**1c. Print the resolved path before proceeding:**

```
Roborazzi output dir: <module-root>/src/jvmTest/snapshots/  [from build.gradle.kts]
  or
Roborazzi output dir: <module-root>/src/jvmTest/snapshots/  [default — outputDir not set]
```

If multiple modules use Roborazzi (monorepo), resolve each independently and audit each.

---

## Step 2 — Find screenshots

```bash
find "<resolved-output-dir>" -name "*.png" \
  -not -name "*_compare.png" \
  -not -name "*_actual.png" \
  | sort
```

Group files into pairs where possible:
- `FooContent_light.png` + `FooContent_dark.png` → one audit unit
- Unpaired files → audit individually

If no PNG files are found: print `No screenshots found at <resolved path>` and stop.

---

## Step 3 — For each screenshot (or pair), run a visual design audit

Read each image using vision and check all of the following. Flag each issue
with the screenshot filename and a short description.

### Color token compliance
- [ ] No solid blocks of arbitrary color visible that don't match a design token pattern
- [ ] Background uses a neutral surface color (not pure `#000000` or `#FFFFFF` in dark mode unless intentional)
- [ ] Interactive elements (buttons, chips) use a consistent accent color across screens
- [ ] Error states are red-family; success states are green-family — consistent with AppTheme

### Dark mode parity (pairs only)
- [ ] Dark variant has a dark background — if it looks identical to the light variant, it is broken
- [ ] Text is light-on-dark in dark mode, not dark-on-dark (invisible text)
- [ ] No elevation shadows that are too harsh in dark mode (shadows should be subtle)
- [ ] Icons and illustrations adapt — no all-white icons on a white dark-mode background

### AppScaffold structure
- [ ] TopAppBar is present at the top of every screen (not a bare Text title in the content body)
- [ ] Screen title appears in the TopAppBar, not duplicated in the content
- [ ] Back/close button is in the TopAppBar navigation slot if the screen is non-root
- [ ] Primary action buttons (save, confirm) are in TopAppBar actions or a prominent CTA — not a plain text link

### Spacing and layout
- [ ] Content has consistent outer padding — elements don't touch the screen edge
- [ ] List items have consistent internal padding between icon, label, and trailing action
- [ ] No obvious alignment breaks — elements that should be flush are flush

### Typography
- [ ] Body text is readable — not too small (visually under ~12sp equivalent)
- [ ] Headings are visually distinct from body text
- [ ] All text truncates with an ellipsis, not by clipping or overflowing

### Accessibility contrast
- [ ] Text on colored backgrounds appears readable — flag if text and background look low-contrast
- [ ] Disabled state elements are visually distinct (greyed out) but not invisible
- [ ] Icon-only buttons have enough visual weight to be tappable

---

## Step 4 — Output

For each screenshot audited:

```
SCREENSHOT: FooContent_light.png + FooContent_dark.png

  ✅ Color tokens       — consistent with design system
  ✅ Dark mode parity   — dark variant correctly inverts background/text
  ⚠️  AppScaffold       — TopAppBar missing; title appears as plain Text in content body
  ✅ Spacing            — consistent outer padding, no edge-touching elements
  ⚠️  Contrast          — "Cancel" button label may be low-contrast on the gray background
  ✅ Typography         — heading/body hierarchy clear
```

Aggregate summary at the end:

```
AUDIT SUMMARY: <N> screenshots / <N> pairs

  PASS:    <N>
  WARNING: <N>   (design issues that don't block but should be addressed)
  FAIL:    <N>   (broken dark mode, missing TopAppBar, invisible text)

RESULT: PASS | NEEDS ATTENTION
```

---

## Step 5 — Recommended fixes

For each WARNING or FAIL, give a concrete fix tied to the design-system skill:

| Finding | Fix | Skill |
|---|---|---|
| Missing TopAppBar | Wrap content in `AppScaffold { AppTopAppBar(...) }` | `kotlin-multiplatform-design-system` |
| Dark mode identical to light | Check `AppTheme.colors.background` is not hardcoded; use semantic tokens | `kotlin-multiplatform-design-system` |
| Low-contrast text | Replace `Color(0xFFAAAAAA)` with `AppTheme.colors.onSurfaceVariant` | `kotlin-multiplatform-design-system` |
| Inconsistent spacing | Replace `padding(16.dp)` with `AppTheme.spacing.lg` | `kotlin-multiplatform-design-system` |
| Title duplicated in content | Remove `Text(title)` from content; move to `AppTopAppBar(title = "...")` | `kotlin-multiplatform-design-system` |

---

## Notes

- Warnings are not blockers by default — the developer decides whether to fix before merging.
  Use `FAIL` only for clearly broken states (invisible text, completely wrong dark mode, absent TopAppBar).
- This audit is a supplement to, not a replacement for, Roborazzi golden image diffs.
  Diffs catch regressions; this audit checks that the golden itself is correct.
- If the screenshots directory contains `_compare.png` or `_actual.png` files (diff artifacts),
  those indicate a failing `jvmTest` run — resolve the test failure before running this audit.
- Run this after `./gradlew recordRoborazziJvm` on a new screen, or after a design-system token update.
- The output directory is always resolved from `build.gradle.kts` first (Step 1) — never assume `src/jvmTest/snapshots/` is correct without checking. Projects with a custom `outputDir` will be missed if the path is hardcoded.
