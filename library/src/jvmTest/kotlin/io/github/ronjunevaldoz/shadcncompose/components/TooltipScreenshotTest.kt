@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

/**
 * Captures the trigger only (not the hover-revealed tooltip bubble) -- a
 * `performMouseInput { moveTo(...) }` hover simulation was tried here and hung the
 * JVM test worker indefinitely (confirmed via `ps`: an actively CPU-spinning "Gradle
 * Test Executor" that had to be `kill -9`'d), unlike every other interaction used
 * elsewhere in this suite (`requestFocus()`, plain clicks). Documented gap, not
 * silently skipped.
 */
class TooltipScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("tooltip_trigger", darkTheme = darkTheme) {
            ShadcnTooltip(text = "Add to library") {
                ShadcnButton(onClick = {}) { ShadcnText("Hover me") }
            }
        }
    }

    @Test fun trigger_light() = states(darkTheme = false)

    @Test fun trigger_dark() = states(darkTheme = true)
}
