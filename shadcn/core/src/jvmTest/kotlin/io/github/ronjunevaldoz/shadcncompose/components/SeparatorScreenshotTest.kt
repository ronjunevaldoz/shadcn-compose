package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class SeparatorScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("separator_states", darkTheme = darkTheme) {
            // Arrangement.spacedBy, not a bare Row: ShadcnSeparator has no built-in
            // margin (matching real shadcn/ui), so without spacing the surrounding
            // text renders flush against the separator line -- see SeparatorDoc.kt's
            // "with spacing" examples for the same fix applied to the catalog demo.
            Row(modifier = Modifier.height(20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ShadcnText("Blog")
                ShadcnSeparator(orientation = ShadcnSeparatorOrientation.Vertical)
                ShadcnText("Docs")
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)
}
