@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDropdownMenu
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDropdownMenuItem
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDropdownMenuLabel
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDropdownMenuSeparator
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText

val dropdownMenuDoc =
    ComponentDoc(
        id = "dropdown-menu",
        title = "Dropdown Menu",
        description = "An anchored list of actions, optionally grouped under labels and separators.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDropdownMenu
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDropdownMenuItem

            var open by remember { mutableStateOf(false) }
            Box {
                ShadcnButton(onClick = { open = true }) { ShadcnText("Open") }
                ShadcnDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                    ShadcnDropdownMenuLabel("My Account")
                    ShadcnDropdownMenuSeparator()
                    ShadcnDropdownMenuItem("Profile", onClick = {})
                    ShadcnDropdownMenuItem("Billing", onClick = {})
                    ShadcnDropdownMenuSeparator()
                    ShadcnDropdownMenuItem("Log out", onClick = {}, destructive = true)
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
                            ShadcnButton(onClick = { open = true }) { ShadcnText("Open") }
                            ShadcnDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                                ShadcnDropdownMenuLabel("My Account")
                                ShadcnDropdownMenuSeparator()
                                ShadcnDropdownMenuItem("Profile", onClick = {})
                                ShadcnDropdownMenuItem("Billing", onClick = {})
                                ShadcnDropdownMenuItem("Team", onClick = {})
                                ShadcnDropdownMenuSeparator()
                                ShadcnDropdownMenuItem("Log out", onClick = {}, destructive = true)
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        var open by remember { mutableStateOf(false) }
                        Box {
                            ShadcnButton(onClick = { open = true }) { ShadcnText("Open") }
                            ShadcnDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                                ShadcnDropdownMenuLabel("My Account")
                                ShadcnDropdownMenuSeparator()
                                ShadcnDropdownMenuItem("Profile", onClick = {})
                                ShadcnDropdownMenuItem("Billing", onClick = {})
                                ShadcnDropdownMenuItem("Team", onClick = {})
                                ShadcnDropdownMenuSeparator()
                                ShadcnDropdownMenuItem("Log out", onClick = {}, destructive = true)
                            }
                        }
                    },
                ),
            ),
    )
