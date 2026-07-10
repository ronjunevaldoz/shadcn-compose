@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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

    /**
     * Regression guard for a real bug: [ShadcnCarouselPrevious]/[ShadcnCarouselNext] used
     * to size themselves via two competing mechanisms -- an outer `Modifier.size(32.dp)`
     * on the button plus `ButtonSize.Icon`'s own Style-level `width(36.dp)/height(36.dp)`
     * -- and the Style-level size won, rendering the button at 36dp despite the (ignored)
     * 32dp modifier. Positioned the way the real doc example does (`.offset(x = -40.dp)`
     * right against a 200dp carousel's edge, matching CarouselDoc.kt exactly), that 4dp
     * overflow bled the Previous button into the carousel card underneath it.
     */
    private fun overlapRegression(darkTheme: Boolean) {
        snapshot("carousel_overlap_regression", darkTheme = darkTheme) {
            val state = rememberPagerState { 3 }
            Box(modifier = Modifier.width(200.dp).height(200.dp), contentAlignment = Alignment.Center) {
                ShadcnCarousel(state = state, modifier = Modifier.fillMaxSize()) { page ->
                    ShadcnCard(modifier = Modifier.fillMaxSize()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            ShadcnText("${page + 1}")
                        }
                    }
                }
                ShadcnCarouselPrevious(
                    state = state,
                    modifier = Modifier.align(Alignment.CenterStart).offset(x = (-40).dp),
                )
                ShadcnCarouselNext(
                    state = state,
                    modifier = Modifier.align(Alignment.CenterEnd).offset(x = 40.dp),
                )
            }
        }
    }

    @Test fun overlap_regression_light() = overlapRegression(darkTheme = false)

    /**
     * Isolated size check, no neighbors (no dots, no Next button, no card) to avoid any
     * ambiguity about which circle in the golden is which -- proves
     * [ShadcnCarouselPrevious] renders at its intended 32dp, not the pre-fix 36dp.
     */
    @Test
    fun previous_isolated_size_light() {
        snapshot("carousel_previous_isolated_size", darkTheme = false) {
            val state = rememberPagerState { 3 }
            ShadcnCarouselPrevious(state = state)
        }
    }
}
