@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

/** The idle (closed) trigger only -- the open list reuses the same row rendering already proven by DropdownMenu/Command. */
class ComboboxScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("combobox_idle", darkTheme = darkTheme) {
            ShadcnCombobox(
                value = null,
                options = listOf("Next.js", "SvelteKit", "Nuxt.js"),
                onValueChange = {},
            )
        }
    }

    @Test fun idle_light() = states(darkTheme = false)

    @Test fun idle_dark() = states(darkTheme = true)
}
