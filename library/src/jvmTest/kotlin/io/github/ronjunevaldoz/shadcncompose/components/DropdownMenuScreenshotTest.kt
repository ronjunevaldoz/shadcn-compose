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
                ShadcnDropdownMenu(
                    expanded = true,
                    onDismissRequest = {},
                    items =
                        listOf(
                            ShadcnDropdownMenuItem("Edit", onClick = {}),
                            ShadcnDropdownMenuItem("Duplicate", onClick = {}),
                            ShadcnDropdownMenuItem("Delete", onClick = {}, destructive = true),
                        ),
                )
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)
}
