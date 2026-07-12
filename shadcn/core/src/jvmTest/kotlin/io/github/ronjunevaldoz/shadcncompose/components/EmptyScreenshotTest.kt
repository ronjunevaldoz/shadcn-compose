@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import kotlin.test.Test

class EmptyScreenshotTest : ShadcnScreenshotTest() {
    private fun states(darkTheme: Boolean) {
        snapshot("empty_states", darkTheme = darkTheme) {
            ShadcnEmpty {
                ShadcnEmptyHeader {
                    ShadcnEmptyMedia { ShadcnEmojiText("📭") }
                    ShadcnEmptyTitle("No results found")
                    ShadcnEmptyDescription("Try adjusting your search or filters.")
                }
                ShadcnEmptyContent {
                    ShadcnButton(onClick = {}, variant = ButtonVariant.Outline) { ShadcnText("Clear filters") }
                }
            }
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)
}
