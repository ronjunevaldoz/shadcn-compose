package io.github.ronjunevaldoz.shadcncompose.components

import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

/** The idle (no panel open) state -- open-panel appearance reuses ShadcnAnchoredPopup, already proven by Popover. */
class NavigationMenuScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("navigation_menu_idle", darkTheme = darkTheme) {
            ShadcnNavigationMenu(
                items =
                    listOf(
                        ShadcnNavigationMenuItem("Home", onClick = {}),
                        ShadcnNavigationMenuItem(
                            "Getting started",
                            panel = { ShadcnText("Re-usable components built with Radix UI.") },
                        ),
                        ShadcnNavigationMenuItem("Docs", onClick = {}),
                    ),
            )
        }
    }

    @Test fun idle_light() = states(darkTheme = false)

    @Test fun idle_dark() = states(darkTheme = true)
}
