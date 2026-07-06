# /update-design-system

Compares the current KMP project's `:core:designsystem/components` against the reference
implementations in the `kotlin-multiplatform-design-system` skill and applies safe updates.

**What is project-owned (never touched):**
- `tokens/` — `AppColors.kt`, `AppTypography.kt`, `AppShapes.kt`, `AppSpacing.kt`
- `theme/` — `AppTheme.kt`, `StyleScopeExtensions.kt`

**What is skill-owned (updateable):**
- `components/App*.kt` — component implementations; bug fixes and new variants flow here

---

## Step 1 — Locate the project root

If the user ran `/update-design-system` without specifying a path, ask:

```
Which project should I update the design system for?
(Provide the path to the KMP project root, e.g. ~/projects/MyApp)
```

Set `PROJECT_ROOT` from the answer.

---

## Step 2 — Run the comparison script

```bash
python3 <skills_root>/skills/kotlin-multiplatform-design-system/scripts/update_design_system.py \
  "$PROJECT_ROOT"
```

Where `<skills_root>` is the directory containing this skills collection.

**Exit 0** → all components CURRENT or MODIFIED. Continue to Step 3.
**Exit 1** → one or more components MISSING. Continue to Step 3.
**Exit 2** → SKILL.md not found. Report the error and stop.

---

## Step 3 — Present the report

Show the script output verbatim. Then for each status category:

### MISSING components
Offer to generate them now using the design-system skill:
```
The following components are missing from your project:
  AppTextField.kt

Generate them now? [yes / no / select]
```
If yes (or all), run the design-system skill's Step 6 for each missing component and write
the file to `core/designsystem/components/`.

### MODIFIED components
Do NOT overwrite automatically. For each modified component, show the diff:

```bash
python3 <skills_root>/skills/kotlin-multiplatform-design-system/scripts/update_design_system.py \
  "$PROJECT_ROOT" --diff <ComponentName>
```

Present the diff and ask:
```
AppButton.kt has project customisations. Options:
  [1] Keep project version  — no change
  [2] Apply skill version   — overwrites with reference (loses customisations)
  [3] Show diff again
```

Wait for the user to choose for **each** modified component. Never bulk-overwrite modified files.

### CURRENT components
Report as up to date. No action needed.

---

## Step 4 — Apply changes

For each component the user approved in Step 3:

1. Read the reference code from the diff output or re-run `--diff` to get the skill version.
2. Write it to `core/designsystem/components/<ComponentName>.kt`.
3. Confirm: "Updated `AppButton.kt` ✅"

After all updates, remind the user to:
```
Next steps:
  1. Run ./gradlew :core:designsystem:compileCommonMainKotlinMetadata
  2. Run ./gradlew :androidApp:assembleDebug
  3. Check Roborazzi goldens for visual regressions
```

---

## Step 5 — Extended components (optional)

If the project also uses `kotlin-multiplatform-design-system-extended`, run the same
comparison against the extended skill's components. The extended skill does not have a
dedicated script yet — check manually whether the project's extended components match the
skill's Step 2 onward.

---

## Rules

- **Never** modify `tokens/`, `theme/AppTheme.kt`, or `theme/StyleScopeExtensions.kt`.
- **Never** bulk-overwrite MODIFIED components — always diff and confirm per component.
- **Always** compile after applying changes to catch import or API breakage early.
- This command is additive: it only writes files the user explicitly approved.
