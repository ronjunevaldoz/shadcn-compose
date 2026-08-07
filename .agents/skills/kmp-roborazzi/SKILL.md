---
name: kmp-roborazzi
description: >
  Complete UI layer testing for KMP: semantic test tags on composables, Compose UI
  interaction tests in commonTest (runComposeUiTest, onNodeWithTag, performClick,
  assertIsDisplayed) that run on every target — JVM, Android instrumented, iOS
  simulator, Wasm — and Roborazzi screenshot tests that capture @Preview composables
  on JVM/Desktop for visual regression detection. Covers the full stack from testTag
  conventions to CI golden image diffs. Replaces kmp-testing-robot for
  UI regression testing.
license: Apache-2.0
metadata:
  author: kmp-agent-skills
  last-updated: '2026-07-10'
  keywords:
    - Roborazzi
    - screenshot test
    - golden image
    - '@Preview'
    - JVM screenshot
    - visual regression
    - CI diff
    - KMP
    - Kotlin Multiplatform
    - Desktop JVM
    - testTag
    - test tag
    - Compose UI test
    - createComposeRule
    - runComposeUiTest
    - commonTest UI test
    - multiplatform UI test
    - onNodeWithTag
    - interaction test
    - semantics
    - visual accuracy
    - layout test
    - canvas test
    - arrangement test
    - pixel-perfect
    - layout regression
    - bounds sidecar
    - position regression
    - boundsInRoot
    - fetchSemanticsNode
    - exact position diff
---

## When to Use This Skill

Use when you need to:
- Add semantic `testTag` identifiers to composables so tests can target specific nodes
- Write Compose UI interaction tests (`onNodeWithTag`, `performClick`, `assertIsDisplayed`)
- Capture screenshot golden images from `@Preview` composables on JVM
- Detect visual regressions automatically in CI
- Wire the full UI testing stack: test tags → interaction tests → screenshot tests

**Trigger keywords:** screenshot test, Roborazzi, golden image, visual regression, preview screenshot,
UI test JVM, screenshot diff, CI visual test, testTag, test tag, compose test rule, onNodeWithTag,
createComposeRule, interaction test, compose UI test, semantics node, visual accuracy,
pixel-perfect, layout test, canvas test, arrangement test, layout regression, 100% accuracy,
UI layout verification, canvas layout test, visual confirmation, test layout,
test UI, test screen, UI testing, visual test, test this screen, test the layout,
screenshot testing, visual regression testing, UI coverage, test composable,
drag test, swipe test, performTouchInput, performMouseInput, test drag,
test resizable panel, test scrollbar, boundsInRoot, layout stability test.

**Freshness rule:** Roborazzi is actively developed — the Gradle plugin API and the
`captureRoboImage` API change between minor versions. Recheck the GitHub releases page before
pinning a version. `runComposeUiTest` (`org.jetbrains.compose.ui:ui-test`) tracks the Compose
Multiplatform version and is still `@ExperimentalTestApi` — recheck its signature before
upgrading CMP.

---

## Recommendation First

Default to this three-layer UI testing stack:

1. **Test tags** on every interactive or assertable node — `Modifier.testTag(FooTestTags.LOGIN_BUTTON)`
2. **Interaction tests** in `commonTest` with `runComposeUiTest` — verify behaviour
   (enabled/disabled, text shown, clicks fire). Required CI gate: `jvmTest`, no emulator
   needed. Per-platform matrix (`androidDeviceTest`, `iosSimulatorArm64Test`, `wasmJsTest`)
   is opt-in/nightly — see CI Integration below.
3. **Roborazzi screenshot tests** in `jvmTest` — verify visual output (layout, color,
   loading/error/empty states). For feature `Content` screens, cover phone, tablet, and
   desktop sizes, and record both light and dark themes when the screen supports them.

Why:
- Test tags make tests stable — `onNodeWithTag` doesn't break when copy changes
- `commonTest` interaction tests catch real platform-specific bugs (text input, focus,
  gesture handling differ per target) that JVM-only tests structurally cannot
