---
name: kotlin-multiplatform-roborazzi
description: >
  Complete UI layer testing for KMP: semantic test tags on composables, Compose UI
  interaction tests (createComposeRule, onNodeWithTag, performClick, assertIsDisplayed),
  and Roborazzi screenshot tests that capture @Preview composables on JVM/Desktop for
  visual regression detection. Covers the full stack from testTag conventions to CI golden
  image diffs. Replaces kotlin-multiplatform-testing-robot for UI regression testing.
license: Apache-2.0
metadata:
  author: kmm-agent-skills
  last-updated: '2026-06-20'
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
    - onNodeWithTag
    - interaction test
    - semantics
    - visual accuracy
    - layout test
    - canvas test
    - arrangement test
    - pixel-perfect
    - layout regression
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
screenshot testing, visual regression testing, UI coverage, test composable.

**Freshness rule:** Roborazzi is actively developed — the Gradle plugin API and the
`captureRoboImage` API change between minor versions. Recheck the GitHub releases page before
pinning a version. The JetBrains Compose UI test dependency (`compose.uiTestJUnit4`) tracks
the Compose Multiplatform version.

---

## Recommendation First

Default to this three-layer UI testing stack:

1. **Test tags** on every interactive or assertable node — `Modifier.testTag(FooTestTags.LOGIN_BUTTON)`
2. **Interaction tests** with `createComposeRule` — verify behaviour (enabled/disabled, text shown, clicks fire)
3. **Roborazzi screenshot tests** — verify visual output (layout, color, loading/error/empty states)
   For feature `Content` screens, cover phone, tablet, and desktop sizes, and record both
   light and dark themes when the screen supports them.

Why:
- Test tags make tests stable — `onNodeWithTag` doesn't break when copy changes
- Interaction tests run on JVM via `jvmTest`, no emulator needed
- Roborazzi goldens catch unintentional visual regressions that logic tests miss
- All three layers share the same `:ui` module and the same `jvmTest` task

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
        jvmTest.dependencies {
            implementation(compose.uiTestJUnit4)       // Compose UI test rule
            implementation(libs.roborazzi)
            implementation(libs.roborazzi.compose)
            implementation(libs.roborazzi.junit.rule)
            implementation(libs.kotlin.test)
        }
    }
}
```

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

## Step 2: Compose UI Interaction Tests

```kotlin
// :feature:auth:ui/src/jvmTest/kotlin/GROUP_ID/feature/auth/ui/AuthContentInteractionTest.kt
package GROUP_ID.feature.auth.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import GROUP_ID.core.designsystem.theme.AppTheme
import kotlin.test.Test
import org.junit.Rule

class AuthContentInteractionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginButton_isDisabled_whenLoading() {
        composeTestRule.setContent {
            AppTheme {
                AuthContent(
                    state = AuthContract.State(isLoading = true),
                    onIntent = {},
                )
            }
        }
        composeTestRule
            .onNodeWithTag(AuthTestTags.LOGIN_BUTTON)
            .assertIsNotEnabled()
    }

    @Test
    fun errorMessage_isDisplayed_whenErrorInState() {
        composeTestRule.setContent {
            AppTheme {
                AuthContent(
                    state = AuthContract.State(error = "Invalid credentials"),
                    onIntent = {},
                )
            }
        }
        composeTestRule
            .onNodeWithTag(AuthTestTags.ERROR_MESSAGE)
            .assertIsDisplayed()
            .assertTextContains("Invalid credentials")
    }

    @Test
    fun loadingIndicator_isDisplayed_whenLoading() {
        composeTestRule.setContent {
            AppTheme {
                AuthContent(
                    state = AuthContract.State(isLoading = true),
                    onIntent = {},
                )
            }
        }
        composeTestRule
            .onNodeWithTag(AuthTestTags.LOADING_INDICATOR)
            .assertIsDisplayed()
    }

    @Test
    fun loginButton_firesIntent_whenClicked() {
        val intents = mutableListOf<AuthContract.Intent>()
        composeTestRule.setContent {
            AppTheme {
                AuthContent(
                    state = AuthContract.State(),
                    onIntent = { intents.add(it) },
                )
            }
        }
        composeTestRule
            .onNodeWithTag(AuthTestTags.LOGIN_BUTTON)
            .performClick()

        assert(intents.contains(AuthContract.Intent.LoginClicked))
    }

    @Test
    fun emailField_updatesState_onTextInput() {
        val intents = mutableListOf<AuthContract.Intent>()
        composeTestRule.setContent {
            AppTheme {
                AuthContent(
                    state = AuthContract.State(),
                    onIntent = { intents.add(it) },
                )
            }
        }
        composeTestRule
            .onNodeWithTag(AuthTestTags.EMAIL_FIELD)
            .performTextInput("user@example.com")

        assert(
            intents.filterIsInstance<AuthContract.Intent.EmailChanged>()
                .any { it.value == "user@example.com" }
        )
    }
}
```

**Key APIs:**

| API | Use |
|---|---|
| `onNodeWithTag(tag)` | Target a node by semantic tag |
| `assertIsDisplayed()` | Node is visible on screen |
| `assertIsNotEnabled()` | Node is disabled |
| `assertTextContains(text)` | Node contains the given text |
| `performClick()` | Simulate a tap |
| `performTextInput(text)` | Type into a text field |
| `assertDoesNotExist()` | Node is not in the composition |
| `onNodeWithTag(tag, useUnmergedTree = true)` | Target inside a merged semantics tree |

---

## Step 3: Roborazzi Screenshot Tests

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

## Recording and Verifying Goldens

```bash
# Record (first run — writes golden PNGs to snapshots/)
./gradlew :feature:auth:ui:jvmTest -PrecordRoborazzi

