@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import kotlin.test.Test

class AlertDialogScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("alert_dialog_states", darkTheme = darkTheme) {
            ShadcnAlertDialog(visible = true, onDismissRequest = {}) {
                ShadcnDialogHeader {
                    ShadcnDialogTitle("Are you absolutely sure?")
                    ShadcnDialogDescription("This action cannot be undone. This will permanently delete your account.")
                }
                ShadcnDialogFooter {
                    ShadcnButton(onClick = {}, variant = ButtonVariant.Outline) { ShadcnText("Cancel") }
                    ShadcnButton(onClick = {}, variant = ButtonVariant.Destructive) { ShadcnText("Continue") }
                }
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)
}
