package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.ui.Modifier
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.styles.shadcnShimmer
import kotlin.test.Test

/**
 * `Modifier.shadcnShimmer()` drives an [androidx.compose.animation.core.InfiniteTransition],
 * whose real-clock frame position at capture time is otherwise uncontrolled -- a plain
 * [snapshot] call here caught the sweep off-screen more often than not, producing a golden
 * that was indistinguishable from plain unstyled text (confirmed by looking at it, not
 * assumed). Freezing [ShadcnScreenshotTest.composeRule]'s `mainClock` and advancing it to
 * exactly half the sweep's `durationMillis` guarantees the highlight band sits mid-text
 * every time, deterministically.
 */
class ShimmerScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        composeRule.mainClock.autoAdvance = false
        setThemedContent(darkTheme = darkTheme) {
            ShadcnText("Generating response…", muted = true, modifier = Modifier.shadcnShimmer())
        }
        composeRule.mainClock.advanceTimeBy(1000L)
        captureNamed("shimmer_states", darkTheme = darkTheme)
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)
}
