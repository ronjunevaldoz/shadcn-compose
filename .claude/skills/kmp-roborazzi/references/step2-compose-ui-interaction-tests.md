# Step 2: Compose UI Interaction Tests (commonTest)

Part of `kmp-roborazzi`. Load this file when working on: step 2: compose ui interaction tests (commontest).

---

Interaction tests live in `commonTest` and run per-target via `runComposeUiTest` — the
same test body executes under `jvmTest` (required CI gate), and optionally under
`androidDeviceTest`, `iosSimulatorArm64Test`, `wasmJsTest` (opt-in/nightly matrix, see CI
Integration). No JUnit4 `@get:Rule` — `runComposeUiTest` takes a lambda with a
`ComposeUiTest` receiver that exposes the same `onNodeWithTag`/`performClick`/assert API.

```kotlin
// :feature:auth:ui/src/commonTest/kotlin/GROUP_ID/feature/auth/ui/AuthContentInteractionTest.kt
package GROUP_ID.feature.auth.ui

import androidx.compose.ui.test.*
import GROUP_ID.core.designsystem.theme.AppTheme
import kotlin.test.Test

class AuthContentInteractionTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun loginButton_isDisabled_whenLoading() = runComposeUiTest {
        setContent {
            AppTheme {
                AuthContent(
                    state = AuthContract.State(isLoading = true),
                    onIntent = {},
                )
            }
        }
        onNodeWithTag(AuthTestTags.LOGIN_BUTTON).assertIsNotEnabled()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun errorMessage_isDisplayed_whenErrorInState() = runComposeUiTest {
        setContent {
            AppTheme {
                AuthContent(
                    state = AuthContract.State(error = "Invalid credentials"),
                    onIntent = {},
                )
            }
        }
        onNodeWithTag(AuthTestTags.ERROR_MESSAGE)
            .assertIsDisplayed()
            .assertTextContains("Invalid credentials")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun loadingIndicator_isDisplayed_whenLoading() = runComposeUiTest {
        setContent {
            AppTheme {
                AuthContent(
                    state = AuthContract.State(isLoading = true),
                    onIntent = {},
                )
            }
        }
        onNodeWithTag(AuthTestTags.LOADING_INDICATOR).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun loginButton_firesIntent_whenClicked() = runComposeUiTest {
        val intents = mutableListOf<AuthContract.Intent>()
        setContent {
            AppTheme {
                AuthContent(
                    state = AuthContract.State(),
                    onIntent = { intents.add(it) },
                )
            }
        }
        onNodeWithTag(AuthTestTags.LOGIN_BUTTON).performClick()

        assert(intents.contains(AuthContract.Intent.LoginClicked))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun emailField_updatesState_onTextInput() = runComposeUiTest {
        val intents = mutableListOf<AuthContract.Intent>()
        setContent {
            AppTheme {
                AuthContent(
                    state = AuthContract.State(),
                    onIntent = { intents.add(it) },
                )
            }
        }
        onNodeWithTag(AuthTestTags.EMAIL_FIELD).performTextInput("user@example.com")

        assert(
            intents.filterIsInstance<AuthContract.Intent.EmailChanged>()
                .any { it.value == "user@example.com" }
        )
    }
}
```

**Key APIs (unchanged from JUnit4 — only the harness differs):**

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

### Layout stability regression test (trigger position on toggle)

A screenshot golden alone won't reliably catch a trigger button shifting a few pixels
when toggled (collapsible/accordion chevrons, expand/collapse triggers) — diff tools
often tolerate small deltas. Assert the trigger's bounds directly instead:

```kotlin
@OptIn(ExperimentalTestApi::class)
@Test
fun collapsibleTrigger_positionUnchanged_whenToggled() = runComposeUiTest {
    setContent {
        AppTheme { Collapsible(/* ... */) }
    }
    val before = onNodeWithTag(CollapsibleTestTags.TRIGGER).fetchSemanticsNode().boundsInRoot
    onNodeWithTag(CollapsibleTestTags.TRIGGER).performClick()
    mainClock.advanceTimeBy(250)  // past the toggle animation's tween duration
    val after = onNodeWithTag(CollapsibleTestTags.TRIGGER).fetchSemanticsNode().boundsInRoot

    assertEquals(before, after)
}
```

This is the deterministic version of the two failure modes the `kmp-audit`
detectors `toggle icon swap instead of rotation [MEDIUM]` and `bare conditional collapse
[MEDIUM]` catch statically — write this test alongside any collapsible/accordion trigger,
not just for components that already broke once.

### Drag interaction test (resizable panels, custom scrollbar thumbs)

`runComposeUiTest` doesn't have a `Modifier.draggable`-specific assertion — drive the
gesture with `performTouchInput { swipe(...) }` (or `performMouseInput { press(); moveTo();
release() }` for Desktop-only pointer drags) and assert on the resulting state, not on
intermediate frames:

```kotlin
@OptIn(ExperimentalTestApi::class)
@Test
fun resizablePanelGroup_drag_resizesStartPaneWithinBounds() = runComposeUiTest {
    setContent {
        AppTheme { AppResizablePanelGroup(start = { StartPane() }, end = { EndPane() }) }
    }
    val divider = onNodeWithTag(ResizablePanelTestTags.DIVIDER)
    val startBoundsBefore = onNodeWithTag(ResizablePanelTestTags.START_PANE)
        .fetchSemanticsNode().boundsInRoot

    divider.performTouchInput { swipe(start = center, end = center.copy(x = center.x + 100f)) }

    val startBoundsAfter = onNodeWithTag(ResizablePanelTestTags.START_PANE)
        .fetchSemanticsNode().boundsInRoot
    assertTrue(startBoundsAfter.width > startBoundsBefore.width)
}
```

Test the clamp explicitly, not just "dragging changes something" — swipe far past the
divider's travel range and assert the pane width stops at `minWeight`/`maxWeight` rather
than continuing to shrink/grow or overshooting past the container:

```kotlin
@OptIn(ExperimentalTestApi::class)
@Test
fun resizablePanelGroup_drag_clampsToMaxWeight() = runComposeUiTest {
    setContent {
        AppTheme {
            AppResizablePanelGroup(start = { StartPane() }, end = { EndPane() }, maxWeight = 0.85f)
        }
    }
    onNodeWithTag(ResizablePanelTestTags.DIVIDER)
        .performTouchInput { swipe(start = center, end = center.copy(x = center.x + 5000f)) }

    val rootWidth = onRoot().fetchSemanticsNode().boundsInRoot.width
    val startWidth = onNodeWithTag(ResizablePanelTestTags.START_PANE).fetchSemanticsNode().boundsInRoot.width
    assertTrue(startWidth <= rootWidth * 0.85f + 1f)  // +1f tolerance for rounding
}
```

The same `performTouchInput { swipe(...) }` pattern applies to a custom scrollbar thumb
built with `pointerInput` — assert the underlying `ScrollState.value` (or
`LazyListState.firstVisibleItemIndex`) changed after the drag, not the thumb's own pixel
position, since the thumb's position is derived from scroll state, not the other way
around.

---

