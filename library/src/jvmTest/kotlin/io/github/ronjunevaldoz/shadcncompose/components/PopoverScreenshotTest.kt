@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Box
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class PopoverScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("popover_states", darkTheme = darkTheme) {
            Box {
                ShadcnButton(onClick = {}) { ShadcnText("Open popover") }
                ShadcnPopover(expanded = true, onDismissRequest = {}) {
                    ShadcnText("Place content for the popover here.")
                }
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)
}
