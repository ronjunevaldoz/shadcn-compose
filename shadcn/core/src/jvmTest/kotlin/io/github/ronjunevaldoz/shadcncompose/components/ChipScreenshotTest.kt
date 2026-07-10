@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.styles.ChipVariant
import kotlin.test.Test

class ChipScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("chip_states", darkTheme = darkTheme) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ShadcnChip(label = "Default", selected = false)
                ShadcnChip(label = "Selected", selected = true)
                ShadcnChip(label = "Outline", variant = ChipVariant.Outline)
                ShadcnChip(label = "Disabled", enabled = false)
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)

    @Test
    fun focused_light() {
        snapshotFocused("chip_focused", focusTag = "chip", darkTheme = false) {
            ShadcnChip(label = "Focus me", onClick = {}, modifier = Modifier.testTag("chip"))
        }
    }

    @Test
    fun focused_dark() {
        snapshotFocused("chip_focused", focusTag = "chip", darkTheme = true) {
            ShadcnChip(label = "Focus me", onClick = {}, modifier = Modifier.testTag("chip"))
        }
    }
}
