@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDropdownMenu
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDropdownMenuCheckboxItem
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDropdownMenuItem
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDropdownMenuLabel
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDropdownMenuRadioGroup
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDropdownMenuRadioItem
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
                ComponentExample(
                    title = "Checkboxes and radio group",
                    code =
                        """
                        var open by remember { mutableStateOf(false) }
                        var statusBar by remember { mutableStateOf(true) }
                        var activityBar by remember { mutableStateOf(false) }
                        var panelPosition by remember { mutableStateOf("bottom") }
                        Box {
                            ShadcnButton(onClick = { open = true }) { ShadcnText("Open") }
                            ShadcnDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                                ShadcnDropdownMenuLabel("Appearance", inset = true)
                                ShadcnDropdownMenuCheckboxItem(
                                    "Status Bar",
                                    checked = statusBar,
                                    onCheckedChange = { statusBar = it },
                                )
                                ShadcnDropdownMenuCheckboxItem(
                                    "Activity Bar",
                                    checked = activityBar,
                                    onCheckedChange = { activityBar = it },
                                    shortcut = "⌘B",
                                )
                                ShadcnDropdownMenuSeparator()
                                ShadcnDropdownMenuLabel("Panel Position", inset = true)
                                ShadcnDropdownMenuRadioGroup(
                                    value = panelPosition,
                                    onValueChange = { panelPosition = it },
                                ) {
                                    ShadcnDropdownMenuRadioItem("Top", value = "top")
                                    ShadcnDropdownMenuRadioItem("Bottom", value = "bottom")
                                    ShadcnDropdownMenuRadioItem("Right", value = "right")
                                }
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        var open by remember { mutableStateOf(false) }
                        var statusBar by remember { mutableStateOf(true) }
                        var activityBar by remember { mutableStateOf(false) }
                        var panelPosition by remember { mutableStateOf("bottom") }
                        Box {
                            ShadcnButton(onClick = { open = true }) { ShadcnText("Open") }
                            ShadcnDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                                ShadcnDropdownMenuLabel("Appearance", inset = true)
                                ShadcnDropdownMenuCheckboxItem(
                                    "Status Bar",
                                    checked = statusBar,
                                    onCheckedChange = { statusBar = it },
                                )
                                ShadcnDropdownMenuCheckboxItem(
                                    "Activity Bar",
                                    checked = activityBar,
                                    onCheckedChange = { activityBar = it },
                                    shortcut = "⌘B",
                                )
                                ShadcnDropdownMenuSeparator()
                                ShadcnDropdownMenuLabel("Panel Position", inset = true)
                                ShadcnDropdownMenuRadioGroup(
                                    value = panelPosition,
                                    onValueChange = { panelPosition = it },
                                ) {
                                    ShadcnDropdownMenuRadioItem("Top", value = "top")
                                    ShadcnDropdownMenuRadioItem("Bottom", value = "bottom")
                                    ShadcnDropdownMenuRadioItem("Right", value = "right")
                                }
                            }
                        }
                    },
                ),
            ),
    )
