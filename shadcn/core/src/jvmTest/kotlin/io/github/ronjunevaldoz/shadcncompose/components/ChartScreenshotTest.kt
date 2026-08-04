package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.center
import androidx.compose.ui.test.down
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class ChartScreenshotTest : ShadcnScreenshotTest() {
    private val config =
        mapOf(
            "desktop" to ShadcnChartSeries("Desktop", Color(0xFF2563EB)),
            "mobile" to ShadcnChartSeries("Mobile", Color(0xFF60A5FA)),
        )
    private val data =
        listOf(
            ShadcnChartPoint("Jan", mapOf("desktop" to 186f, "mobile" to 80f)),
            ShadcnChartPoint("Feb", mapOf("desktop" to 305f, "mobile" to 200f)),
            ShadcnChartPoint("Mar", mapOf("desktop" to 237f, "mobile" to 120f)),
        )

    private fun barStates(darkTheme: Boolean) {
        snapshot("chart_bar_states", darkTheme = darkTheme) {
            Column {
                ShadcnChartContainer { ShadcnBarChart(data = data, config = config) }
                ShadcnChartLegend(config = config)
            }
        }
    }

    private fun lineStates(darkTheme: Boolean) {
        snapshot("chart_line_states", darkTheme = darkTheme) {
            Column {
                ShadcnChartContainer { ShadcnLineChart(data = data, config = config) }
                ShadcnChartLegend(config = config)
            }
        }
    }

    @Test fun bar_light() = barStates(darkTheme = false)

    @Test fun bar_dark() = barStates(darkTheme = true)

    @Test fun line_light() = lineStates(darkTheme = false)

    @Test fun line_dark() = lineStates(darkTheme = true)

    /**
     * Simulates a touch press to surface the hover/tap tooltip, via `performTouchInput
     * { down(center) }` -- not `performMouseInput { moveTo(...) }`, which hung the JVM
     * test worker indefinitely for [TooltipScreenshotTest] (see its doc comment).
     * `down()` is the same proven-safe pattern [ButtonScreenshotTest] already uses for
     * its pressed state, so it's used here instead of a static-only capture.
     */
    @Test
    fun bar_tooltip_light() {
        setThemedContent(darkTheme = false) {
            Column {
                ShadcnChartContainer {
                    ShadcnBarChart(data = data, config = config, modifier = Modifier.testTag("chart"))
                }
                ShadcnChartLegend(config = config)
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("chart").performTouchInput { down(center) }
        composeRule.waitForIdle()
        captureNamed("chart_bar_tooltip", darkTheme = false)
    }
}
