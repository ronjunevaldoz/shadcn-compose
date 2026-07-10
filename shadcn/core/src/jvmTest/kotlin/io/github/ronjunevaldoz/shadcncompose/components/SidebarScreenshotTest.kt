@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.styles.CardSize
import kotlin.test.Test

class SidebarScreenshotTest : ShadcnScreenshotTest() {
    private val items =
        listOf(
            ShadcnSidebarMenuItem("home", "Home"),
            ShadcnSidebarMenuItem("inbox", "Inbox"),
            ShadcnSidebarMenuItem("settings", "Settings"),
        )

    private fun states(
        darkTheme: Boolean,
        expanded: Boolean,
    ) {
        val suffix = if (expanded) "_expanded" else "_collapsed"
        snapshot("sidebar$suffix", darkTheme = darkTheme) {
            ShadcnSidebarProvider(expanded = expanded, onExpandedChange = {}, modifier = Modifier.height(200.dp)) {
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
        }
    }

    private fun appShell(darkTheme: Boolean) {
        snapshot("sidebar_app_shell", darkTheme = darkTheme) {
            ShadcnSidebarProvider(expanded = true, onExpandedChange = {}, modifier = Modifier.height(260.dp)) {
                ShadcnSidebar {
                    ShadcnSidebarGroup(label = "Application") {
                        ShadcnSidebarMenu(items = items, activeId = "home", onItemClick = {})
                    }
                }
                ShadcnSidebarInset {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ShadcnSidebarTrigger()
                        ShadcnBreadcrumb {
                            ShadcnBreadcrumbLink("Building Your App", onClick = {})
                            ShadcnBreadcrumbSeparator()
                            ShadcnBreadcrumbPage("Data Fetching")
                        }
                    }
                    ShadcnCard(size = CardSize.Sm, modifier = Modifier.padding(horizontal = 8.dp)) {
                        ShadcnText("Page content goes here.")
                    }
                }
            }
        }
    }

    @Test fun expanded_light() = states(darkTheme = false, expanded = true)

    @Test fun expanded_dark() = states(darkTheme = true, expanded = true)

    @Test fun collapsed_light() = states(darkTheme = false, expanded = false)

    @Test fun collapsed_dark() = states(darkTheme = true, expanded = false)

    @Test fun app_shell_light() = appShell(darkTheme = false)

    @Test fun app_shell_dark() = appShell(darkTheme = true)

    private fun triggerFocused(darkTheme: Boolean) {
        snapshotFocused("sidebar_trigger_focused", focusTag = "sidebar-trigger", darkTheme = darkTheme) {
            ShadcnSidebarProvider(expanded = true, onExpandedChange = {}, modifier = Modifier.height(200.dp)) {
                ShadcnSidebarInset {
                    ShadcnSidebarTrigger(modifier = Modifier.testTag("sidebar-trigger"))
                    ShadcnText("Main content")
                }
            }
        }
    }

    @Test fun trigger_focused_light() = triggerFocused(darkTheme = false)
}
