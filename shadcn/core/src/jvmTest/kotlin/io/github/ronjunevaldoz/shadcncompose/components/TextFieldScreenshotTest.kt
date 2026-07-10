@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.styles.TextFieldVariant
import kotlin.test.Test

class TextFieldScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("textfield_states", darkTheme = darkTheme) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(TextFieldVariant.Default, TextFieldVariant.Filled, TextFieldVariant.Ghost).forEach { variant ->
                    ShadcnTextField(
                        value = "",
                        onValueChange = {},
                        placeholder = variant::class.simpleName,
                        variant = variant,
                        modifier = Modifier.width(200.dp),
                    )
                }
                ShadcnTextField(
                    value = "Invalid value",
                    onValueChange = {},
                    isError = true,
                    supportingText = "This field is required",
                    modifier = Modifier.width(200.dp),
                )
                ShadcnTextField(
                    value = "Disabled",
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier.width(200.dp),
                )
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)

    @Test
    fun focused_light() {
        snapshotFocused("textfield_focused", focusTag = "tf", darkTheme = false) {
            ShadcnTextField(value = "", onValueChange = {}, modifier = Modifier.width(200.dp).testTag("tf"))
        }
    }

    @Test
    fun focused_dark() {
        snapshotFocused("textfield_focused", focusTag = "tf", darkTheme = true) {
            ShadcnTextField(value = "", onValueChange = {}, modifier = Modifier.width(200.dp).testTag("tf"))
        }
    }
}
