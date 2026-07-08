package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import io.github.ronjunevaldoz.shadcncompose.components.ShadcnHoverCard
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText

val hoverCardDoc =
    ComponentDoc(
        id = "hover-card",
        title = "Hover Card",
        description = "A hover-triggered panel for richer preview content than a Tooltip.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnHoverCard

            ShadcnHoverCard(trigger = { ShadcnText("@shadcn") }) {
                ShadcnText("The React Framework – created and maintained by @vercel.")
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        ShadcnHoverCard(trigger = { ShadcnText("@shadcn") }) {
                            ShadcnText("The React Framework – created and maintained by @vercel.")
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnHoverCard(trigger = { ShadcnText("@shadcn") }) {
                            ShadcnText("The React Framework – created and maintained by @vercel.")
                        }
                    },
                ),
            ),
    )