- Roborazzi goldens catch unintentional visual regressions that logic tests miss
- Roborazzi has **no multiplatform equivalent** — it's built directly on Robolectric's
  Android-framework shadow rendering, so screenshot tests stay JVM-only regardless of
  where interaction tests live
- Keeping the full device/simulator matrix out of the required PR gate keeps CI fast;
  emulator/simulator boot time is real cost that shouldn't block every push

---

## Gradle Setup

### `libs.versions.toml`

```toml
[versions]
roborazzi = "1.64.0"

[libraries]
roborazzi             = { module = "io.github.takahirom.roborazzi:roborazzi",             version.ref = "roborazzi" }
roborazzi-compose     = { module = "io.github.takahirom.roborazzi:roborazzi-compose",     version.ref = "roborazzi" }
roborazzi-junit-rule  = { module = "io.github.takahirom.roborazzi:roborazzi-junit-rule",  version.ref = "roborazzi" }

[plugins]
roborazzi = { id = "io.github.takahirom.roborazzi", version.ref = "roborazzi" }
```

### Convention plugin: `GROUP_ID.feature.ui.gradle.kts`

```kotlin
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("io.github.takahirom.roborazzi")
}

roborazzi {
    outputDir = project.file("src/jvmTest/snapshots")
}
```

### Feature `:ui` module `build.gradle.kts`

```kotlin
plugins {
    id("GROUP_ID.feature.ui")
}

kotlin {
    androidLibrary {
        namespace = "GROUP_ID.feature.FEATURE_NAME.ui"
    }

    sourceSets {
        commonTest.dependencies {
            implementation(compose.uiTest)              // runComposeUiTest — commonTest, multiplatform
            implementation(libs.kotlin.test)
        }
        jvmTest.dependencies {
            implementation(libs.roborazzi)               // Roborazzi is JVM/Robolectric-only
            implementation(libs.roborazzi.compose)
            implementation(libs.roborazzi.junit.rule)
        }
    }
}
```

