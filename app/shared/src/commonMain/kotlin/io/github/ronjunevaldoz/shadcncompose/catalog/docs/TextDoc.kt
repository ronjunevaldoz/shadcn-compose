package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Column
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle

val textDoc =
    ComponentDoc(
        id = "text",
        title = "Text",
        description = "Renders text using the design system's type scale, with an optional muted tone.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle

            ShadcnText("Title", style = ShadcnTextStyle.TitleLarge)
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Type scale",
                    code =
                        """
                        ShadcnText("Title Large", style = ShadcnTextStyle.TitleLarge)
                        ShadcnText("Body Medium", style = ShadcnTextStyle.BodyMedium)
                        """.trimIndent(),
                    preview = {
                        Column {
                            ShadcnText("Title Large", style = ShadcnTextStyle.TitleLarge)
                            ShadcnText("Body Medium", style = ShadcnTextStyle.BodyMedium)
                        }
                    },
                ),
                ComponentExample(
                    title = "Muted",
                    code = """ShadcnText("Muted body", style = ShadcnTextStyle.BodyMedium, muted = true)""",
                    preview = { ShadcnText("Muted body", style = ShadcnTextStyle.BodyMedium, muted = true) },
                ),
            ),
    )
