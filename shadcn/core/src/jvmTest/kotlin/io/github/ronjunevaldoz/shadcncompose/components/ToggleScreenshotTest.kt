@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.styles.ToggleVariant
import kotlin.test.Test

class ToggleScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("toggle_states", darkTheme = darkTheme) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(ToggleVariant.Default, ToggleVariant.Outline).forEach { variant ->
                    ShadcnToggle(pressed = false, onPressedChange = {}, variant = variant) { ShadcnText("Off") }
                    ShadcnToggle(pressed = true, onPressedChange = {}, variant = variant) { ShadcnText("On") }
                    ShadcnToggle(
                        pressed = false,
                        onPressedChange = {},
                        variant = variant,
                        enabled = false,
                    ) { ShadcnText("Disabled") }
                }
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)

    @Test
    fun focused_light() {
        snapshotFocused("toggle_focused", focusTag = "tg", darkTheme = false) {
            ShadcnToggle(
                pressed = false,
                onPressedChange = {},
                modifier = Modifier.testTag("tg"),
            ) { ShadcnText("Focus me") }
        }
    }

    @Test
    fun focused_dark() {
        snapshotFocused("toggle_focused", focusTag = "tg", darkTheme = true) {
            ShadcnToggle(
                pressed = false,
                onPressedChange = {},
                modifier = Modifier.testTag("tg"),
            ) { ShadcnText("Focus me") }
        }
    }
}
