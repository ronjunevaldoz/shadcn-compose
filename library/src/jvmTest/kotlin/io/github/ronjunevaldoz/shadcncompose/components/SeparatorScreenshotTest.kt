package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class SeparatorScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("separator_states", darkTheme = darkTheme) {
            Row(modifier = Modifier.height(24.dp)) {
                ShadcnText("Blog")
                ShadcnSeparator(orientation = ShadcnSeparatorOrientation.Vertical)
                ShadcnText("Docs")
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)
}
