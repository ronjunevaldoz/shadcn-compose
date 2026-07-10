package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class TextScreenshotTest : ShadcnScreenshotTest() {
    private fun allStyles(darkTheme: Boolean) {
        snapshot("text_styles", darkTheme = darkTheme) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ShadcnTextStyle.entries.forEach { style ->
                    ShadcnText(text = style.name, style = style)
                }
                ShadcnText(text = "Muted body text", muted = true)
            }
        }
    }

    @Test fun styles_light() = allStyles(darkTheme = false)

    @Test fun styles_dark() = allStyles(darkTheme = true)
}
