@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant

val buttonDoc =
    ComponentDoc(
        id = "button",
        title = "Button",
        description = "Triggers an action or event. Supports six variants and five sizes.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText

            ShadcnButton(onClick = { /* ... */ }) {
                ShadcnText("Continue")
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        ShadcnButton(onClick = {}) {
                            ShadcnText("Default")
                        }
                        """.trimIndent(),
                    preview = { ShadcnButton(onClick = {}) { ShadcnText("Default") } },
                ),
                ComponentExample(
                    title = "Outline",
                    code =
                        """
                        ShadcnButton(onClick = {}, variant = ButtonVariant.Outline) {
                            ShadcnText("Outline")
                        }
                        """.trimIndent(),
                    preview = { ShadcnButton(onClick = {}, variant = ButtonVariant.Outline) { ShadcnText("Outline") } },
                ),
                ComponentExample(
                    title = "Secondary",
                    code =
                        """
                        ShadcnButton(onClick = {}, variant = ButtonVariant.Secondary) {
                            ShadcnText("Secondary")
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnButton(
                            onClick = {},
                            variant = ButtonVariant.Secondary,
                        ) { ShadcnText("Secondary") }
                    },
                ),
                ComponentExample(
                    title = "Ghost",
                    code =
                        """
                        ShadcnButton(onClick = {}, variant = ButtonVariant.Ghost) {
                            ShadcnText("Ghost")
                        }
                        """.trimIndent(),
                    preview = { ShadcnButton(onClick = {}, variant = ButtonVariant.Ghost) { ShadcnText("Ghost") } },
                ),
                ComponentExample(
                    title = "Destructive",
                    code =
                        """
                        ShadcnButton(onClick = {}, variant = ButtonVariant.Destructive) {
                            ShadcnText("Destructive")
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnButton(onClick = {}, variant = ButtonVariant.Destructive) { ShadcnText("Destructive") }
                    },
                ),
                ComponentExample(
                    title = "Disabled",
                    code =
                        """
                        ShadcnButton(onClick = {}, enabled = false) {
                            ShadcnText("Disabled")
                        }
                        """.trimIndent(),
                    preview = { ShadcnButton(onClick = {}, enabled = false) { ShadcnText("Disabled") } },
                ),
            ),
    )
