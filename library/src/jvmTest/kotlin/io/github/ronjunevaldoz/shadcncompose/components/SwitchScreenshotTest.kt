@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class SwitchScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("switch_states", darkTheme = darkTheme) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ShadcnSwitch(checked = false, onCheckedChange = {})
                ShadcnSwitch(checked = true, onCheckedChange = {})
                ShadcnSwitch(checked = false, onCheckedChange = {}, enabled = false)
                ShadcnSwitch(checked = true, onCheckedChange = {}, enabled = false)
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)

    @Test
    fun focused_light() {
        snapshotFocused("switch_focused", focusTag = "sw", darkTheme = false) {
            ShadcnSwitch(checked = false, onCheckedChange = {}, modifier = Modifier.testTag("sw"))
        }
    }

    @Test
    fun focused_dark() {
        snapshotFocused("switch_focused", focusTag = "sw", darkTheme = true) {
            ShadcnSwitch(checked = false, onCheckedChange = {}, modifier = Modifier.testTag("sw"))
        }
    }
}
