package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnContextMenu
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDropdownMenuItem
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

val contextMenuDoc =
    ComponentDoc(
        id = "context-menu",
        title = "Context Menu",
        description = "A menu triggered by right-click, opened at the cursor.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnContextMenu
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDropdownMenuItem

            ShadcnContextMenu(items = listOf(ShadcnDropdownMenuItem("Copy", onClick = {}))) {
                ShadcnText("Right-click me")
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        ShadcnContextMenu(
                            items = listOf(
                                ShadcnDropdownMenuItem("Back", onClick = {}),
                                ShadcnDropdownMenuItem("Reload", onClick = {}),
                            ),
                        ) {
                            Box(
                                modifier = Modifier.size(width = 240.dp, height = 80.dp)
                                    .background(shadcnTheme.colors.muted, RoundedCornerShape(shadcnTheme.shapes.md)),
                                contentAlignment = Alignment.Center,
                            ) {
                                ShadcnText("Right-click here", muted = true)
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnContextMenu(
                            items =
                                listOf(
                                    ShadcnDropdownMenuItem("Back", onClick = {}),
                                    ShadcnDropdownMenuItem("Reload", onClick = {}),
                                ),
                        ) {
                            Box(
                                modifier =
                                    Modifier.size(width = 240.dp, height = 80.dp)
                                        .background(
                                            shadcnTheme.colors.muted,
                                            RoundedCornerShape(shadcnTheme.shapes.md),
                                        ),
                                contentAlignment = Alignment.Center,
                            ) {
                                ShadcnText("Right-click here", muted = true)
                            }
                        }
                    },
                ),
            ),
    )
