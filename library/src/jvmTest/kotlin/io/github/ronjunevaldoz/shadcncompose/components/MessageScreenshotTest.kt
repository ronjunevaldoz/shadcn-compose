package io.github.ronjunevaldoz.shadcncompose.components

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

    @Test fun start_aligned_light() = startAligned(darkTheme = false)

    @Test fun start_aligned_dark() = startAligned(darkTheme = true)

    @Test fun end_aligned_light() = endAligned(darkTheme = false)

    @Test fun end_aligned_dark() = endAligned(darkTheme = true)

    @Test fun group_light() = group(darkTheme = false)
}
