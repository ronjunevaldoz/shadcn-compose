@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

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
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonSize
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import kotlin.test.Test

/**
 * The auto-scroll/button-visibility decisions ([shouldAutoScrollToBottom],
 * [isMessageScrollerNearBottom]) are unit tested directly in
 * `ShadcnMessageScrollerTest`, per this project's rule against driving decision math
 * through a live gesture in a Compose UI Test. These screenshots instead cover fixed,
 * settled render states -- including the real (non-obvious) behavior that a freshly
 * composed scroller with overflowing content auto-scrolls to the bottom before the
 * first frame is captured, so no floating button appears on initial load.
 */
class MessageScrollerScreenshotTest : ShadcnScreenshotTest() {
    private fun shortContent(darkTheme: Boolean) {
        snapshot("message_scroller_short_content", darkTheme = darkTheme) {
            ShadcnMessageScroller(modifier = Modifier.width(280.dp).height(160.dp)) {
                ShadcnMessage(avatar = { ShadcnMessageAvatar { ShadcnText("AI") } }) {
                    ShadcnText("Hi there!")
                }
            }
        }
    }

    private fun overflowingContentSettlesAtBottom(darkTheme: Boolean) {
        snapshot("message_scroller_overflow_settles_at_bottom", darkTheme = darkTheme) {
            ShadcnMessageScroller(modifier = Modifier.width(280.dp).height(160.dp)) {
                repeat(10) { index ->
                    ShadcnMessage(avatar = { ShadcnMessageAvatar { ShadcnText("AI") } }) {
                        ShadcnText("Message number $index")
                    }
                }
            }
        }
    }

    private fun jumpToBottomButton(darkTheme: Boolean) {
        snapshot("message_scroller_button_visible", darkTheme = darkTheme) {
            ShadcnMessageScrollerButton(visible = true, onClick = {})
        }
    }

    private fun chatPanel(darkTheme: Boolean) {
        snapshot("message_scroller_chat_panel", darkTheme = darkTheme) {
            Column {
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
                    footer = {
                        Row(
                            modifier =
                                Modifier
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
                                style = ShadcnTextStyle.BodySmall,
                                muted = true,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            ShadcnButton(onClick = {}, variant = ButtonVariant.Default, size = ButtonSize.Icon) {
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
                                    ShadcnText(
                                        "Does it auto-scroll while I'm reading old messages?",
                                    )
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
                ShadcnText("Demo is read only.", style = ShadcnTextStyle.LabelSmall, muted = true)
            }
        }
    }

    @Test fun short_content_light() = shortContent(darkTheme = false)

    @Test fun short_content_dark() = shortContent(darkTheme = true)

    @Test fun overflowing_content_settles_at_bottom_light() = overflowingContentSettlesAtBottom(darkTheme = false)

    @Test fun overflowing_content_settles_at_bottom_dark() = overflowingContentSettlesAtBottom(darkTheme = true)

    @Test fun jump_to_bottom_button_light() = jumpToBottomButton(darkTheme = false)

    @Test fun chat_panel_light() = chatPanel(darkTheme = false)

    @Test fun chat_panel_dark() = chatPanel(darkTheme = true)
}
