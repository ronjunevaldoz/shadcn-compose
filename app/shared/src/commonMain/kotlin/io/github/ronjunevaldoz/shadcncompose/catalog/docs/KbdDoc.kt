package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import io.github.ronjunevaldoz.shadcncompose.components.ShadcnKbd
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnKbdGroup

val kbdDoc =
    ComponentDoc(
        id = "kbd",
        title = "Kbd",
        description = "A keyboard-shortcut label.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnKbd

            ShadcnKbd("⌘K")
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Group",
                    code =
                        """
                        ShadcnKbdGroup {
                            ShadcnKbd("Ctrl")
                            ShadcnKbd("K")
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnKbdGroup {
                            ShadcnKbd("Ctrl")
                            ShadcnKbd("K")
                        }
                    },
                ),
                ComponentExample(
                    title = "Single",
                    code = "ShadcnKbd(\"⌘K\")",
                    preview = { ShadcnKbd("⌘K") },
                ),
            ),
    )
