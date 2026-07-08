package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
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

    @Test fun expanded_light() = states(darkTheme = false, expanded = true)

    @Test fun expanded_dark() = states(darkTheme = true, expanded = true)

    @Test fun collapsed_light() = states(darkTheme = false, expanded = false)

    @Test fun collapsed_dark() = states(darkTheme = true, expanded = false)
}
