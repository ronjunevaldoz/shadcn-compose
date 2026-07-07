package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import kotlin.test.Test

class AspectRatioScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("aspect_ratio_states", darkTheme = darkTheme) {
            ShadcnAspectRatio(ratio = 16f / 9f, modifier = Modifier.width(240.dp)) {
                Box(
                    modifier = Modifier.fillMaxSize().background(shadcnTheme.colors.muted),
                    contentAlignment = Alignment.Center,
                ) {
                    ShadcnText("16:9", style = ShadcnTextStyle.LabelSmall, muted = true)
                }
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)
}
