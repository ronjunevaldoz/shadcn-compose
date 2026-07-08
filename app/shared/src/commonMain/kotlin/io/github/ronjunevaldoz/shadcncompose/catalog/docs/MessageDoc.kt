package io.github.ronjunevaldoz.shadcncompose.catalog.docs

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
                    title = "Group with footer timestamp",
                    code =
                        """
                        ShadcnMessageGroup {
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
                        """.trimIndent(),
                    preview = {
                        ShadcnMessageGroup {
                            ShadcnMessage(avatar = { ShadcnMessageAvatar { ShadcnText("AI") } }) {
                                ShadcnText("Sure, one moment.")
                            }
                            ShadcnMessage(
                                align = ShadcnMessageAlign.End,
                                avatar = { ShadcnMessageAvatar { ShadcnText("Me") } },
                            ) {
                                ShadcnText("Thanks!")
                                ShadcnMessageFooter {
                                    ShadcnText("2:04 PM", style = ShadcnTextStyle.LabelSmall, muted = true)
                                }
                            }
                        }
                    },
                ),
            ),
    )
