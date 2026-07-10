@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
            // Fixed width: HorizontalPager needs a bounded main-axis size for its own page
            // width -- without one it silently expands to whatever ambient width it's given.
            Column(modifier = Modifier.width(200.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                ShadcnCarousel(state = state, modifier = Modifier.fillMaxWidth().height(100.dp)) { page ->
                    Box(Modifier, contentAlignment = Alignment.Center) { ShadcnText("Slide ${page + 1}") }
                }
                // Previous/Next must render circular (real shadcn: rounded-full) -- covers
                // the shape fix, never previously exercised by this golden.
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShadcnCarouselPrevious(state = state)
                    ShadcnCarouselNext(state = state)
                }
                ShadcnCarouselDots(state = state)
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)
}
