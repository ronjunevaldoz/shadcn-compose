@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAlert
import io.github.ronjunevaldoz.shadcncompose.styles.AlertVariant

val alertDoc =
    ComponentDoc(
        id = "alert",
        title = "Alert",
        description = "A short, prominent callout for important information.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAlert

            ShadcnAlert(title = "Heads up!", description = "You can add components to your app.")
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code = "ShadcnAlert(title = \"Heads up!\", description = \"You can add components to your app.\")",
                    preview = {
                        ShadcnAlert(title = "Heads up!", description = "You can add components to your app.")
                    },
                ),
                ComponentExample(
                    title = "Destructive",
                    code =
                        """
                        ShadcnAlert(
                            variant = AlertVariant.Destructive,
                            title = "Error",
                            description = "Your session has expired. Please log in again.",
                        )
                        """.trimIndent(),
                    preview = {
                        ShadcnAlert(
                            variant = AlertVariant.Destructive,
                            title = "Error",
                            description = "Your session has expired. Please log in again.",
                        )
                    },
                ),
            ),
    )
