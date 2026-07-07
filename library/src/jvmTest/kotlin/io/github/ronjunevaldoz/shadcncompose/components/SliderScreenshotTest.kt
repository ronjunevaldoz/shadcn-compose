@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class SliderScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("slider_states", darkTheme = darkTheme) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ShadcnSlider(value = 0f, onValueChange = {}, modifier = Modifier.width(160.dp))
                ShadcnSlider(value = 0.5f, onValueChange = {}, modifier = Modifier.width(160.dp))
                ShadcnSlider(value = 1f, onValueChange = {}, modifier = Modifier.width(160.dp))
                ShadcnSlider(value = 0.5f, onValueChange = {}, enabled = false, modifier = Modifier.width(160.dp))
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)

    @Test
    fun focused_light() {
        snapshotFocused("slider_focused", focusTag = "sl", darkTheme = false) {
            ShadcnSlider(value = 0.5f, onValueChange = {}, modifier = Modifier.width(160.dp).testTag("sl"))
        }
    }

    @Test
    fun focused_dark() {
        snapshotFocused("slider_focused", focusTag = "sl", darkTheme = true) {
            ShadcnSlider(value = 0.5f, onValueChange = {}, modifier = Modifier.width(160.dp).testTag("sl"))
        }
    }
}
