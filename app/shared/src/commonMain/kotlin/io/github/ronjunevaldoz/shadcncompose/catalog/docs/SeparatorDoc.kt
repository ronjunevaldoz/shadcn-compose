package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSeparator
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSeparatorOrientation
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText

val separatorDoc =
    ComponentDoc(
        id = "separator",
        title = "Separator",
        description = "A thin line for visually or semantically separating content.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSeparator

            ShadcnSeparator()
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Horizontal",
                    code = "ShadcnSeparator()",
                    preview = { ShadcnSeparator() },
                ),
                ComponentExample(
                    title = "Vertical",
                    code =
                        """
                        Row(modifier = Modifier.height(24.dp)) {
                            ShadcnText("Blog")
                            ShadcnSeparator(orientation = ShadcnSeparatorOrientation.Vertical)
                            ShadcnText("Docs")
                        }
                        """.trimIndent(),
                    preview = {
                        Row(modifier = Modifier.height(24.dp)) {
                            ShadcnText("Blog")
                            ShadcnSeparator(orientation = ShadcnSeparatorOrientation.Vertical)
                            ShadcnText("Docs")
                        }
                    },
                ),
            ),
    )
