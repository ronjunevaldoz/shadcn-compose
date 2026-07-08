package io.github.ronjunevaldoz.shadcncompose.components

import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

/** The idle (no menu open) state -- open-panel appearance reuses DropdownMenu's already-proven rendering. */
class MenubarScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("menubar_idle", darkTheme = darkTheme) {
            ShadcnMenubar(
                menus =
                    listOf(
                        ShadcnMenubarMenu("File", listOf(ShadcnDropdownMenuItem("New Tab", onClick = {}))),
                        ShadcnMenubarMenu("Edit", listOf(ShadcnDropdownMenuItem("Undo", onClick = {}))),
                        ShadcnMenubarMenu("View", listOf(ShadcnDropdownMenuItem("Zoom In", onClick = {}))),
                    ),
            )
        }
    }

    @Test fun idle_light() = states(darkTheme = false)

    @Test fun idle_dark() = states(darkTheme = true)
}
