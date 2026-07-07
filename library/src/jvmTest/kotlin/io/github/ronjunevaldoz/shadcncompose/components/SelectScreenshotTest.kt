@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnStylePreset
import kotlin.test.Test

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

    @Test fun closed_light() = closed(darkTheme = false)

    @Test fun closed_dark() = closed(darkTheme = true)
}
