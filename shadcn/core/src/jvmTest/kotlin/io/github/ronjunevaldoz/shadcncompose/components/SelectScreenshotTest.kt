@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnStylePreset
import kotlin.test.Test

/** The idle (closed) trigger only -- the open panel reuses the same row rendering already proven by Combobox/DropdownMenu. */
class SelectScreenshotTest : ShadcnScreenshotTest() {
    private fun closed(darkTheme: Boolean) {
        snapshot("select_closed", darkTheme = darkTheme) {
            ShadcnSelect(
                value = ShadcnStylePreset.Vega,
                options = ShadcnStylePreset.entries,
                onValueChange = {},
                label = { it.label },
            )
        }
    }

    private fun placeholder(darkTheme: Boolean) {
        snapshot("select_placeholder", darkTheme = darkTheme) {
            ShadcnSelect<String>(
                value = null,
                options = listOf("Light", "Dark", "System"),
                onValueChange = {},
                placeholder = "Theme",
            )
        }
    }

    @Test fun closed_light() = closed(darkTheme = false)

    @Test fun closed_dark() = closed(darkTheme = true)

    @Test fun placeholder_light() = placeholder(darkTheme = false)

    @Test fun placeholder_dark() = placeholder(darkTheme = true)
}
