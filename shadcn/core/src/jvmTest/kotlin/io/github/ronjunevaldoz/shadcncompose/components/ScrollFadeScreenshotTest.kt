package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.styles.shadcnScrollFade
import kotlin.test.Test

class ScrollFadeScreenshotTest : ShadcnScreenshotTest() {
    /** Scrolled to the middle so both edges fade at once -- at rest (scrollValue = 0) the
     * leading edge never fades, so a resting capture would only ever show half the effect. */
    private fun states(darkTheme: Boolean) {
        snapshot("scroll_fade_states", darkTheme = darkTheme) {
            val scrollState = rememberScrollState()
            LaunchedEffect(Unit) { scrollState.scrollTo(scrollState.maxValue / 2) }
            Column(
                modifier =
                    Modifier
                        .height(120.dp)
                        .verticalScroll(scrollState)
                        .shadcnScrollFade(scrollState),
            ) {
                repeat(12) { index -> ShadcnText("Row $index") }
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)
}
