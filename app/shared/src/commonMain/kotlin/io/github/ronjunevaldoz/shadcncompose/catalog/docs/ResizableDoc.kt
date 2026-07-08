package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnResizableHandle
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnResizablePanelGroup
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

val resizableDoc =
    ComponentDoc(
        id = "resizable",
        title = "Resizable",
        description = "Two panes divided by a draggable handle.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnResizablePanelGroup
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnResizableHandle

            ShadcnResizablePanelGroup { first, second, onHandleDrag ->
                Box(first) { ShadcnText("One") }
                ShadcnResizableHandle(onDrag = onHandleDrag)
                Box(second) { ShadcnText("Two") }
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Sidebar and content",
                    code =
                        """
                        ShadcnResizablePanelGroup(
                            modifier = Modifier.width(320.dp).height(160.dp),
                            initialFraction = 0.35f,
                        ) { first, second, onHandleDrag ->
                            Box(
                                first.background(shadcnTheme.colors.muted),
                                contentAlignment = Alignment.Center,
                            ) { ShadcnText("Sidebar", muted = true) }
                            ShadcnResizableHandle(onDrag = onHandleDrag)
                            Box(second, contentAlignment = Alignment.Center) { ShadcnText("Content") }
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnResizablePanelGroup(
                            modifier = Modifier.width(320.dp).height(160.dp),
                            initialFraction = 0.35f,
                        ) { first, second, onHandleDrag ->
                            Box(
                                first.background(shadcnTheme.colors.muted),
                                contentAlignment = Alignment.Center,
                            ) { ShadcnText("Sidebar", muted = true) }
                            ShadcnResizableHandle(onDrag = onHandleDrag)
                            Box(second, contentAlignment = Alignment.Center) { ShadcnText("Content") }
                        }
                    },
                ),
            ),
    )
