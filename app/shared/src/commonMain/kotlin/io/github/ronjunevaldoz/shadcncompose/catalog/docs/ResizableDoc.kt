package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnResizableHandle
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnResizablePanelGroup
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText

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
                    title = "Default",
                    code =
                        """
                        ShadcnResizablePanelGroup(modifier = Modifier.height(160.dp)) { first, second, onHandleDrag ->
                            Box(first, contentAlignment = Alignment.Center) { ShadcnText("One") }
                            ShadcnResizableHandle(onDrag = onHandleDrag)
                            Box(second, contentAlignment = Alignment.Center) { ShadcnText("Two") }
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnResizablePanelGroup(modifier = Modifier.height(160.dp)) { first, second, onHandleDrag ->
                            Box(first, contentAlignment = Alignment.Center) { ShadcnText("One") }
                            ShadcnResizableHandle(onDrag = onHandleDrag)
                            Box(second, contentAlignment = Alignment.Center) { ShadcnText("Two") }
                        }
                    },
                ),
            ),
    )
