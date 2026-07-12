@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBubble
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBubbleContent
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBubbleGroup
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBubbleReactions
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBubbleVariant
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnEmojiText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMessageAlign
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle

val bubbleDoc =
    ComponentDoc(
        id = "bubble",
        title = "Bubble",
        description = "A single chat message bubble, self-aligned to the left or right of the transcript.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.*

            ShadcnBubbleGroup {
                ShadcnBubble(align = ShadcnMessageAlign.End) {
                    ShadcnBubbleContent { ShadcnText("Hey, how's it going?") }
                }
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Variants",
                    code =
                        """
                        ShadcnBubbleGroup {
                            ShadcnBubble { ShadcnBubbleContent(variant = ShadcnBubbleVariant.Default) { ShadcnText("Default") } }
                            ShadcnBubble { ShadcnBubbleContent(variant = ShadcnBubbleVariant.Secondary) { ShadcnText("Secondary") } }
                            ShadcnBubble { ShadcnBubbleContent(variant = ShadcnBubbleVariant.Muted) { ShadcnText("Muted") } }
                            ShadcnBubble { ShadcnBubbleContent(variant = ShadcnBubbleVariant.Outline) { ShadcnText("Outline") } }
                            ShadcnBubble { ShadcnBubbleContent(variant = ShadcnBubbleVariant.Ghost) { ShadcnText("Ghost") } }
                            ShadcnBubble { ShadcnBubbleContent(variant = ShadcnBubbleVariant.Destructive) { ShadcnText("Destructive") } }
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnBubbleGroup {
                            ShadcnBubble {
                                ShadcnBubbleContent(
                                    variant = ShadcnBubbleVariant.Default,
                                ) { ShadcnText("Default") }
                            }
                            ShadcnBubble {
                                ShadcnBubbleContent(
                                    variant = ShadcnBubbleVariant.Secondary,
                                ) { ShadcnText("Secondary") }
                            }
                            ShadcnBubble {
                                ShadcnBubbleContent(
                                    variant = ShadcnBubbleVariant.Muted,
                                ) { ShadcnText("Muted") }
                            }
                            ShadcnBubble {
                                ShadcnBubbleContent(
                                    variant = ShadcnBubbleVariant.Outline,
                                ) { ShadcnText("Outline") }
                            }
                            ShadcnBubble {
                                ShadcnBubbleContent(
                                    variant = ShadcnBubbleVariant.Ghost,
                                ) { ShadcnText("Ghost") }
                            }
                            ShadcnBubble {
                                ShadcnBubbleContent(
                                    variant = ShadcnBubbleVariant.Destructive,
                                ) { ShadcnText("Destructive") }
                            }
                        }
                    },
                ),
                ComponentExample(
                    title = "Conversation alignment",
                    code =
                        """
                        ShadcnBubbleGroup {
                            ShadcnBubble(align = ShadcnMessageAlign.Start) {
                                ShadcnBubbleContent(variant = ShadcnBubbleVariant.Muted) { ShadcnText("Hi, how can I help?") }
                            }
                            ShadcnBubble(align = ShadcnMessageAlign.End) {
                                ShadcnBubbleContent(variant = ShadcnBubbleVariant.Default) { ShadcnText("Summarize this for me.") }
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnBubbleGroup {
                            ShadcnBubble(align = ShadcnMessageAlign.Start) {
                                ShadcnBubbleContent(
                                    variant = ShadcnBubbleVariant.Muted,
                                ) { ShadcnText("Hi, how can I help?") }
                            }
                            ShadcnBubble(align = ShadcnMessageAlign.End) {
                                ShadcnBubbleContent(variant = ShadcnBubbleVariant.Default) {
                                    ShadcnText("Summarize this for me.")
                                }
                            }
                        }
                    },
                ),
                ComponentExample(
                    title = "Conversation with reactions",
                    code =
                        """
                        ShadcnBubbleGroup {
                            ShadcnBubble {
                                ShadcnBubbleContent(variant = ShadcnBubbleVariant.Muted) {
                                    ShadcnText("Hey! Want to see chat bubbles?")
                                }
                            }
                            ShadcnBubble {
                                ShadcnBubbleContent(variant = ShadcnBubbleVariant.Muted) {
                                    ShadcnText("I can group messages, switch sides, and keep the whole thread easy to scan.")
                                }
                                ShadcnBubbleReactions { ShadcnEmojiText("👍", style = ShadcnTextStyle.LabelSmall) }
                            }
                            ShadcnBubble(align = ShadcnMessageAlign.End) {
                                ShadcnBubbleContent { ShadcnText("Sure. Hit me with your best demo.") }
                            }
                            ShadcnBubble {
                                ShadcnBubbleContent(variant = ShadcnBubbleVariant.Muted) {
                                    ShadcnText("Yes. You are reading a demo that is demoing itself. Very meta. Very on-brand.")
                                }
                                ShadcnBubbleReactions {
                                    ShadcnEmojiText("👍", style = ShadcnTextStyle.LabelSmall)
                                    ShadcnEmojiText("🔥", style = ShadcnTextStyle.LabelSmall)
                                    ShadcnText("+2", style = ShadcnTextStyle.LabelSmall, muted = true)
                                }
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnBubbleGroup {
                            ShadcnBubble {
                                ShadcnBubbleContent(variant = ShadcnBubbleVariant.Muted) {
                                    ShadcnText("Hey! Want to see chat bubbles?")
                                }
                            }
                            ShadcnBubble {
                                ShadcnBubbleContent(variant = ShadcnBubbleVariant.Muted) {
                                    ShadcnText(
                                        "I can group messages, switch sides, and keep the whole thread easy to scan.",
                                    )
                                }
                                ShadcnBubbleReactions { ShadcnEmojiText("👍", style = ShadcnTextStyle.LabelSmall) }
                            }
                            ShadcnBubble(align = ShadcnMessageAlign.End) {
                                ShadcnBubbleContent { ShadcnText("Sure. Hit me with your best demo.") }
                            }
                            ShadcnBubble {
                                ShadcnBubbleContent(variant = ShadcnBubbleVariant.Muted) {
                                    ShadcnText(
                                        "Yes. You are reading a demo that is demoing itself. Very meta. Very on-brand.",
                                    )
                                }
                                ShadcnBubbleReactions {
                                    ShadcnEmojiText("👍", style = ShadcnTextStyle.LabelSmall)
                                    ShadcnEmojiText("🔥", style = ShadcnTextStyle.LabelSmall)
                                    ShadcnText("+2", style = ShadcnTextStyle.LabelSmall, muted = true)
                                }
                            }
                        }
                    },
                ),
            ),
    )
