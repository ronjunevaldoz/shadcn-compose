@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnField
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnFieldGroup
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnFieldLabel
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnPopover
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextField
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

val popoverDoc =
    ComponentDoc(
        id = "popover",
        title = "Popover",
        description = "A click-triggered anchored panel for rich content.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnPopover

            var open by remember { mutableStateOf(false) }
            Box {
                ShadcnButton(onClick = { open = true }) { ShadcnText("Open popover") }
                ShadcnPopover(expanded = open, onDismissRequest = { open = false }) {
                    ShadcnText("Place content for the popover here.")
                }
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Dimensions form",
                    code =
                        """
                        var open by remember { mutableStateOf(false) }
                        var width by remember { mutableStateOf("100%") }
                        var height by remember { mutableStateOf("25px") }
                        Box {
                            ShadcnButton(onClick = { open = true }, variant = ButtonVariant.Outline) {
                                ShadcnText("Open popover")
                            }
                            ShadcnPopover(expanded = open, onDismissRequest = { open = false }) {
                                Column(
                                    modifier = Modifier.width(240.dp),
                                    verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm),
                                ) {
                                    ShadcnText("Dimensions", style = ShadcnTextStyle.TitleSmall)
                                    ShadcnText(
                                        "Set the dimensions for the layer.",
                                        style = ShadcnTextStyle.BodySmall,
                                        muted = true,
                                    )
                                    ShadcnFieldGroup {
                                        ShadcnField {
                                            ShadcnFieldLabel("Width")
                                            ShadcnTextField(value = width, onValueChange = { width = it })
                                        }
                                        ShadcnField {
                                            ShadcnFieldLabel("Height")
                                            ShadcnTextField(value = height, onValueChange = { height = it })
                                        }
                                    }
                                }
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        var open by remember { mutableStateOf(false) }
                        var width by remember { mutableStateOf("100%") }
                        var height by remember { mutableStateOf("25px") }
                        Box {
                            ShadcnButton(onClick = { open = true }, variant = ButtonVariant.Outline) {
                                ShadcnText("Open popover")
                            }
                            ShadcnPopover(expanded = open, onDismissRequest = { open = false }) {
                                Column(
                                    modifier = Modifier.width(240.dp),
                                    verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm),
                                ) {
                                    ShadcnText("Dimensions", style = ShadcnTextStyle.TitleSmall)
                                    ShadcnText(
                                        "Set the dimensions for the layer.",
                                        style = ShadcnTextStyle.BodySmall,
                                        muted = true,
                                    )
                                    ShadcnFieldGroup {
                                        ShadcnField {
                                            ShadcnFieldLabel("Width")
                                            ShadcnTextField(value = width, onValueChange = { width = it })
                                        }
                                        ShadcnField {
                                            ShadcnFieldLabel("Height")
                                            ShadcnTextField(value = height, onValueChange = { height = it })
                                        }
                                    }
                                }
                            }
                        }
                    },
                ),
            ),
    )