`compose.uiTest` is inherited by every platform test source set (`jvmTest`,
`androidDeviceTest`, `iosSimulatorArm64Test`, `wasmJsTest`) automatically — declare it once
in `commonTest`, not per-target. Android instrumented tests additionally need a minimal
`AndroidManifest.xml` under `src/androidDeviceTest/`; see the official
[Compose Multiplatform testing guide](https://kotlinlang.org/docs/multiplatform/compose-test.html)
for the exact setup.

---

## Step 1: Test Tags

Create a `TestTags` object per feature. Place it in `commonMain` so both production code
and tests can reference the constants without string literals.

```kotlin
// :feature:auth:ui/src/commonMain/kotlin/GROUP_ID/feature/auth/ui/AuthTestTags.kt
package GROUP_ID.feature.auth.ui

object AuthTestTags {
    const val EMAIL_FIELD        = "auth:email_field"
    const val PASSWORD_FIELD     = "auth:password_field"
    const val LOGIN_BUTTON       = "auth:login_button"
    const val LOADING_INDICATOR  = "auth:loading_indicator"
    const val ERROR_MESSAGE      = "auth:error_message"
}
```

**Naming convention**: `<feature>:<node>` — the feature prefix avoids collisions when
multiple features are on screen simultaneously (e.g. in a navigation test).

Apply tags in `AuthContent`:

```kotlin
AppTextField(
    value = state.email,
    onValueChange = { onIntent(AuthContract.Intent.EmailChanged(it)) },
    modifier = Modifier.testTag(AuthTestTags.EMAIL_FIELD),
)

AppTextField(
    value = state.password,
    onValueChange = { onIntent(AuthContract.Intent.PasswordChanged(it)) },
    modifier = Modifier.testTag(AuthTestTags.PASSWORD_FIELD),
    isPassword = true,
)

AppButton(
    onClick = { onIntent(AuthContract.Intent.LoginClicked) },
    enabled = !state.isLoading,
    modifier = Modifier
        .fillMaxWidth()
        .testTag(AuthTestTags.LOGIN_BUTTON),
)

if (state.isLoading) {
    AppSpinner(modifier = Modifier.testTag(AuthTestTags.LOADING_INDICATOR))
}

if (state.error != null) {
    AppText(
        text = state.error,
        modifier = Modifier.testTag(AuthTestTags.ERROR_MESSAGE),
    )
}
```

**Tag what matters** — tag interactive nodes (buttons, fields) and assertable output nodes
(error banners, loading indicators). Don't tag decorative containers.

---

## Step 2: Compose UI Interaction Tests (commonTest)

Full content: `references/step2-compose-ui-interaction-tests.md`.

## Step 3: Roborazzi Screenshot Tests (jvmTest only)

Unlike interaction tests, screenshot tests stay in `jvmTest` — Roborazzi has no
multiplatform equivalent.

```kotlin
// :feature:auth:ui/src/jvmTest/kotlin/GROUP_ID/feature/auth/ui/AuthContentScreenshotTest.kt
package GROUP_ID.feature.auth.ui

import com.github.takahirom.roborazzi.captureRoboImage
import GROUP_ID.core.designsystem.theme.AppTheme
import kotlin.test.Test

class AuthContentScreenshotTest {

    @Test
    fun authContent_default() {
        captureRoboImage("auth_content_default.png") {
            AppTheme {
                AuthContent(state = AuthContract.State(), onIntent = {})
            }
        }
    }

    @Test
    fun authContent_loading() {
        captureRoboImage("auth_content_loading.png") {
            AppTheme {
                AuthContent(state = AuthContract.State(isLoading = true), onIntent = {})
            }
        }
    }

    @Test
    fun authContent_error() {
        captureRoboImage("auth_content_error.png") {
            AppTheme {
                AuthContent(state = AuthContract.State(error = "Session expired"), onIntent = {})
            }
        }
    }

    @Test
    fun authContent_dark() {
        captureRoboImage("auth_content_dark.png") {
            AppTheme(darkTheme = true) {
                AuthContent(state = AuthContract.State(), onIntent = {})
            }
        }
    }
}
```

Each call writes a PNG to `src/jvmTest/snapshots/`.

**Required minimum per screen:**
- `_light` + `_dark` variant for the default state — always, no exceptions
- `_light` + `_dark` variant for each meaningful variant (loading, error, empty)
- If adaptive layout is in use: Compact + Expanded × light + dark = minimum 4 captures

A test with only a light capture is a reviewer blocker (`[THEME]`). A color that
passes visual review in light mode may be invisible or low-contrast in dark mode.

Avoid redundant captures (don't capture every error message — capture the error state
shape once, light and dark).

---

## Step 3b: Bounds Sidecar (exact position/size regression — no vision needed)

Full content: `references/step3b-bounds-sidecar.md`.

## Recording and Verifying Goldens

```bash
# Record (first run — writes golden PNGs to snapshots/)
./gradlew :feature:auth:ui:jvmTest -PrecordRoborazzi

# Verify (diff against committed goldens)
./gradlew :feature:auth:ui:jvmTest

# Verify all :ui modules at once
./gradlew jvmTest
```

Commit the `snapshots/` directory to git — this includes any `.bounds.json` sidecars
written by `captureBoundsSnapshot`, since they live alongside the PNGs in the same
directory. PRs that change UI produce image diffs *and* exact position/size diffs in the
PR review — reviewers see before/after without running tests locally.

---

## CI Integration

Full content: `references/ci-integration.md`.

---

## References

Full implementation content lives in `references/*.md`: `step2-compose-ui-interaction-tests`,
`step3b-bounds-sidecar`, `ci-integration`. Load the specific file named in the pointer
under its matching heading above, not all of them.

---

## Related Skills

- `kmp-compose-preview-driven-development` — the `@Preview` workflow that feeds directly into Roborazzi
- `kmp-presenter-module` — Screen/Content split that makes `Content` injectable with fixed state
- `kmp-unit-testing` — Roborazzi covers `:ui`; use `runTest` + Turbine for `:presenter` and `:domain`
- `kmp-ci-github-actions` — where the CI screenshot job is wired
- `kmp-compose-design-system` — owns the `Style`/token source that border-width and corner-radius regressions are diffed against directly, instead of re-deriving them from a screenshot

---

## Visual Design Audit

After recording new golden images, run `/kmp-audit-screenshots` to verify the goldens themselves
are design-system-compliant — not just pixel-stable. The audit uses Claude vision and checks:

| Category | What is checked |
|---|---|
| Color tokens | No raw `Color(0xFF…)` visible; backgrounds use semantic surface colors |
| Dark mode parity | Dark variant has dark background; text is light-on-dark, not invisible |
| AppScaffold structure | TopAppBar present; title not duplicated in content body; back button in nav slot |
| Spacing | Content has outer padding; list items have consistent internal padding |
| Typography | Body readable; headings distinct; text truncates with ellipsis |
| Contrast | Text on colored backgrounds is readable; disabled states are visually distinct |

Position and size regressions are checked separately, and exactly — `/kmp-audit-screenshots`
diffs any `.bounds.json` sidecar (see "Step 3b: Bounds Sidecar" above) before touching
vision at all, since a sidecar diff is an exact number and vision is an estimate.

Running the audit:
```bash
# After recording new goldens — pass the project root, not the snapshots path
./gradlew recordRoborazziJvm
/kmp-audit-screenshots .
```

`/kmp-audit-screenshots` resolves the output directory dynamically by reading
`roborazzi { outputDir = ... }` from `build.gradle.kts`. If `outputDir` is not set,
it falls back to `src/jvmTest/snapshots/` (jvmTest target) or `src/test/snapshots/`
(Android target). Never hardcode the path — it varies by project configuration.

The audit is also wired into `/kmp-verify` (Step 5) — it runs automatically after `jvmTest`
if new or modified PNGs are present.

Findings map to reviewer blockers: FAIL-level → `[THEME]` or `[LAYOUT]`; WARNING-level → non-blocking.

---

## Common Anti-Patterns

- using Playwright, computer-use tooling, `adb screencap`, `xcrun simctl io`, or `Robot.createScreenCapture` for UI screenshots — use `captureRoboImage` on JVM instead; manually launching and driving the app requires a running device/emulator, produces non-reproducible results, and produces nothing committable. This applies to "verify this UI change" / "check the screen" requests too, not just when explicitly writing a screenshot test — reach for `runComposeUiTest`/`captureRoboImage` before reaching for a live app or a computer-use tool
- using `onNodeWithText("Sign in")` instead of `onNodeWithTag` — breaks when copy changes; always use tags
- tagging the `Screen` composable (with a real ViewModel) — inject fixed state into `Content` instead
- not committing golden images — CI has nothing to diff against; diffs only work with committed goldens
- running Roborazzi on Android instead of JVM — slower, needs emulator; use `jvmTest` unless Android-specific resources are required
- one test class per state instead of one class per component — excessive boilerplate; group all states in one test class
- forgetting to record new goldens after a planned UI change — run `-PrecordRoborazzi` and commit the updated images
- covering only one device size in a feature screenshot test — preview coverage should span phone, tablet, and desktop
- putting test tag constants as bare string literals in the test — define them in `object FooTestTags` in `commonMain`
- using `assertTextContains` for copy that will be localized — use `assertIsDisplayed()` on the tagged node instead
- trying to move Roborazzi screenshot tests to `commonTest` — Roborazzi has no multiplatform equivalent; it depends directly on Robolectric's Android-framework shadow rendering, so it stays JVM-only regardless of where interaction tests live
- adding `iosSimulatorArm64Test`/`connectedAndroidTest` to the required per-PR CI gate — emulator/simulator boot time is expensive; keep the full device matrix opt-in or nightly and `jvmTest` as the required fast gate
- writing new interaction tests with `createComposeRule` + JUnit4 `@get:Rule` in `jvmTest` — use `runComposeUiTest` in `commonTest` instead so the same test body can run per-target
- asking Claude vision to estimate an exact position/size delta from two screenshots — vision isn't precise at exact pixel numbers; capture `.bounds.json` via `captureBoundsSnapshot` instead and diff the text file
- writing a pixel-based border-width or corner-radius detector — those values already exist exactly in the `Style` source (`ButtonStyles.kt`/`CardStyles.kt`); a regression there is a normal code diff, not something to re-derive from an image

If a screenshot test fails after a Compose upgrade, re-record goldens — font rendering shifts between versions.

---

## Output Style

When asked about UI testing, test tags, or visual regression for KMP, respond in this order:
1. `TestTag` object setup with naming convention
2. `Modifier.testTag()` applied to the composable
3. Interaction test with `createComposeRule` + `onNodeWithTag`
4. Roborazzi screenshot test for the same component
5. record/verify commands and CI job

---

## Changelog

| Date | Change |
|---|---|
| 2026-08-04 | Split Step 2 (Compose UI Interaction Tests), Step 3b (Bounds Sidecar), and CI Integration out of SKILL.md into `references/*.md`, leaving pointer stubs plus a new References section. SKILL.md drops from 769 to 430 lines, clearing the agentskills.io 500-line recommendation. No content removed, only relocated. Part of the same backlog cleanup as `kmp-compose-design-system`/`-extended`/`kmp-mvi`/`kmp-feature-scaffold`/`kmp-code-quality`/`kmp-library-publishing`/`kmp-expert`/`kmp-navigation`/`kmp-legal-docs` (KI-008). |
| 2026-07-10 | Added "Step 3b: Bounds Sidecar" — `captureBoundsSnapshot()` writes exact `fetchSemanticsNode().boundsInRoot` position/size to a `.bounds.json` file next to each golden PNG, so a position/size regression is a plain `git diff` on committed text instead of something an agent has to estimate from a pixel diff image. Proven with a standalone JSON-diff test (exact delta surfaced, zero noise for unchanged nodes) before writing this into the skill. Verified the real multiplatform-JVM `captureRoboImage` entry point (`onRoot().captureRoboImage(...)` inside `runDesktopComposeUiTest`) against Roborazzi's own `sample-compose-desktop-jvm` test, since it differs from the plain content-lambda form. Wired into `/kmp-record-design-baselines` (sidecars ride along in the existing `snapshots/` copy step) and `/kmp-audit-screenshots` (new Step 2b checks sidecar diffs before falling through to vision). Explicitly out of scope: pixel-based border-width/corner-radius detection — those values already exist exactly in `Style` source, so a regression there is a normal code diff. 2 new anti-patterns, 1 new Related Skills cross-reference. |
| 2026-07-08 | Added a "Drag interaction test" pattern — `performTouchInput { swipe(...) }` / `performMouseInput { press(); moveTo(); release() }` for resizable panel dividers and custom scrollbar thumbs, asserting resulting state (pane width, clamp bounds, scroll offset) rather than intermediate frames. |
| 2026-07-08 | Added a "Layout stability regression test" pattern — asserting `boundsInRoot()` on a trigger before/after toggle (via `mainClock.advanceTimeBy`) to deterministically catch a collapsible/accordion trigger shifting position on toggle. Cross-links the new `kmp-audit` detectors `toggle icon swap instead of rotation` and `bare conditional collapse`. |
| 2026-07-07 | Moved Compose UI interaction tests from `jvmTest`/`createComposeRule`+JUnit4 to `commonTest`/`runComposeUiTest`, so the same test body runs per-target (JVM, Android instrumented, iOS simulator, Wasm). Roborazzi screenshot tests stay `jvmTest`-only (no multiplatform equivalent — depends on Robolectric shadow rendering). Added an opt-in/nightly CI matrix job alongside the required `jvmTest` gate, updated Gradle setup (`compose.uiTest` in `commonTest.dependencies`), and 3 new anti-patterns. |
| 2026-06-20 | Initial release. |
