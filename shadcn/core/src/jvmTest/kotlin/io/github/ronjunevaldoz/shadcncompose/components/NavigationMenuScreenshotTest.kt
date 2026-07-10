package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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

    private fun itemFocused(darkTheme: Boolean) {
        snapshotFocused("navigation_menu_item_focused", focusTag = "navmenu", darkTheme = darkTheme) {
            ShadcnNavigationMenu(
                modifier = Modifier.testTag("navmenu"),
                items = listOf(ShadcnNavigationMenuItem("Home", onClick = {})),
            )
        }
    }

    @Test fun item_focused_light() = itemFocused(darkTheme = false)
}
