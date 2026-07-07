@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.Style
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
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
                    ButtonGroupItem("Copy", onClick = {}),
                    ButtonGroupItem("Share", onClick = {}),
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
                                ButtonGroupItem("Copy", onClick = {}),
                                ButtonGroupItem("Share", onClick = {}),
                            ),
                        )
                        """.trimIndent(),
                    preview = {
                        ShadcnButtonGroup(
                            items =
                                listOf(
                                    ButtonGroupItem("Copy", onClick = {}),
                                    ButtonGroupItem("Share", onClick = {}),
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
                        ShadcnButtonGroup {
                            ShadcnButtonGroupText("https://")
                            ShadcnButtonGroupSeparator()
                            ShadcnButton(onClick = {}, variant = ButtonVariant.Ghost) { ShadcnText("example.com") }
                        }
                        """.trimIndent(),
                    preview = {
//                        ShadcnButtonGroup {
//                            ShadcnButtonGroupText("https://")
//                            ShadcnButtonGroupSeparator()
//                            ShadcnButton(
//                                onClick = {},
//                                variant = ButtonVariant.Ghost,
//                                style = Style {
//                                    // TODO workaround so that last item has no separator
//                                    val itemShape = RoundedCornerShape(0.dp, shadcnTheme.shapes.lg, shadcnTheme.shapes.lg, 0.dp)
//                                    shape(itemShape)
//                                }
//                            ) { ShadcnText("example.com") }
//                        }
                        ShadcnButtonGroup(
                            items = listOf(
                                ButtonGroupItem("https://", onClick = {}),
                                ButtonGroupItem("example.com", onClick = {}),
                            )
                        )
//                        ShadcnButtonGroup {
//                            ShadcnButtonGroupText("https://")
//                            ShadcnButtonGroupSeparator()
//                            ShadcnButton(onClick = {}, variant = ButtonVariant.Ghost) { ShadcnText("example.com") }
//                        }
                    },
                ),
            ),
    )