# Verify (diff against committed goldens)
./gradlew :feature:auth:ui:jvmTest

# Verify all :ui modules at once
./gradlew jvmTest
```

Commit the `snapshots/` directory to git. PRs that change UI produce image diffs in the
PR review — reviewers see before/after without running tests locally.

---

## CI Integration

```yaml
# .github/workflows/ci.yml
test-screenshot:
  name: Screenshot Tests (JVM)
  runs-on: ubuntu-latest
  needs: lint
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

    - name: Run UI + screenshot tests
      run: ./gradlew jvmTest

    - name: Upload screenshot diffs on failure
      if: failure()
      uses: actions/upload-artifact@v4
      with:
        name: screenshot-diffs
        path: '**/src/jvmTest/snapshots/**/*_compare.png'
        retention-days: 7
```

The same `jvmTest` task runs both interaction tests and screenshot tests — one CI step
covers the entire UI layer.

---

## Related Skills

- `kotlin-multiplatform-preview-driven-development` — the `@Preview` workflow that feeds directly into Roborazzi
- `kotlin-multiplatform-presenter-module` — Screen/Content split that makes `Content` injectable with fixed state
- `kotlin-multiplatform-unit-testing` — Roborazzi covers `:ui`; use `runTest` + Turbine for `:presenter` and `:domain`
- `kotlin-multiplatform-ci-github-actions` — where the CI screenshot job is wired

---

## Visual Design Audit

After recording new golden images, run `/kmm-audit-screenshots` to verify the goldens themselves
are design-system-compliant — not just pixel-stable. The audit uses Claude vision and checks:

| Category | What is checked |
|---|---|
| Color tokens | No raw `Color(0xFF…)` visible; backgrounds use semantic surface colors |
| Dark mode parity | Dark variant has dark background; text is light-on-dark, not invisible |
| AppScaffold structure | TopAppBar present; title not duplicated in content body; back button in nav slot |
| Spacing | Content has outer padding; list items have consistent internal padding |
| Typography | Body readable; headings distinct; text truncates with ellipsis |
| Contrast | Text on colored backgrounds is readable; disabled states are visually distinct |

Running the audit:
```bash
# After recording new goldens — pass the project root, not the snapshots path
./gradlew recordRoborazziJvm
/kmm-audit-screenshots .
```

`/kmm-audit-screenshots` resolves the output directory dynamically by reading
`roborazzi { outputDir = ... }` from `build.gradle.kts`. If `outputDir` is not set,
it falls back to `src/jvmTest/snapshots/` (jvmTest target) or `src/test/snapshots/`
(Android target). Never hardcode the path — it varies by project configuration.

The audit is also wired into `/kmm-verify` (Step 5) — it runs automatically after `jvmTest`
if new or modified PNGs are present.

Findings map to reviewer blockers: FAIL-level → `[THEME]` or `[LAYOUT]`; WARNING-level → non-blocking.

---

## Common Anti-Patterns

- using Playwright, `adb screencap`, `xcrun simctl io`, or `Robot.createScreenCapture` for UI screenshots — use `captureRoboImage` on JVM instead; system capture requires a running device/emulator, produces non-reproducible results, and is flagged by `audit_project.py`
- using `onNodeWithText("Sign in")` instead of `onNodeWithTag` — breaks when copy changes; always use tags
- tagging the `Screen` composable (with a real ViewModel) — inject fixed state into `Content` instead
- not committing golden images — CI has nothing to diff against; diffs only work with committed goldens
- running Roborazzi on Android instead of JVM — slower, needs emulator; use `jvmTest` unless Android-specific resources are required
- one test class per state instead of one class per component — excessive boilerplate; group all states in one test class
- forgetting to record new goldens after a planned UI change — run `-PrecordRoborazzi` and commit the updated images
- covering only one device size in a feature screenshot test — preview coverage should span phone, tablet, and desktop
- putting test tag constants as bare string literals in the test — define them in `object FooTestTags` in `commonMain`
- using `assertTextContains` for copy that will be localized — use `assertIsDisplayed()` on the tagged node instead

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
| 2026-06-20 | Initial release. |
