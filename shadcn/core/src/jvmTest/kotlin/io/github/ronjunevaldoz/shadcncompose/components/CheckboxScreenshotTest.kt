@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class CheckboxScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("checkbox_states", darkTheme = darkTheme) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ShadcnCheckbox(checked = false, onCheckedChange = {})
                ShadcnCheckbox(checked = true, onCheckedChange = {})
                ShadcnCheckbox(checked = false, indeterminate = true, onCheckedChange = {})
                ShadcnCheckbox(checked = false, onCheckedChange = {}, enabled = false)
                ShadcnCheckbox(checked = true, onCheckedChange = {}, enabled = false)
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)

    @Test
    fun focused_light() {
        snapshotFocused("checkbox_focused", focusTag = "cb", darkTheme = false) {
            ShadcnCheckbox(checked = false, onCheckedChange = {}, modifier = Modifier.testTag("cb"))
        }
    }

    @Test
    fun focused_dark() {
        snapshotFocused("checkbox_focused", focusTag = "cb", darkTheme = true) {
            ShadcnCheckbox(checked = false, onCheckedChange = {}, modifier = Modifier.testTag("cb"))
        }
    }
}
