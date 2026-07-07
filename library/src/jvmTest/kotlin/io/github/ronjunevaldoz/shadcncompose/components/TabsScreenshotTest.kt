package io.github.ronjunevaldoz.shadcncompose.components

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
}
