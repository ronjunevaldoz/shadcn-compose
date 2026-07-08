@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class SheetScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("sheet_states", darkTheme = darkTheme) {
            ShadcnSheet(visible = true, onDismissRequest = {}, side = ShadcnSheetSide.End) {
                ShadcnDialogTitle("Edit profile")
                ShadcnDialogDescription("Make changes to your profile here. Click save when you're done.")
            }
        }
    }

    private fun editProfile(darkTheme: Boolean) {
        snapshot("sheet_edit_profile", darkTheme = darkTheme) {
            ShadcnSheet(visible = true, onDismissRequest = {}, side = ShadcnSheetSide.End) {
                ShadcnDialogTitle("Edit profile")
                ShadcnDialogDescription("Make changes to your profile here. Click save when you're done.")
                ShadcnFieldGroup(modifier = Modifier.padding(top = 16.dp)) {
                    ShadcnField {
                        ShadcnFieldLabel("Name")
                        ShadcnTextField(value = "Pedro Duarte", onValueChange = {})
                    }
                    ShadcnField {
                        ShadcnFieldLabel("Username")
                        ShadcnTextField(value = "@peduarte", onValueChange = {})
                    }
                }
                ShadcnDialogFooter(modifier = Modifier.padding(top = 16.dp)) {
                    ShadcnButton(onClick = {}) { ShadcnText("Save changes") }
                }
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)

    @Test fun edit_profile_light() = editProfile(darkTheme = false)

    @Test fun edit_profile_dark() = editProfile(darkTheme = true)
}
