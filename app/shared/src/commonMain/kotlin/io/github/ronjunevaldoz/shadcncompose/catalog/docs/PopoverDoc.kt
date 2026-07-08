@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnPopover
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText

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
                    title = "Default",
                    code =
                        """
                        var open by remember { mutableStateOf(false) }
                        Box {
                            ShadcnButton(onClick = { open = true }) { ShadcnText("Open popover") }
                            ShadcnPopover(expanded = open, onDismissRequest = { open = false }) {
                                ShadcnText("Place content for the popover here.")
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        var open by remember { mutableStateOf(false) }
                        Box {
                            ShadcnButton(onClick = { open = true }) { ShadcnText("Open popover") }
                            ShadcnPopover(expanded = open, onDismissRequest = { open = false }) {
                                ShadcnText("Place content for the popover here.")
                            }
                        }
                    },
                ),
            ),
    )
