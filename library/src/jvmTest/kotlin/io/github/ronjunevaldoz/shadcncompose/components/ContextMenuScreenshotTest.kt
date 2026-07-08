@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import kotlin.test.Test

/**
 * Only the idle (no menu open) state -- the open-panel appearance reuses the exact
 * same [DropdownMenuRow]/Popup rendering already proven correct by
 * [DropdownMenuScreenshotTest]; the one genuinely new piece ([ShadcnPointPositionProvider])
 * has its own direct math tests in `ShadcnPopupPositionProviderTest`. Right-click gesture
 * simulation wasn't attempted here after `performMouseInput { moveTo(...) }` hung the
 * test worker in [TooltipScreenshotTest] -- not worth the same risk for a state that's
 * already covered another way.
 */
class ContextMenuScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("context_menu_idle", darkTheme = darkTheme) {
            ShadcnContextMenu(items = listOf(ShadcnDropdownMenuItem("Back", onClick = {}))) {
                Box(
                    modifier =
                        Modifier
                            .size(width = 240.dp, height = 80.dp)
                            .background(shadcnTheme.colors.muted, RoundedCornerShape(shadcnTheme.shapes.md)),
                    contentAlignment = Alignment.Center,
                ) {
                    ShadcnText("Right-click here", muted = true)
                }
            }
        }
    }

    @Test fun idle_light() = states(darkTheme = false)

    @Test fun idle_dark() = states(darkTheme = true)
}
