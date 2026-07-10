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

class TextareaScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("textarea_states", darkTheme = darkTheme) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ShadcnTextarea(
                    value = "",
                    onValueChange = {},
                    placeholder = "Type here...",
                    modifier = Modifier.width(220.dp),
                )
                ShadcnTextarea(
                    value = "Invalid",
                    onValueChange = {},
                    isError = true,
                    supportingText = "This field is required",
                    modifier = Modifier.width(220.dp),
                )
                ShadcnTextarea(
                    value = "Disabled",
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier.width(220.dp),
                )
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)

    @Test
    fun focused_light() {
        snapshotFocused("textarea_focused", focusTag = "ta", darkTheme = false) {
            ShadcnTextarea(value = "", onValueChange = {}, modifier = Modifier.width(220.dp).testTag("ta"))
        }
    }

    @Test
    fun focused_dark() {
        snapshotFocused("textarea_focused", focusTag = "ta", darkTheme = true) {
            ShadcnTextarea(value = "", onValueChange = {}, modifier = Modifier.width(220.dp).testTag("ta"))
        }
    }
}
