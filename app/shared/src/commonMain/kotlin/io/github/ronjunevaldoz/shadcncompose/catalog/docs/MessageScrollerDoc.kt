@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBubble
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBubbleContent
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBubbleVariant
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCard
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCardHeader
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMessage
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMessageAlign
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMessageAvatar
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMessageScroller
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonSize
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

val messageScrollerDoc =
    ComponentDoc(
        id = "message-scroller",
        title = "Message Scroller",
        description =
            "A chat-transcript scroll container that follows new messages to the bottom -- but only while " +
                "the reader hasn't scrolled away -- with a floating jump-to-bottom button when they have.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.*

            ShadcnMessageScroller(modifier = Modifier.fillMaxSize()) {
                messages.forEach { message ->
                    ShadcnMessage(avatar = { ShadcnMessageAvatar { ShadcnText("AI") } }) {
                        ShadcnText(message.text)
                    }
                }
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        // modifier must constrain the main-axis size (here: a fixed height) --
                        // this composable fills whatever it's given.
                        ShadcnMessageScroller(modifier = Modifier.width(280.dp).height(200.dp)) {
                            repeat(10) { index ->
                                ShadcnMessage(avatar = { ShadcnMessageAvatar { ShadcnText("AI") } }) {
                                    ShadcnText("Message number ${'$'}index")
                                }
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnMessageScroller(modifier = Modifier.width(280.dp).height(200.dp)) {
                            repeat(10) { index ->
                                ShadcnMessage(avatar = { ShadcnMessageAvatar { ShadcnText("AI") } }) {
                                    ShadcnText("Message number $index")
                                }
                            }
                        }
                    },
                ),
                ComponentExample(
                    title = "Chat panel",
                    code =
                        """
                        ShadcnCard(
                            modifier = Modifier.width(320.dp).height(420.dp),
                            header = {
                                ShadcnCardHeader(
                                    title = "New Chat",
                                    description = "How can I help you today?",
                                    action = {
                                        ShadcnButton(onClick = {}, variant = ButtonVariant.Outline, size = ButtonSize.Icon) {
                                            ShadcnText("↻")
                                        }
                                    },
                                )
                            },
                            footer = { ChatComposer() },
                        ) {
                            ShadcnMessageScroller(modifier = Modifier.weight(1f)) {
                                ShadcnMessage(avatar = { ShadcnMessageAvatar { ShadcnText("AI") } }) {
                                    ShadcnBubble {
                                        ShadcnBubbleContent(variant = ShadcnBubbleVariant.Muted) {
                                            ShadcnText("Hi! Ask me anything about MessageScroller.")
                                        }
                                    }
                                }
                                ShadcnMessage(
                                    align = ShadcnMessageAlign.End,
                                    avatar = { ShadcnMessageAvatar { ShadcnText("Me") } },
                                ) {
                                    ShadcnBubble(align = ShadcnMessageAlign.End) {
                                        ShadcnBubbleContent { ShadcnText("Does it auto-scroll while I'm reading old messages?") }
                                    }
                                }
                                ShadcnMessage(avatar = { ShadcnMessageAvatar { ShadcnText("AI") } }) {
                                    ShadcnBubble {
                                        ShadcnBubbleContent(variant = ShadcnBubbleVariant.Muted) {
                                            ShadcnText(
                                                "Nope -- only when you're already near the bottom. " +
                                                    "Scroll up and it leaves you alone.",
                                            )
                                        }
                                    }
                                }
                                ShadcnMessage(
                                    align = ShadcnMessageAlign.End,
                                    avatar = { ShadcnMessageAvatar { ShadcnText("Me") } },
                                ) {
                                    ShadcnBubble(align = ShadcnMessageAlign.End) {
                                        ShadcnBubbleContent { ShadcnText("And this message you're reading right now?") }
                                    }
                                }
                                ShadcnMessage(avatar = { ShadcnMessageAvatar { ShadcnText("AI") } }) {
                                    ShadcnBubble {
                                        ShadcnBubbleContent(variant = ShadcnBubbleVariant.Muted) {
                                            ShadcnText("Proof it works -- you're at the bottom, so it followed me here.")
                                        }
                                    }
                                }
                            }
                        }

                        @Composable
                        private fun ChatComposer() {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(shadcnTheme.colors.muted, RoundedCornerShape(shadcnTheme.shapes.full))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                ShadcnButton(onClick = {}, variant = ButtonVariant.Ghost, size = ButtonSize.Icon) {
                                    ShadcnText("+")
                                }
                                ShadcnText(
                                    "Okay, but when someone sends a new message the view still feels jarring…",
                                    modifier = Modifier.weight(1f),
                                    muted = true,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                ShadcnButton(onClick = {}, variant = ButtonVariant.Default, size = ButtonSize.Icon) {
                                    ShadcnText("↑")
                                }
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ShadcnCard(
                                modifier = Modifier.width(320.dp).height(420.dp),
                                header = {
                                    ShadcnCardHeader(
                                        title = "New Chat",
                                        description = "How can I help you today?",
                                        action = {
                                            ShadcnButton(
                                                onClick = {},
                                                variant = ButtonVariant.Outline,
                                                size = ButtonSize.Icon,
                                            ) {
                                                ShadcnText("↻")
                                            }
                                        },
                                    )
                                },
                                footer = {
                                    Row(
                                        modifier =
                                            Modifier
                                                .background(
                                                    shadcnTheme.colors.muted,
                                                    RoundedCornerShape(shadcnTheme.shapes.full),
                                                )
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        ShadcnButton(
                                            onClick = {},
                                            variant = ButtonVariant.Ghost,
                                            size = ButtonSize.Icon,
                                        ) {
                                            ShadcnText("+")
                                        }
                                        ShadcnText(
                                            "Okay, but when someone sends a new message the view still feels jarring…",
                                            modifier = Modifier.weight(1f),
                                            style = ShadcnTextStyle.BodySmall,
                                            muted = true,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        ShadcnButton(
                                            onClick = {},
                                            variant = ButtonVariant.Default,
                                            size = ButtonSize.Icon,
                                        ) {
                                            ShadcnText("↑")
                                        }
                                    }
                                },
                            ) {
                                ShadcnMessageScroller(modifier = Modifier.weight(1f)) {
                                    ShadcnMessage(avatar = { ShadcnMessageAvatar { ShadcnText("AI") } }) {
                                        ShadcnBubble {
                                            ShadcnBubbleContent(variant = ShadcnBubbleVariant.Muted) {
                                                ShadcnText("Hi! Ask me anything about MessageScroller.")
                                            }
                                        }
                                    }
                                    ShadcnMessage(
                                        align = ShadcnMessageAlign.End,
                                        avatar = { ShadcnMessageAvatar { ShadcnText("Me") } },
                                    ) {
                                        ShadcnBubble(align = ShadcnMessageAlign.End) {
                                            ShadcnBubbleContent {
                                                ShadcnText("Does it auto-scroll while I'm reading old messages?")
                                            }
                                        }
                                    }
                                    ShadcnMessage(avatar = { ShadcnMessageAvatar { ShadcnText("AI") } }) {
                                        ShadcnBubble {
                                            ShadcnBubbleContent(variant = ShadcnBubbleVariant.Muted) {
                                                ShadcnText(
                                                    "Nope -- only when you're already near the bottom. " +
                                                        "Scroll up and it leaves you alone.",
                                                )
                                            }
                                        }
                                    }
                                    ShadcnMessage(
                                        align = ShadcnMessageAlign.End,
                                        avatar = { ShadcnMessageAvatar { ShadcnText("Me") } },
                                    ) {
                                        ShadcnBubble(align = ShadcnMessageAlign.End) {
                                            ShadcnBubbleContent {
                                                ShadcnText("And this message you're reading right now?")
                                            }
                                        }
                                    }
                                    ShadcnMessage(avatar = { ShadcnMessageAvatar { ShadcnText("AI") } }) {
                                        ShadcnBubble {
                                            ShadcnBubbleContent(variant = ShadcnBubbleVariant.Muted) {
                                                ShadcnText(
                                                    "Proof it works -- you're at the bottom, so it followed me here.",
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            ShadcnText(
                                "Demo is read only.",
                                style = ShadcnTextStyle.LabelSmall,
                                muted = true,
                            )
                        }
                    },
                ),
            ),
    )
