@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.Style
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ButtonGroupItem
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButtonGroup
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButtonGroupSeparator
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButtonGroupText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

val buttonGroupDoc =
    ComponentDoc(
        id = "button-group",
        title = "Button Group",
        description = "Visually joins a row of buttons into a single segmented control.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ButtonGroupItem
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButtonGroup

            ShadcnButtonGroup(
                items = listOf(
                    ButtonGroupItem("Archive", onClick = {}),
                    ButtonGroupItem("Report", onClick = {}),
                    ButtonGroupItem("Snooze", onClick = {}),
                ),
            )
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        ShadcnButtonGroup(
                            items = listOf(
                                ButtonGroupItem("Archive", onClick = {}),
                                ButtonGroupItem("Report", onClick = {}),
                                ButtonGroupItem("Snooze", onClick = {}),
                            ),
                        )
                        """.trimIndent(),
                    preview = {
                        ShadcnButtonGroup(
                            items =
                                listOf(
                                    ButtonGroupItem("Archive", onClick = {}),
                                    ButtonGroupItem("Report", onClick = {}),
                                    ButtonGroupItem("Snooze", onClick = {}),
                                ),
                        )
                    },
                ),
                ComponentExample(
                    title = "Outline",
                    code =
                        """
                        ShadcnButtonGroup(
                            items = listOf(
                                ButtonGroupItem("Copy",variant = ButtonVariant.Outline, onClick = {}),
                                ButtonGroupItem("Share",variant = ButtonVariant.Outline, onClick = {}),
                            ),
                        )
                        """.trimIndent(),
                    preview = {
                        ShadcnButtonGroup(
                            items =
                                listOf(
                                    ButtonGroupItem("Copy", variant = ButtonVariant.Outline, onClick = {}),
                                    ButtonGroupItem("Share", variant = ButtonVariant.Outline, onClick = {}),
                                ),
                        )
                    },
                ),
                ComponentExample(
                    title = "With label",
                    code =
                        """
                        val rounded = shadcnTheme.shapes.lg
                        ShadcnButtonGroup {
                            ShadcnButtonGroupText("https://", topStart = rounded, bottomStart = rounded)
                            ShadcnButtonGroupSeparator()
                            ShadcnButton(
                                onClick = {},
                                variant = ButtonVariant.Ghost,
                                style = Style {
                                    shape(RoundedCornerShape(0.dp, rounded, rounded, 0.dp))
                                }
                            ) { ShadcnText("example.com") }
                        }
                        """.trimIndent(),
                    preview = {
                        val rounded = shadcnTheme.shapes.lg
                        ShadcnButtonGroup {
                            ShadcnButtonGroupText("https://", topStart = rounded, bottomStart = rounded)
                            ShadcnButtonGroupSeparator()
                            ShadcnButton(
                                onClick = {},
                                variant = ButtonVariant.Ghost,
                                style =
                                    Style {
                                        shape(RoundedCornerShape(0.dp, rounded, rounded, 0.dp))
                                    },
                            ) { ShadcnText("example.com") }
                        }
                    },
                ),
            ),
    )
