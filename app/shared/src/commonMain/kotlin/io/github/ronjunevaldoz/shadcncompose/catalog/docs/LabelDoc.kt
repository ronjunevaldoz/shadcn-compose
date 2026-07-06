package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import io.github.ronjunevaldoz.shadcncompose.components.ShadcnLabel

val labelDoc =
    ComponentDoc(
        id = "label",
        title = "Label",
        description = "A form-field label, optionally marked required.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnLabel

            ShadcnLabel("Email", required = true)
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code = """ShadcnLabel("Email")""",
                    preview = { ShadcnLabel("Email") },
                ),
                ComponentExample(
                    title = "Required",
                    code = """ShadcnLabel("Email", required = true)""",
                    preview = { ShadcnLabel("Email", required = true) },
                ),
                ComponentExample(
                    title = "Disabled",
                    code = """ShadcnLabel("Email", disabled = true)""",
                    preview = { ShadcnLabel("Email", disabled = true) },
                ),
            ),
    )
