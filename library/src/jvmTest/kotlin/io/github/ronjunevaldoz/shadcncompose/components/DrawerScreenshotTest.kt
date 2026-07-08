@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

/**
 * Idle (undragged) states only, one per direction -- the swipe-to-dismiss gesture isn't
 * simulated live, matching this project's avoidance of continuous gesture simulation in
 * this test harness (see `shouldDismissDrawer`'s doc comment). That decision math is
 * unit tested directly in `ShadcnDrawerTest` instead.
 */
class DrawerScreenshotTest : ShadcnScreenshotTest() {
    private fun states(
        darkTheme: Boolean,
        direction: ShadcnDrawerDirection,
        suffix: String,
    ) {
        snapshot("drawer_$suffix", darkTheme = darkTheme) {
            ShadcnDrawer(visible = true, onDismissRequest = {}, direction = direction) {
                ShadcnDialogTitle("Edit profile")
                ShadcnDialogDescription("Make changes to your profile here.")
            }
        }
    }

    private fun editProfile(darkTheme: Boolean) {
        snapshot("drawer_edit_profile", darkTheme = darkTheme) {
            ShadcnDrawer(visible = true, onDismissRequest = {}) {
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

    @Test fun bottom_light() = states(darkTheme = false, direction = ShadcnDrawerDirection.Bottom, suffix = "bottom")

    @Test fun bottom_dark() = states(darkTheme = true, direction = ShadcnDrawerDirection.Bottom, suffix = "bottom")

    @Test fun top_light() = states(darkTheme = false, direction = ShadcnDrawerDirection.Top, suffix = "top")

    @Test fun end_light() = states(darkTheme = false, direction = ShadcnDrawerDirection.End, suffix = "end")

    @Test fun edit_profile_light() = editProfile(darkTheme = false)

    @Test fun edit_profile_dark() = editProfile(darkTheme = true)
}
