package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class TabsScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("tabs_states", darkTheme = darkTheme) {
            ShadcnTabsList(
                items = listOf(ShadcnTabItem("account", "Account"), ShadcnTabItem("password", "Password")),
                selected = "account",
                onSelectedChange = {},
            )
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)

    private fun itemFocused(darkTheme: Boolean) {
        snapshotFocused("tabs_item_focused", focusTag = "tabs", darkTheme = darkTheme) {
            ShadcnTabsList(
                modifier = Modifier.testTag("tabs"),
                items = listOf(ShadcnTabItem("account", "Account")),
                selected = "account",
                onSelectedChange = {},
            )
        }
    }

    @Test fun item_focused_light() = itemFocused(darkTheme = false)
}
