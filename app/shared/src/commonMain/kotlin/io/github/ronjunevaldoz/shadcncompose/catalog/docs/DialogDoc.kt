@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialog
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialogDescription
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialogFooter
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialogHeader
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialogTitle
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnField
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnFieldGroup
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnFieldLabel
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextField
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant

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
                    title = "Edit profile",
                    code =
                        """
                        var open by remember { mutableStateOf(false) }
                        var name by remember { mutableStateOf("Pedro Duarte") }
                        var username by remember { mutableStateOf("@peduarte") }
                        ShadcnButton(onClick = { open = true }) { ShadcnText("Edit profile") }
                        ShadcnDialog(visible = open, onDismissRequest = { open = false }) {
                            ShadcnDialogHeader {
                                ShadcnDialogTitle("Edit profile")
                                ShadcnDialogDescription("Make changes to your profile here. Click save when you're done.")
                            }
                            ShadcnFieldGroup(modifier = Modifier.padding(top = 16.dp)) {
                                ShadcnField {
                                    ShadcnFieldLabel("Name")
                                    ShadcnTextField(value = name, onValueChange = { name = it })
                                }
                                ShadcnField {
                                    ShadcnFieldLabel("Username")
                                    ShadcnTextField(value = username, onValueChange = { username = it })
                                }
                            }
                            ShadcnDialogFooter(modifier = Modifier.padding(top = 16.dp)) {
                                ShadcnButton(onClick = { open = false }, variant = ButtonVariant.Outline) {
                                    ShadcnText("Cancel")
                                }
                                ShadcnButton(onClick = { open = false }) { ShadcnText("Save changes") }
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        var open by remember { mutableStateOf(false) }
                        var name by remember { mutableStateOf("Pedro Duarte") }
                        var username by remember { mutableStateOf("@peduarte") }
                        ShadcnButton(onClick = { open = true }) { ShadcnText("Edit profile") }
                        ShadcnDialog(visible = open, onDismissRequest = { open = false }) {
                            ShadcnDialogHeader {
                                ShadcnDialogTitle("Edit profile")
                                ShadcnDialogDescription(
                                    "Make changes to your profile here. Click save when you're done.",
                                )
                            }
                            ShadcnFieldGroup(modifier = Modifier.padding(top = 16.dp)) {
                                ShadcnField {
                                    ShadcnFieldLabel("Name")
                                    ShadcnTextField(value = name, onValueChange = { name = it })
                                }
                                ShadcnField {
                                    ShadcnFieldLabel("Username")
                                    ShadcnTextField(value = username, onValueChange = { username = it })
                                }
                            }
                            ShadcnDialogFooter(modifier = Modifier.padding(top = 16.dp)) {
                                ShadcnButton(onClick = { open = false }, variant = ButtonVariant.Outline) {
                                    ShadcnText("Cancel")
                                }
                                ShadcnButton(onClick = { open = false }) { ShadcnText("Save changes") }
                            }
                        }
                    },
                ),
            ),
    )
