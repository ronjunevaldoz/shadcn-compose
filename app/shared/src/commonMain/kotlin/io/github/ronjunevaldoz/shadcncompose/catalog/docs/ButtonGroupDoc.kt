@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButtonGroup
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButtonGroupSeparator
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButtonGroupText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant

val buttonGroupDoc =
    ComponentDoc(
        id = "button-group",
        title = "Button Group",
        description = "Visually joins a row of buttons into a single segmented control.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButtonGroup
            import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant

            ShadcnButtonGroup {
                ShadcnButton(onClick = {}, variant = ButtonVariant.Ghost) { ShadcnText("Copy") }
                ShadcnButtonGroupSeparator()
                ShadcnButton(onClick = {}, variant = ButtonVariant.Ghost) { ShadcnText("Share") }
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        ShadcnButtonGroup {
                            ShadcnButton(onClick = {}, variant = ButtonVariant.Ghost) { ShadcnText("Copy") }
                            ShadcnButtonGroupSeparator()
                            ShadcnButton(onClick = {}, variant = ButtonVariant.Ghost) { ShadcnText("Share") }
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnButtonGroup {
                            ShadcnButton(onClick = {}, variant = ButtonVariant.Ghost) { ShadcnText("Copy") }
                            ShadcnButtonGroupSeparator()
                            ShadcnButton(onClick = {}, variant = ButtonVariant.Ghost) { ShadcnText("Share") }
                        }
                    },
                ),
                ComponentExample(
                    title = "With label",
                    code =
                        """
                        ShadcnButtonGroup {
                            ShadcnButtonGroupText("https://")
                            ShadcnButtonGroupSeparator()
                            ShadcnButton(onClick = {}, variant = ButtonVariant.Ghost) { ShadcnText("example.com") }
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnButtonGroup {
                            ShadcnButtonGroupText("https://")
                            ShadcnButtonGroupSeparator()
                            ShadcnButton(onClick = {}, variant = ButtonVariant.Ghost) { ShadcnText("example.com") }
                        }
                    },
                ),
            ),
    )
