@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialogDescription
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialogTitle
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSheet
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText

val sheetDoc =
    ComponentDoc(
        id = "sheet",
        title = "Sheet",
        description = "A modal panel that slides in from a screen edge.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSheet

            var open by remember { mutableStateOf(false) }
            ShadcnSheet(visible = open, onDismissRequest = { open = false }) {
                ShadcnDialogTitle("Edit profile")
                ShadcnDialogDescription("Make changes to your profile here.")
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default (right edge)",
                    code =
                        """
                        var open by remember { mutableStateOf(false) }
                        ShadcnButton(onClick = { open = true }) { ShadcnText("Open") }
                        ShadcnSheet(visible = open, onDismissRequest = { open = false }) {
                            ShadcnDialogTitle("Edit profile")
                            ShadcnDialogDescription("Make changes to your profile here. Click save when you're done.")
                        }
                        """.trimIndent(),
                    preview = {
                        var open by remember { mutableStateOf(false) }
                        ShadcnButton(onClick = { open = true }) { ShadcnText("Open") }
                        ShadcnSheet(visible = open, onDismissRequest = { open = false }) {
                            ShadcnDialogTitle("Edit profile")
                            ShadcnDialogDescription(
                                "Make changes to your profile here. Click save when you're done.",
                            )
                        }
                    },
                ),
            ),
    )
