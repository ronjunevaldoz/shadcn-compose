package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class ScrollAreaScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("scroll_area_states", darkTheme = darkTheme) {
            ShadcnScrollArea(modifier = Modifier.height(120.dp)) {
                Column {
                    repeat(10) { index -> ShadcnText("Row $index") }
                }
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)
}
