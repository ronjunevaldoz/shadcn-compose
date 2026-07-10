@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

/** Captures the trigger only -- see [TooltipScreenshotTest]'s doc comment for why hover simulation isn't used. */
class HoverCardScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("hover_card_trigger", darkTheme = darkTheme) {
            ShadcnHoverCard(trigger = { ShadcnText("@shadcn") }) {
                ShadcnText("The React Framework – created and maintained by @vercel.")
            }
        }
    }

    @Test fun trigger_light() = states(darkTheme = false)

    @Test fun trigger_dark() = states(darkTheme = true)
}
