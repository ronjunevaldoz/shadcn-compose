@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialogDescription
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDialogTitle
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDrawer
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText

val drawerDoc =
    ComponentDoc(
        id = "drawer",
        title = "Drawer",
        description =
            "A modal panel that slides in from a screen edge and can be swiped back toward that edge to " +
                "dismiss. Distinct from Sheet: same sliding-panel shape, but with real drag-to-dismiss physics.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDrawer

            var open by remember { mutableStateOf(false) }
            ShadcnDrawer(visible = open, onDismissRequest = { open = false }) {
                ShadcnDialogTitle("Edit profile")
                ShadcnDialogDescription("Make changes to your profile here.")
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default (bottom edge, drag to dismiss)",
                    code =
                        """
                        var open by remember { mutableStateOf(false) }
                        ShadcnButton(onClick = { open = true }) { ShadcnText("Open drawer") }
                        ShadcnDrawer(visible = open, onDismissRequest = { open = false }) {
                            ShadcnDialogTitle("Edit profile")
                            ShadcnDialogDescription("Make changes to your profile here. Click save when you're done.")
                        }
                        """.trimIndent(),
                    preview = {
                        var open by remember { mutableStateOf(false) }
                        ShadcnButton(onClick = { open = true }) { ShadcnText("Open drawer") }
                        ShadcnDrawer(visible = open, onDismissRequest = { open = false }) {
                            ShadcnDialogTitle("Edit profile")
                            ShadcnDialogDescription(
                                "Make changes to your profile here. Click save when you're done.",
                            )
                        }
                    },
                ),
            ),
    )
