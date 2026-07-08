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

    private fun conversationWithReactions(darkTheme: Boolean) {
        snapshot("bubble_conversation_with_reactions", darkTheme = darkTheme) {
            ShadcnBubbleGroup(modifier = Modifier.width(280.dp)) {
                ShadcnBubble {
                    ShadcnBubbleContent(variant = ShadcnBubbleVariant.Muted) {
                        ShadcnText("Hey! Want to see chat bubbles?")
                    }
                }
                ShadcnBubble {
                    ShadcnBubbleContent(variant = ShadcnBubbleVariant.Muted) {
                        ShadcnText("I can group messages, switch sides, and keep the whole thread easy to scan.")
                    }
                    ShadcnBubbleReactions { ShadcnText("👍", style = ShadcnTextStyle.LabelSmall) }
                }
                ShadcnBubble(align = ShadcnMessageAlign.End) {
                    ShadcnBubbleContent { ShadcnText("Sure. Hit me with your best demo.") }
                }
                ShadcnBubble {
                    ShadcnBubbleContent(variant = ShadcnBubbleVariant.Muted) {
                        ShadcnText("Yes. You are reading a demo that is demoing itself. Very meta. Very on-brand.")
                    }
                    ShadcnBubbleReactions {
                        ShadcnText("👍", style = ShadcnTextStyle.LabelSmall)
                        ShadcnText("🔥", style = ShadcnTextStyle.LabelSmall)
                        ShadcnText("👀", style = ShadcnTextStyle.LabelSmall)
                        ShadcnText("+2", style = ShadcnTextStyle.LabelSmall, muted = true)
                    }
                }
            }
        }
    }

    @Test fun variants_light() = variants(darkTheme = false)

    @Test fun variants_dark() = variants(darkTheme = true)

    @Test fun alignment_light() = alignment(darkTheme = false)

    @Test fun alignment_dark() = alignment(darkTheme = true)

    @Test fun conversation_with_reactions_light() = conversationWithReactions(darkTheme = false)

    @Test fun conversation_with_reactions_dark() = conversationWithReactions(darkTheme = true)
}
