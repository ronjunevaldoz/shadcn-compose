package io.github.ronjunevaldoz.shadcncompose.components

import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class ToggleGroupScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("toggle_group_states", darkTheme = darkTheme) {
            ShadcnToggleGroup(
                items =
                    listOf(
                        ToggleGroupItem("bold", "B"),
                        ToggleGroupItem("italic", "I"),
                        ToggleGroupItem("underline", "U"),
                    ),
                selected = setOf("bold"),
                onSelectedChange = {},
            )
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)
}
