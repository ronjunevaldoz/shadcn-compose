package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnNavigationMenu
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnNavigationMenuItem
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

private data class NavLink(val title: String, val description: String)

private val gettingStartedLinks =
    listOf(
        NavLink("Introduction", "Re-usable components built with Radix UI and Tailwind CSS."),
        NavLink("Installation", "How to install dependencies and structure your app."),
        NavLink("Typography", "Styles for headings, paragraphs, lists, and more."),
    )

@Composable
private fun NavMenuPanel(links: List<NavLink>) {
    Column(verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.md)) {
        links.forEach { link ->
            Column(verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xs)) {
                ShadcnText(link.title, style = ShadcnTextStyle.LabelSmall)
                ShadcnText(link.description, style = ShadcnTextStyle.BodySmall, muted = true)
            }
        }
    }
}

val navigationMenuDoc =
    ComponentDoc(
        id = "navigation-menu",
        title = "Navigation Menu",
        description = "A horizontal top-level site navigation row where some items open a panel of related links.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnNavigationMenu
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnNavigationMenuItem

            ShadcnNavigationMenu(
                items = listOf(
                    ShadcnNavigationMenuItem("Home", onClick = {}),
                    ShadcnNavigationMenuItem("Getting started", panel = { /* titled list of links */ }),
                ),
            )
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        ShadcnNavigationMenu(
                            items = listOf(
                                ShadcnNavigationMenuItem("Home", onClick = {}),
                                ShadcnNavigationMenuItem(
                                    "Getting started",
                                    panel = {
                                        Column {
                                            // one titled list item per link, e.g.:
                                            ShadcnText("Introduction", style = ShadcnTextStyle.LabelSmall)
                                            ShadcnText(
                                                "Re-usable components built with Radix UI and Tailwind CSS.",
                                                style = ShadcnTextStyle.BodySmall,
                                                muted = true,
                                            )
                                            // ...Installation, Typography
                                        }
                                    },
                                ),
                                ShadcnNavigationMenuItem("Docs", onClick = {}),
                            ),
                        )
                        """.trimIndent(),
                    preview = {
                        ShadcnNavigationMenu(
                            items =
                                listOf(
                                    ShadcnNavigationMenuItem("Home", onClick = {}),
                                    ShadcnNavigationMenuItem(
                                        "Getting started",
                                        panel = { NavMenuPanel(gettingStartedLinks) },
                                    ),
                                    ShadcnNavigationMenuItem("Docs", onClick = {}),
                                ),
                        )
                    },
                ),
            ),
    )
