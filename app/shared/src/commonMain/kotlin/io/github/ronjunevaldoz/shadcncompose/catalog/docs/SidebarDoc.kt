@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBreadcrumb
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBreadcrumbLink
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBreadcrumbPage
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBreadcrumbSeparator
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCard
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSidebar
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSidebarGroup
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSidebarInset
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSidebarMenu
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSidebarMenuItem
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSidebarProvider
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSidebarTrigger
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.styles.CardSize
import io.github.ronjunevaldoz.heroicons.outline.Bars3

private val sidebarItems =
    listOf(
        ShadcnSidebarMenuItem("home", "Home"),
        ShadcnSidebarMenuItem("inbox", "Inbox"),
        ShadcnSidebarMenuItem("settings", "Settings"),
    )

val sidebarDoc =
    ComponentDoc(
        id = "sidebar",
        title = "Sidebar",
        description = "A collapsible side navigation rail with grouped menu sections and a main content inset.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.*

            var expanded by remember { mutableStateOf(true) }
            ShadcnSidebarProvider(expanded = expanded, onExpandedChange = { expanded = it }) {
                ShadcnSidebar {
                    ShadcnSidebarGroup(label = "Application") {
                        ShadcnSidebarMenu(items = items, activeId = "home", onItemClick = {})
                    }
                }
                ShadcnSidebarInset {
                    ShadcnSidebarTrigger()
                    ShadcnText("Main content")
                }
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "App shell",
                    code =
                        """
                        var expanded by remember { mutableStateOf(true) }
                        var active by remember { mutableStateOf("home") }
                        ShadcnSidebarProvider(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            modifier = Modifier.height(260.dp),
                        ) {
                            ShadcnSidebar {
                                ShadcnSidebarGroup(label = "Application") {
                                    ShadcnSidebarMenu(
                                        items = sidebarItems,
                                        activeId = active,
                                        onItemClick = { active = it },
                                    )
                                }
                            }
                            ShadcnSidebarInset {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    ShadcnSidebarTrigger(icon = { DocIcon(Bars3) })
                                    ShadcnBreadcrumb {
                                        ShadcnBreadcrumbLink("Building Your App", onClick = {})
                                        ShadcnBreadcrumbSeparator()
                                        ShadcnBreadcrumbPage("Data Fetching")
                                    }
                                }
                                ShadcnCard(
                                    size = CardSize.Sm,
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                ) {
                                    ShadcnText("Page content goes here.")
                                }
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        var expanded by remember { mutableStateOf(true) }
                        var active by remember { mutableStateOf("home") }
                        ShadcnSidebarProvider(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            modifier = Modifier.height(260.dp),
                        ) {
                            ShadcnSidebar {
                                ShadcnSidebarGroup(label = "Application") {
                                    ShadcnSidebarMenu(
                                        items = sidebarItems,
                                        activeId = active,
                                        onItemClick = { active = it },
                                    )
                                }
                            }
                            ShadcnSidebarInset {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    ShadcnSidebarTrigger(icon = { DocIcon(Bars3) })
                                    ShadcnBreadcrumb {
                                        ShadcnBreadcrumbLink("Building Your App", onClick = {})
                                        ShadcnBreadcrumbSeparator()
                                        ShadcnBreadcrumbPage("Data Fetching")
                                    }
                                }
                                ShadcnCard(
                                    size = CardSize.Sm,
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                ) {
                                    ShadcnText("Page content goes here.")
                                }
                            }
                        }
                    },
                ),
            ),
    )
