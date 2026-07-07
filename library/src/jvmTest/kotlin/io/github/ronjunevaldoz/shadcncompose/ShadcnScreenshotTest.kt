package io.github.ronjunevaldoz.shadcncompose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasRequestFocusAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnStylePreset
import io.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule

/**
 * Base class for Roborazzi screenshot tests. Runs Robolectric-less on the JVM/Desktop
 * target via `roborazzi-compose-desktop` -- there is no Android/Robolectric dependency
 * anywhere in this module, matching :library's plain jvm() target.
 *
 * Every capture goes through [snapshot] or [snapshotFocused] so every golden is themed
 * consistently and named `<name>_<light|dark>.png`, per the project convention that a
 * screenshot test with only a light-mode capture is incomplete.
 */
abstract class ShadcnScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    // The Gradle plugin's `roborazzi { outputDir = ... }` targets the classic
    // Robolectric path; the Robolectric-less desktop capture used here just resolves
    // the filename passed to `captureRoboImage` against the module's working
    // directory, so the snapshots/ prefix is applied by hand here instead.
    private val snapshotDir = "src/jvmTest/snapshots"

    /** Renders [content] inside a themed, padded background and captures it as `<name>_<light|dark>.png`. */
    fun snapshot(
        name: String,
        darkTheme: Boolean = false,
        stylePreset: ShadcnStylePreset = ShadcnStylePreset.Vega,
        content: @Composable () -> Unit,
    ) {
        composeRule.setContent {
            ShadcnSnapshotSurface(darkTheme, stylePreset, content)
        }
        // Lets finite transitions (AnimatedVisibility, animateFloatAsState, ...) settle
        // before capture -- Compose UI Test explicitly excludes InfiniteTransition-driven
        // animations (Skeleton's pulse, Spinner's rotation) from idle-detection, so this
        // never blocks on those.
        composeRule.waitForIdle()
        captureCompositeRoot().captureRoboImage("$snapshotDir/$name${themeSuffix(darkTheme)}.png")
    }

    /**
     * Like [snapshot], but requests real system focus on the node tagged [focusTag] before
     * capturing -- this exercises the same focus/interaction pipeline a real user would
     * drive, rather than special-casing a "forced focused" prop on the component under test.
     *
     * [focusTag] doesn't have to sit on the exact focusable node: some components (e.g.
     * `ShadcnTextField`, `ShadcnTextarea`, `ShadcnSlider`) apply `modifier` to an outer
     * layout wrapper around the real focus target (a `BasicTextField`, a drag handle), so
     * this searches [focusTag]'s own subtree for whichever descendant actually exposes the
     * `RequestFocus` semantics action.
     */
    fun snapshotFocused(
        name: String,
        focusTag: String,
        darkTheme: Boolean = false,
        stylePreset: ShadcnStylePreset = ShadcnStylePreset.Vega,
        content: @Composable () -> Unit,
    ) {
        composeRule.setContent {
            ShadcnSnapshotSurface(darkTheme, stylePreset, content)
        }
        val focusable = hasRequestFocusAction() and (hasTestTag(focusTag) or hasAnyAncestor(hasTestTag(focusTag)))
        composeRule.onNode(focusable, useUnmergedTree = true).requestFocus()
        composeRule.waitForIdle()
        captureCompositeRoot().captureRoboImage("$snapshotDir/$name${themeSuffix(darkTheme)}.png")
    }

    /** Captures the *already-composed* current tree -- for tests that drive multiple sequential captures off one `setContent` call. */
    fun captureNamed(
        name: String,
        darkTheme: Boolean,
    ) {
        captureCompositeRoot().captureRoboImage("$snapshotDir/$name${themeSuffix(darkTheme)}.png")
    }

    /**
     * A `Popup` (used by every overlay component -- see the `overlay` package) opens
     * its own semantics root on top of the base content's root, so `onRoot()` throws
     * ("expected exactly 1 root, found 2+") once one is showing. Verified empirically
     * (see the deleted `PopupCaptureSpikeTest` spike): the *last* root reported by
     * `onAllNodes(isRoot())` is always the one representing the fully composited
     * window -- base content plus every open overlay layered on top, exactly what a
     * real screenshot should show -- so always capturing the last root is correct
     * whether zero, one, or several Popups are open.
     */
    private fun captureCompositeRoot(): SemanticsNodeInteraction {
        val roots = composeRule.onAllNodes(isRoot())
        val lastIndex = roots.fetchSemanticsNodes().size - 1
        return roots[lastIndex]
    }

    private fun themeSuffix(darkTheme: Boolean) = if (darkTheme) "_dark" else "_light"
}

@Composable
private fun ShadcnSnapshotSurface(
    darkTheme: Boolean,
    stylePreset: ShadcnStylePreset,
    content: @Composable () -> Unit,
) {
    ShadcnTheme(preset = stylePreset, isDark = darkTheme) {
        Box(modifier = Modifier.background(shadcnTheme.colors.background).padding(24.dp)) {
            content()
        }
    }
}
