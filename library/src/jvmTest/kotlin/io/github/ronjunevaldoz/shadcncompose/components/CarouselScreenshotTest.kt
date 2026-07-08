@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class CarouselScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("carousel_states", darkTheme = darkTheme) {
            val state = rememberPagerState { 3 }
            Column {
                ShadcnCarousel(state = state, modifier = Modifier.height(100.dp)) { page ->
                    Box(Modifier, contentAlignment = Alignment.Center) { ShadcnText("Slide ${page + 1}") }
                }
                ShadcnCarouselDots(state = state)
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)
}
