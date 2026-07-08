@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAlertDialog
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialogDescription
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialogFooter
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialogHeader
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialogTitle
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant

val alertDialogDoc =
    ComponentDoc(
        id = "alert-dialog",
        title = "Alert Dialog",
        description = "A Dialog that can't be dismissed by clicking outside -- requires an explicit choice.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAlertDialog

            var open by remember { mutableStateOf(false) }
            ShadcnAlertDialog(visible = open, onDismissRequest = { open = false }) {
                ShadcnDialogHeader {
                    ShadcnDialogTitle("Are you absolutely sure?")
                    ShadcnDialogDescription("This action cannot be undone.")
                }
                ShadcnDialogFooter {
                    ShadcnButton(onClick = { open = false }, variant = ButtonVariant.Outline) { ShadcnText("Cancel") }
                    ShadcnButton(onClick = { open = false }, variant = ButtonVariant.Destructive) { ShadcnText("Continue") }
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
                        ShadcnButton(onClick = { open = true }, variant = ButtonVariant.Outline) { ShadcnText("Show Dialog") }
                        ShadcnAlertDialog(visible = open, onDismissRequest = { open = false }) {
                            ShadcnDialogHeader {
                                ShadcnDialogTitle("Are you absolutely sure?")
                                ShadcnDialogDescription(
                                    "This action cannot be undone. This will permanently delete your account.",
                                )
                            }
                            ShadcnDialogFooter {
                                ShadcnButton(onClick = { open = false }, variant = ButtonVariant.Outline) {
                                    ShadcnText("Cancel")
                                }
                                ShadcnButton(onClick = { open = false }, variant = ButtonVariant.Destructive) {
                                    ShadcnText("Continue")
                                }
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        var open by remember { mutableStateOf(false) }
                        ShadcnButton(onClick = { open = true }, variant = ButtonVariant.Outline) {
                            ShadcnText("Show Dialog")
                        }
                        ShadcnAlertDialog(visible = open, onDismissRequest = { open = false }) {
                            ShadcnDialogHeader {
                                ShadcnDialogTitle("Are you absolutely sure?")
                                ShadcnDialogDescription(
                                    "This action cannot be undone. This will permanently delete your account.",
                                )
                            }
                            ShadcnDialogFooter {
                                ShadcnButton(onClick = { open = false }, variant = ButtonVariant.Outline) {
                                    ShadcnText("Cancel")
                                }
                                ShadcnButton(onClick = { open = false }, variant = ButtonVariant.Destructive) {
                                    ShadcnText("Continue")
                                }
                            }
                        }
                    },
                ),
            ),
    )
