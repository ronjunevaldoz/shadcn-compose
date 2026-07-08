package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnScrollArea
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSeparator
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText

val scrollAreaDoc =
    ComponentDoc(
        id = "scroll-area",
        title = "Scroll Area",
        description = "A scrollable region with a slim custom scrollbar thumb instead of the native one.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnScrollArea

            ShadcnScrollArea(modifier = Modifier.height(200.dp)) {
                Column { repeat(20) { ShadcnText("Item ${'$'}it") } }
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        ShadcnScrollArea(modifier = Modifier.height(160.dp)) {
                            Column {
                                repeat(20) { index ->
                                    ShadcnText("Tag ${'$'}index", modifier = Modifier.padding(vertical = 6.dp))
                                    ShadcnSeparator()
                                }
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnScrollArea(modifier = Modifier.height(160.dp)) {
                            Column {
                                repeat(20) { index ->
                                    ShadcnText("Tag $index", modifier = Modifier.padding(vertical = 6.dp))
                                    ShadcnSeparator()
                                }
                            }
                        }
                    },
                ),
            ),
    )
