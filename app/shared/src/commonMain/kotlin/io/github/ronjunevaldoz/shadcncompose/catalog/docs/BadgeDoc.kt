@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBadge
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.styles.BadgeVariant

val badgeDoc =
    ComponentDoc(
        id = "badge",
        title = "Badge",
        description = "A small label used to highlight status, counts, or metadata.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBadge
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText

            ShadcnBadge { ShadcnText("New") }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code = """ShadcnBadge { ShadcnText("New") }""",
                    preview = { ShadcnBadge { ShadcnText("New") } },
                ),
                ComponentExample(
                    title = "Secondary",
                    code = """ShadcnBadge(variant = BadgeVariant.Secondary) { ShadcnText("Beta") }""",
                    preview = { ShadcnBadge(variant = BadgeVariant.Secondary) { ShadcnText("Beta") } },
                ),
                ComponentExample(
                    title = "Destructive",
                    code = """ShadcnBadge(variant = BadgeVariant.Destructive) { ShadcnText("Error") }""",
                    preview = { ShadcnBadge(variant = BadgeVariant.Destructive) { ShadcnText("Error") } },
                ),
                ComponentExample(
                    title = "Outline",
                    code = """ShadcnBadge(variant = BadgeVariant.Outline) { ShadcnText("Draft") }""",
                    preview = { ShadcnBadge(variant = BadgeVariant.Outline) { ShadcnText("Draft") } },
                ),
            ),
    )
