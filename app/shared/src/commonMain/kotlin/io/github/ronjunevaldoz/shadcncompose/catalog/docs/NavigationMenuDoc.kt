package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import io.github.ronjunevaldoz.shadcncompose.components.ShadcnNavigationMenu
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnNavigationMenuItem
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText

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
                    ShadcnNavigationMenuItem("Getting started", panel = { ShadcnText("Docs links here") }),
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
                                    panel = { ShadcnText("Re-usable components built with Radix UI.") },
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
                                        panel = { ShadcnText("Re-usable components built with Radix UI.") },
                                    ),
                                    ShadcnNavigationMenuItem("Docs", onClick = {}),
                                ),
                        )
                    },
                ),
            ),
    )
