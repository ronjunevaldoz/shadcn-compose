@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBubble
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBubbleContent
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBubbleReactions
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBubbleVariant
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnEmojiText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMessage
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMessageAlign
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMessageAvatar
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMessageFooter
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMessageGroup
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMessageHeader
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle

val messageDoc =
    ComponentDoc(
        id = "message",
        title = "Message",
        description =
            "One chat-transcript row: avatar and content laid out side by side, mirrored for the sender's own " +
                "messages.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.*

            ShadcnMessage(
                align = ShadcnMessageAlign.Start,
                avatar = { ShadcnMessageAvatar { ShadcnText("AI") } },
            ) {
                ShadcnText("Hello! How can I help you today?")
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Start aligned",
                    code =
                        """
                        ShadcnMessage(
                            avatar = { ShadcnMessageAvatar { ShadcnText("AI") } },
                        ) {
                            ShadcnMessageHeader { ShadcnText("Assistant", style = ShadcnTextStyle.LabelLarge) }
                            ShadcnText("Hello! How can I help you today?")
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnMessage(
                            avatar = { ShadcnMessageAvatar { ShadcnText("AI") } },
                        ) {
                            ShadcnMessageHeader { ShadcnText("Assistant", style = ShadcnTextStyle.LabelLarge) }
                            ShadcnText("Hello! How can I help you today?")
                        }
                    },
                ),
                ComponentExample(
                    title = "End aligned",
                    code =
                        """
                        ShadcnMessage(
                            align = ShadcnMessageAlign.End,
                            avatar = { ShadcnMessageAvatar { ShadcnText("Me") } },
                        ) {
                            ShadcnText("Can you summarize this document?")
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnMessage(
                            align = ShadcnMessageAlign.End,
                            avatar = { ShadcnMessageAvatar { ShadcnText("Me") } },
                        ) {
                            ShadcnText("Can you summarize this document?")
                        }
                    },
                ),
                ComponentExample(
                    title = "Conversation with bubbles",
                    code =
                        """
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
                        """.trimIndent(),
                    preview = {
                        Column(
                            modifier = Modifier.width(280.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
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
                                        ShadcnBubbleReactions {
                                            ShadcnEmojiText("👍", style = ShadcnTextStyle.LabelSmall)
                                        }
                                    }
                                }
                            }
                            ShadcnText("Oliver is typing…", style = ShadcnTextStyle.LabelSmall, muted = true)
                        }
                    },
                ),
            ),
    )
