@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBubble
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBubbleContent
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBubbleGroup
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBubbleVariant
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMessageAlign
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText

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
            ),
    )
