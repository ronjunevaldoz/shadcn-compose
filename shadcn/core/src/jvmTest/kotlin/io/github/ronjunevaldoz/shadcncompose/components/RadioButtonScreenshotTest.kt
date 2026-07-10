@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class RadioButtonScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("radio_states", darkTheme = darkTheme) {
            ShadcnRadioGroup {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ShadcnRadioButton(selected = false, onClick = {})
                    ShadcnRadioButton(selected = true, onClick = {})
                    ShadcnRadioButton(selected = false, onClick = {}, enabled = false)
                    ShadcnRadioButton(selected = true, onClick = {}, enabled = false)
                }
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)

    @Test
    fun focused_light() {
        snapshotFocused("radio_focused", focusTag = "rb", darkTheme = false) {
            ShadcnRadioButton(selected = false, onClick = {}, modifier = Modifier.testTag("rb"))
        }
    }

    @Test
    fun focused_dark() {
        snapshotFocused("radio_focused", focusTag = "rb", darkTheme = true) {
            ShadcnRadioButton(selected = false, onClick = {}, modifier = Modifier.testTag("rb"))
        }
    }
}
