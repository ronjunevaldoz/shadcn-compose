package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSeparator
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSeparatorOrientation
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle

val separatorDoc =
    ComponentDoc(
        id = "separator",
        title = "Separator",
        description =
            "A thin line for visually or semantically separating content. Matches real " +
                "shadcn/ui: it has no built-in margin, so surrounding spacing is always the " +
                "caller's responsibility -- via Arrangement.spacedBy or explicit padding, as " +
                "shown in the examples below.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSeparator

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ShadcnText("Content above")
                ShadcnSeparator()
                ShadcnText("Content below")
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Horizontal, with spacing",
                    code =
                        """
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ShadcnText("Radix Primitives", style = ShadcnTextStyle.LabelLarge)
                                ShadcnText("An open-source UI component library.", muted = true)
                            }
                            ShadcnSeparator()
                            ShadcnText("Unstyled, accessible components for building high-quality design systems.")
                        }
                        """.trimIndent(),
                    preview = {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ShadcnText("Radix Primitives", style = ShadcnTextStyle.LabelLarge)
                                ShadcnText("An open-source UI component library.", muted = true)
                            }
                            ShadcnSeparator()
                            ShadcnText(
                                "Unstyled, accessible components for building high-quality design systems.",
                            )
                        }
                    },
                ),
                ComponentExample(
                    title = "Vertical, with spacing",
                    code =
                        """
                        Row(
                            modifier = Modifier.height(20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            ShadcnText("Blog")
                            ShadcnSeparator(orientation = ShadcnSeparatorOrientation.Vertical)
                            ShadcnText("Docs")
                            ShadcnSeparator(orientation = ShadcnSeparatorOrientation.Vertical)
                            ShadcnText("Source")
                        }
                        """.trimIndent(),
                    preview = {
                        Row(
                            modifier = Modifier.height(20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            ShadcnText("Blog")
                            ShadcnSeparator(orientation = ShadcnSeparatorOrientation.Vertical)
                            ShadcnText("Docs")
                            ShadcnSeparator(orientation = ShadcnSeparatorOrientation.Vertical)
                            ShadcnText("Source")
                        }
                    },
                ),
                ComponentExample(
                    title = "Bare (no spacing)",
                    code = "ShadcnSeparator()",
                    preview = { ShadcnSeparator() },
                ),
            ),
    )
