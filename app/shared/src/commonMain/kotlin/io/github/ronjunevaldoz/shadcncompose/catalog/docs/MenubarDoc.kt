package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDropdownMenuItem
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnDropdownMenuSeparator
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMenubar
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMenubarMenu

val menubarDoc =
    ComponentDoc(
        id = "menubar",
        title = "Menubar",
        description = "A horizontal row of menu triggers, desktop-app style.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMenubar
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMenubarMenu

            ShadcnMenubar(
                menus = listOf(
                    ShadcnMenubarMenu("File") {
                        ShadcnDropdownMenuItem("New Tab", onClick = {})
                        ShadcnDropdownMenuSeparator()
                        ShadcnDropdownMenuItem("Close Window", onClick = {}, destructive = true)
                    },
                    ShadcnMenubarMenu("Edit") { ShadcnDropdownMenuItem("Undo", onClick = {}) },
                ),
            )
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        ShadcnMenubar(
                            menus = listOf(
                                ShadcnMenubarMenu("File") {
                                    ShadcnDropdownMenuItem("New Tab", onClick = {})
                                    ShadcnDropdownMenuItem("New Window", onClick = {})
                                    ShadcnDropdownMenuSeparator()
                                    ShadcnDropdownMenuItem("Close Window", onClick = {}, destructive = true)
                                },
                                ShadcnMenubarMenu("Edit") {
                                    ShadcnDropdownMenuItem("Undo", onClick = {})
                                    ShadcnDropdownMenuItem("Redo", onClick = {})
                                },
                                ShadcnMenubarMenu("View") {
                                    ShadcnDropdownMenuItem("Zoom In", onClick = {})
                                    ShadcnDropdownMenuItem("Zoom Out", onClick = {})
                                },
                            ),
                        )
                        """.trimIndent(),
                    preview = {
                        ShadcnMenubar(
                            menus =
                                listOf(
                                    ShadcnMenubarMenu("File") {
                                        ShadcnDropdownMenuItem("New Tab", onClick = {})
                                        ShadcnDropdownMenuItem("New Window", onClick = {})
                                        ShadcnDropdownMenuSeparator()
                                        ShadcnDropdownMenuItem("Close Window", onClick = {}, destructive = true)
                                    },
                                    ShadcnMenubarMenu("Edit") {
                                        ShadcnDropdownMenuItem("Undo", onClick = {})
                                        ShadcnDropdownMenuItem("Redo", onClick = {})
                                    },
                                    ShadcnMenubarMenu("View") {
                                        ShadcnDropdownMenuItem("Zoom In", onClick = {})
                                        ShadcnDropdownMenuItem("Zoom Out", onClick = {})
                                    },
                                ),
                        )
                    },
                ),
            ),
    )
