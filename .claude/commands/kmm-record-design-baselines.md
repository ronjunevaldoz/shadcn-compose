# /record-design-baselines

Records Roborazzi golden screenshot baselines for the design system and all
feature screens. Run this once after the design system is stable, then on CI
any screenshot that drifts from baseline fails the build.

---

## When to run

- After initial design system setup (before first PR)
- After intentional design changes (new token values, updated component variants)
- After running `/fix-design` to record the fixed state as the new baseline

Do **not** run this during a `/fix-design` session — record baselines only when you
are satisfied with the current visual state.

---

## Step 1 — Record design system component baselines

```bash
./gradlew :core:designsystem:jvmTest \
  -Proborazzi.record=true \
  --rerun-tasks
```

This runs all `previews/AppXxxPreview.kt` files and writes one PNG per `@Preview`
function to:

```
core/designsystem/build/outputs/roborazzi/
  AppButton_DefaultLight_Phone.png
  AppButton_DefaultLight_Tablet.png
  AppButton_DefaultLight_Desktop.png
  AppButton_DefaultDark_Phone.png
  AppButton_DefaultDark_Tablet.png
  AppButton_DefaultDark_Desktop.png
  AppButton_OutlineLight.png        ← state variants keep plain @Preview (one PNG each)
  ...
```

> **Why three device PNGs per base variant?** The `@MultiDevicePreview` annotation
> (defined in `AppThemePreviewWrapper.kt`) is a composite of three `@Preview`s at
> 360dp / 673dp / 1280dp widths. Each produces a separate PNG so the visual audit
> can compare phone, tablet, and desktop in one baseline record step.

Copy the generated PNGs to the golden directory:

```bash
cp -R core/designsystem/build/outputs/roborazzi/ \
      core/designsystem/src/jvmTest/snapshots/
```

---

## Step 2 — Record feature screen baselines

For each feature module that has Roborazzi screenshot tests:

```bash
./gradlew :<module>:jvmTest \
  -Proborazzi.record=true \
  --rerun-tasks
```

Example for an auth feature:

```bash
./gradlew :feature:auth:ui:jvmTest \
  -Proborazzi.record=true \
  --rerun-tasks
```

Copy PNGs to the snapshots directory for that module:

```bash
cp -R feature/auth/ui/build/outputs/roborazzi/ \
      feature/auth/ui/src/jvmTest/snapshots/
```

---

## Step 3 — Commit baselines

```bash
git add core/designsystem/src/jvmTest/snapshots/
git add feature/*/ui/src/jvmTest/snapshots/
git commit -m "chore: record Roborazzi design baselines"
```

---

## Step 4 — Set up CI diff check

Add to your GitHub Actions workflow (`.github/workflows/ci.yml`):

```yaml
- name: Screenshot diff check
  run: ./gradlew jvmTest -Proborazzi.verify=true
  # Fails the build if any screenshot deviates from the recorded baseline.
  # To update baselines after an intentional change, re-run /record-design-baselines.
```

When `roborazzi.verify=true`, Roborazzi compares every generated PNG against the
committed golden and fails the test if the pixel delta exceeds the threshold configured
in `roborazzi {}` block:

```kotlin
// build.gradle.kts for each module
roborazzi {
    outputDir.set(file("src/jvmTest/snapshots"))
    compareOptions {
        resultValidator = SimpleImageComparator(
            maxErrorPixel = 0.01f,  // 1% pixel delta allowed
        )
    }
}
```

---

## Rules

- **Never commit baselines without reviewing them visually** — open the PNGs and
  confirm colors, spacing, and typography look correct before `git add`.
- **Never run with `roborazzi.record=true` in CI** — recording always succeeds; it
  bypasses the diff check and silently accepts any regression.
- When a CI diff fails on a PR that contains intentional design changes, the PR author
  runs `/record-design-baselines` locally, reviews the new PNGs, and commits the updated
  baselines as part of the PR.
