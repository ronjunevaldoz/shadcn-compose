@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class MessageScreenshotTest : ShadcnScreenshotTest() {
    private fun startAligned(darkTheme: Boolean) {
        snapshot("message_start_aligned", darkTheme = darkTheme) {
            ShadcnMessage(
                modifier = Modifier.width(280.dp),
                align = ShadcnMessageAlign.Start,
                avatar = { ShadcnMessageAvatar { ShadcnText("AI") } },
            ) {
                ShadcnMessageHeader { ShadcnText("Assistant", style = ShadcnTextStyle.LabelLarge) }
                ShadcnText("Hello! How can I help you today?")
            }
        }
    }

    private fun endAligned(darkTheme: Boolean) {
        snapshot("message_end_aligned", darkTheme = darkTheme) {
            ShadcnMessage(
                modifier = Modifier.width(280.dp),
                align = ShadcnMessageAlign.End,
                avatar = { ShadcnMessageAvatar { ShadcnText("Me") } },
            ) {
                ShadcnText("Can you summarize this document?")
            }
        }
    }

    private fun group(darkTheme: Boolean) {
        snapshot("message_group", darkTheme = darkTheme) {
            ShadcnMessageGroup(modifier = Modifier.width(280.dp)) {
                ShadcnMessage(avatar = { ShadcnMessageAvatar { ShadcnText("AI") } }) {
                    ShadcnText("Sure, one moment.")
                }
                ShadcnMessage(
                    align = ShadcnMessageAlign.End,
                    avatar = { ShadcnMessageAvatar { ShadcnText("Me") } },
                ) {
                    ShadcnText("Thanks!")
                    ShadcnMessageFooter { ShadcnText("2:04 PM", style = ShadcnTextStyle.LabelSmall, muted = true) }
                }
            }
        }
    }

    private fun conversationWithBubbles(darkTheme: Boolean) {
        snapshot("message_conversation_with_bubbles", darkTheme = darkTheme) {
            Column(modifier = Modifier.width(280.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ShadcnMessageGroup {
                    ShadcnMessage(
                        align = ShadcnMessageAlign.End,
                        avatar = { ShadcnMessageAvatar { ShadcnEmojiText("🙂") } },
                    ) {
                        ShadcnBubble(align = ShadcnMessageAlign.End) {
                            ShadcnBubbleContent { ShadcnText("Deploying to prod real quick.") }
                        }
                    }
                    ShadcnMessage(avatar = { ShadcnMessageAvatar { ShadcnEmojiText("👨") } }) {
                        ShadcnBubble {
                            ShadcnBubbleContent(variant = ShadcnBubbleVariant.Muted) {
                                ShadcnText("It's 4:55 PM. On a Friday.")
                            }
                        }
                    }
                    ShadcnMessage(
                        align = ShadcnMessageAlign.End,
                        avatar = { ShadcnMessageAvatar { ShadcnEmojiText("🙂") } },
                    ) {
                        ShadcnBubble(align = ShadcnMessageAlign.End) {
                            ShadcnBubbleContent { ShadcnText("It's a one-line change.") }
                        }
                        ShadcnMessageFooter {
                            ShadcnText("Delivered", style = ShadcnTextStyle.LabelSmall, muted = true)
                        }
                    }
                    ShadcnMessage(avatar = { ShadcnMessageAvatar { ShadcnEmojiText("👨") } }) {
                        ShadcnBubble {
                            ShadcnBubbleContent(variant = ShadcnBubbleVariant.Muted) {
                                ShadcnEmojiText("It's always a one-line change 😭.")
                            }
                        }
                    }
                    ShadcnMessage(avatar = { ShadcnMessageAvatar { ShadcnEmojiText("👨") } }) {
                        ShadcnBubble {
                            ShadcnBubbleContent(variant = ShadcnBubbleVariant.Muted) {
                                ShadcnText("Alright, let me take a look.")
                            }
                            ShadcnBubbleReactions { ShadcnEmojiText("👍", style = ShadcnTextStyle.LabelSmall) }
                        }
                    }
                }
                ShadcnText("Oliver is typing…", style = ShadcnTextStyle.LabelSmall, muted = true)
            }
        }
    }

    @Test fun start_aligned_light() = startAligned(darkTheme = false)

    @Test fun start_aligned_dark() = startAligned(darkTheme = true)

    @Test fun end_aligned_light() = endAligned(darkTheme = false)

    @Test fun end_aligned_dark() = endAligned(darkTheme = true)

    @Test fun group_light() = group(darkTheme = false)

    @Test fun conversation_with_bubbles_light() = conversationWithBubbles(darkTheme = false)

    @Test fun conversation_with_bubbles_dark() = conversationWithBubbles(darkTheme = true)
}
