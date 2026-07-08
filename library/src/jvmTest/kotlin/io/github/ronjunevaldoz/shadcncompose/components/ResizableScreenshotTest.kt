package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

/** Idle (undragged) split only -- dragging the handle isn't simulated, matching the project's avoidance of continuous gesture simulation in this test harness. */
class ResizableScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("resizable_states", darkTheme = darkTheme) {
            ShadcnResizablePanelGroup(modifier = Modifier.height(120.dp)) { first, second, onHandleDrag ->
                Box(first, contentAlignment = Alignment.Center) { ShadcnText("One") }
                ShadcnResizableHandle(onDrag = onHandleDrag)
                Box(second, contentAlignment = Alignment.Center) { ShadcnText("Two") }
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)
}
