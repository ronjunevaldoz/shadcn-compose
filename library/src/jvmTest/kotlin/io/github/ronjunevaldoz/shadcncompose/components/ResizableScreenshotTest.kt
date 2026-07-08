package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import kotlin.test.Test

/**
 * Covers the *rendering* half of resizing -- does `weight(fraction)` actually produce the
 * right pane proportions -- via fixed `initialFraction` values instead of simulating a
 * live drag gesture (which has previously hung this project's JVM test worker, see
 * `resizablePanelFraction`'s doc comment). The drag *math* itself is unit tested directly
 * in `ShadcnResizablePanelGroupTest`, with no Compose/gesture involvement at all.
 */
class ResizableScreenshotTest : ShadcnScreenshotTest() {
    private fun split(
        darkTheme: Boolean,
        fraction: Float,
        suffix: String,
    ) {
        snapshot("resizable_$suffix", darkTheme = darkTheme) {
            ShadcnResizablePanelGroup(modifier = Modifier.height(120.dp), initialFraction = fraction) {
                    first, second, onHandleDrag,
                ->
                Box(first, contentAlignment = Alignment.Center) { ShadcnText("One") }
                ShadcnResizableHandle(onDrag = onHandleDrag)
                Box(second, contentAlignment = Alignment.Center) { ShadcnText("Two") }
            }
        }
    }

    private fun sidebarAndContent(darkTheme: Boolean) {
        snapshot("resizable_sidebar_and_content", darkTheme = darkTheme) {
            ShadcnResizablePanelGroup(
                modifier = Modifier.width(320.dp).height(160.dp),
                initialFraction = 0.35f,
            ) { first, second, onHandleDrag ->
                Box(
                    first.background(shadcnTheme.colors.muted),
                    contentAlignment = Alignment.Center,
                ) { ShadcnText("Sidebar", muted = true) }
                ShadcnResizableHandle(onDrag = onHandleDrag)
                Box(second, contentAlignment = Alignment.Center) { ShadcnText("Content") }
            }
        }
    }

    @Test fun states_light() = split(darkTheme = false, fraction = 0.5f, suffix = "states")

    @Test fun states_dark() = split(darkTheme = true, fraction = 0.5f, suffix = "states")

    @Test fun narrow_first_pane_light() = split(darkTheme = false, fraction = 0.25f, suffix = "narrow_first")

    @Test fun wide_first_pane_light() = split(darkTheme = false, fraction = 0.75f, suffix = "wide_first")

    @Test fun sidebar_and_content_light() = sidebarAndContent(darkTheme = false)

    @Test fun sidebar_and_content_dark() = sidebarAndContent(darkTheme = true)
}
