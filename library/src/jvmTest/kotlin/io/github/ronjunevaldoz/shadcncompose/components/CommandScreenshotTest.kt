@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class CommandScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("command_states", darkTheme = darkTheme) {
            ShadcnCommand(
                items =
                    listOf(
                        ShadcnCommandItem("calendar", "Calendar", onSelect = {}),
                        ShadcnCommandItem("emoji", "Search Emoji", onSelect = {}),
                        ShadcnCommandItem("calculator", "Calculator", onSelect = {}),
                    ),
            )
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)
}
