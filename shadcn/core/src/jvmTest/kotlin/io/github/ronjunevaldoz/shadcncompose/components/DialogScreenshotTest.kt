@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class DialogScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("dialog_states", darkTheme = darkTheme) {
            ShadcnDialog(visible = true, onDismissRequest = {}) {
                ShadcnDialogHeader {
                    ShadcnDialogTitle("Edit profile")
                    ShadcnDialogDescription("Make changes to your profile here. Click save when you're done.")
                }
                ShadcnDialogFooter {
                    ShadcnButton(onClick = {}) { ShadcnText("Save changes") }
                }
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)

    private fun closeButtonFocused(darkTheme: Boolean) {
        snapshotFocused("dialog_close_button_focused", focusTag = "dialog", darkTheme = darkTheme) {
            ShadcnDialog(visible = true, onDismissRequest = {}, modifier = Modifier.testTag("dialog")) {
                ShadcnDialogHeader {
                    ShadcnDialogTitle("Edit profile")
                }
            }
        }
    }

    @Test fun close_button_focused_light() = closeButtonFocused(darkTheme = false)
}
