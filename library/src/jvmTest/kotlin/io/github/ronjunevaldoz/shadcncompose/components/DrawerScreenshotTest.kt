@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

/**
 * Idle (undragged) states only, one per direction -- the swipe-to-dismiss gesture isn't
 * simulated live, matching this project's avoidance of continuous gesture simulation in
 * this test harness (see `shouldDismissDrawer`'s doc comment). That decision math is
 * unit tested directly in `ShadcnDrawerTest` instead.
 */
class DrawerScreenshotTest : ShadcnScreenshotTest() {
    private fun states(
        darkTheme: Boolean,
        direction: ShadcnDrawerDirection,
        suffix: String,
    ) {
        snapshot("drawer_$suffix", darkTheme = darkTheme) {
            ShadcnDrawer(visible = true, onDismissRequest = {}, direction = direction) {
                ShadcnDialogTitle("Edit profile")
                ShadcnDialogDescription("Make changes to your profile here.")
            }
        }
    }

    @Test fun bottom_light() = states(darkTheme = false, direction = ShadcnDrawerDirection.Bottom, suffix = "bottom")

    @Test fun bottom_dark() = states(darkTheme = true, direction = ShadcnDrawerDirection.Bottom, suffix = "bottom")

    @Test fun top_light() = states(darkTheme = false, direction = ShadcnDrawerDirection.Top, suffix = "top")

    @Test fun end_light() = states(darkTheme = false, direction = ShadcnDrawerDirection.End, suffix = "end")
}
