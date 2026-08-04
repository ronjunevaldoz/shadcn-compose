@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Box
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class DropdownMenuScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("dropdown_menu_states", darkTheme = darkTheme) {
            Box {
                ShadcnButton(onClick = {}) { ShadcnText("Open") }
                ShadcnDropdownMenu(expanded = true, onDismissRequest = {}) {
                    ShadcnDropdownMenuLabel("My Account")
                    ShadcnDropdownMenuSeparator()
                    ShadcnDropdownMenuItem("Edit", onClick = {})
                    ShadcnDropdownMenuItem("Duplicate", onClick = {})
                    ShadcnDropdownMenuSeparator()
                    ShadcnDropdownMenuItem("Delete", onClick = {}, destructive = true)
                }
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)

    private fun checkboxAndRadio(darkTheme: Boolean) {
        snapshot("dropdown_menu_checkbox_and_radio", darkTheme = darkTheme) {
            Box {
                ShadcnButton(onClick = {}) { ShadcnText("Open") }
                ShadcnDropdownMenu(expanded = true, onDismissRequest = {}) {
                    ShadcnDropdownMenuLabel("Appearance", inset = true)
                    ShadcnDropdownMenuCheckboxItem("Status Bar", checked = true, onCheckedChange = {})
                    ShadcnDropdownMenuCheckboxItem(
                        "Activity Bar",
                        checked = false,
                        onCheckedChange = {},
                        shortcut = "⌘B",
                    )
                    ShadcnDropdownMenuSeparator()
                    ShadcnDropdownMenuRadioGroup(value = "bottom", onValueChange = {}) {
                        ShadcnDropdownMenuRadioItem("Top", value = "top")
                        ShadcnDropdownMenuRadioItem("Bottom", value = "bottom")
                        ShadcnDropdownMenuRadioItem("Right", value = "right", enabled = false)
                    }
                }
            }
        }
    }

    @Test fun checkbox_and_radio_light() = checkboxAndRadio(darkTheme = false)

    @Test fun checkbox_and_radio_dark() = checkboxAndRadio(darkTheme = true)
}
