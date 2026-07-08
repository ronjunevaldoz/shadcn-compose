package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAvatar
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAvatarFallback
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnHoverCard
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

val hoverCardDoc =
    ComponentDoc(
        id = "hover-card",
        title = "Hover Card",
        description = "A hover-triggered panel for richer preview content than a Tooltip.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnHoverCard

            ShadcnHoverCard(trigger = { ShadcnText("@shadcn") }) {
                // avatar, name, description, "Joined December 2021" row
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        ShadcnHoverCard(trigger = { ShadcnText("@shadcn") }) {
                            Row(modifier = Modifier.width(240.dp)) {
                                ShadcnAvatar { ShadcnAvatarFallback("SC") }
                                Column {
                                    ShadcnText("@shadcn", style = ShadcnTextStyle.LabelSmall)
                                    ShadcnText(
                                        "The React Framework – created and maintained by @vercel.",
                                        style = ShadcnTextStyle.BodySmall,
                                    )
                                    ShadcnText("Joined December 2021", style = ShadcnTextStyle.BodySmall, muted = true)
                                }
                            }
                        }
                        """.trimIndent(),
                    preview = { HoverCardPreview() },
                ),
            ),
    )

@Composable
private fun HoverCardPreview() {
    ShadcnHoverCard(trigger = { ShadcnText("@shadcn") }) {
        Row(modifier = Modifier.width(240.dp)) {
            ShadcnAvatar { ShadcnAvatarFallback("SC") }
            Column(
                modifier = Modifier.padding(start = shadcnTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xs),
            ) {
                ShadcnText("@shadcn", style = ShadcnTextStyle.LabelSmall)
                ShadcnText(
                    "The React Framework – created and maintained by @vercel.",
                    style = ShadcnTextStyle.BodySmall,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ShadcnText("Joined December 2021", style = ShadcnTextStyle.BodySmall, muted = true)
                }
            }
        }
    }
}
