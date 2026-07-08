@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialog
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialogDescription
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialogFooter
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialogHeader
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialogTitle
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText

val dialogDoc =
    ComponentDoc(
        id = "dialog",
        title = "Dialog",
        description = "A modal window layered over the page.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialog

            var open by remember { mutableStateOf(false) }
            ShadcnDialog(visible = open, onDismissRequest = { open = false }) {
                ShadcnDialogHeader {
                    ShadcnDialogTitle("Edit profile")
                    ShadcnDialogDescription("Make changes to your profile here.")
                }
                ShadcnDialogFooter {
                    ShadcnButton(onClick = { open = false }) { ShadcnText("Save changes") }
                }
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        var open by remember { mutableStateOf(false) }
                        ShadcnButton(onClick = { open = true }) { ShadcnText("Edit profile") }
                        ShadcnDialog(visible = open, onDismissRequest = { open = false }) {
                            ShadcnDialogHeader {
                                ShadcnDialogTitle("Edit profile")
                                ShadcnDialogDescription("Make changes to your profile here. Click save when you're done.")
                            }
                            ShadcnDialogFooter {
                                ShadcnButton(onClick = { open = false }) { ShadcnText("Save changes") }
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        var open by remember { mutableStateOf(false) }
                        ShadcnButton(onClick = { open = true }) { ShadcnText("Edit profile") }
                        ShadcnDialog(visible = open, onDismissRequest = { open = false }) {
                            ShadcnDialogHeader {
                                ShadcnDialogTitle("Edit profile")
                                ShadcnDialogDescription(
                                    "Make changes to your profile here. Click save when you're done.",
                                )
                            }
                            ShadcnDialogFooter {
                                ShadcnButton(onClick = { open = false }) { ShadcnText("Save changes") }
                            }
                        }
                    },
                ),
            ),
    )
