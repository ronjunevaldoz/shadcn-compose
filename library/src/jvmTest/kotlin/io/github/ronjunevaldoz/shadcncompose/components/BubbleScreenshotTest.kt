@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class BubbleScreenshotTest : ShadcnScreenshotTest() {
    private fun variants(darkTheme: Boolean) {
        snapshot("bubble_variants", darkTheme = darkTheme) {
            ShadcnBubbleGroup(modifier = Modifier.width(280.dp)) {
                ShadcnBubble { ShadcnBubbleContent(variant = ShadcnBubbleVariant.Default) { ShadcnText("Default") } }
                ShadcnBubble {
                    ShadcnBubbleContent(
                        variant = ShadcnBubbleVariant.Secondary,
                    ) { ShadcnText("Secondary") }
                }
                ShadcnBubble { ShadcnBubbleContent(variant = ShadcnBubbleVariant.Muted) { ShadcnText("Muted") } }
                ShadcnBubble { ShadcnBubbleContent(variant = ShadcnBubbleVariant.Outline) { ShadcnText("Outline") } }
                ShadcnBubble { ShadcnBubbleContent(variant = ShadcnBubbleVariant.Ghost) { ShadcnText("Ghost") } }
                ShadcnBubble {
                    ShadcnBubbleContent(
                        variant = ShadcnBubbleVariant.Destructive,
                    ) { ShadcnText("Destructive") }
                }
            }
        }
    }

    private fun alignment(darkTheme: Boolean) {
        snapshot("bubble_alignment", darkTheme = darkTheme) {
            ShadcnBubbleGroup(modifier = Modifier.width(280.dp)) {
                ShadcnBubble(align = ShadcnMessageAlign.Start) {
                    ShadcnBubbleContent(variant = ShadcnBubbleVariant.Muted) { ShadcnText("Hi, how can I help?") }
                }
                ShadcnBubble(align = ShadcnMessageAlign.End) {
                    ShadcnBubbleContent(variant = ShadcnBubbleVariant.Default) { ShadcnText("Summarize this for me.") }
                }
            }
        }
    }

    @Test fun variants_light() = variants(darkTheme = false)

    @Test fun variants_dark() = variants(darkTheme = true)

    @Test fun alignment_light() = alignment(darkTheme = false)

    @Test fun alignment_dark() = alignment(darkTheme = true)
}
