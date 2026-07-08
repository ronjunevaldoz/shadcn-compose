package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCommand
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCommandGroup
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCommandItem

val commandDoc =
    ComponentDoc(
        id = "command",
        title = "Command",
        description = "A searchable/filterable action list -- the building block behind a command palette.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCommand
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCommandGroup
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCommandItem

            ShadcnCommand(
                groups = listOf(
                    ShadcnCommandGroup(
                        heading = "Suggestions",
                        items = listOf(
                            ShadcnCommandItem("calendar", "Calendar", onSelect = {}),
                            ShadcnCommandItem("emoji", "Search Emoji", onSelect = {}),
                        ),
                    ),
                    ShadcnCommandGroup(
                        heading = "Settings",
                        items = listOf(ShadcnCommandItem("profile", "Profile", onSelect = {})),
                    ),
                ),
            )
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        ShadcnCommand(
                            groups = listOf(
                                ShadcnCommandGroup(
                                    heading = "Suggestions",
                                    items = listOf(
                                        ShadcnCommandItem("calendar", "Calendar", onSelect = {}),
                                        ShadcnCommandItem("emoji", "Search Emoji", onSelect = {}),
                                        ShadcnCommandItem("calculator", "Calculator", onSelect = {}),
                                    ),
                                ),
                                ShadcnCommandGroup(
                                    heading = "Settings",
                                    items = listOf(
                                        ShadcnCommandItem("profile", "Profile", onSelect = {}),
                                        ShadcnCommandItem("billing", "Billing", onSelect = {}),
                                    ),
                                ),
                            ),
                        )
                        """.trimIndent(),
                    preview = {
                        ShadcnCommand(
                            groups =
                                listOf(
                                    ShadcnCommandGroup(
                                        heading = "Suggestions",
                                        items =
                                            listOf(
                                                ShadcnCommandItem("calendar", "Calendar", onSelect = {}),
                                                ShadcnCommandItem("emoji", "Search Emoji", onSelect = {}),
                                                ShadcnCommandItem("calculator", "Calculator", onSelect = {}),
                                            ),
                                    ),
                                    ShadcnCommandGroup(
                                        heading = "Settings",
                                        items =
                                            listOf(
                                                ShadcnCommandItem("profile", "Profile", onSelect = {}),
                                                ShadcnCommandItem("billing", "Billing", onSelect = {}),
                                            ),
                                    ),
                                ),
                        )
                    },
                ),
            ),
    )
