@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCard
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCardHeader
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.styles.CardVariant

val cardDoc =
    ComponentDoc(
        id = "card",
        title = "Card",
        description = "Displays content in a bordered container, with optional header and footer slots.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCard
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCardHeader

            ShadcnCard(
                header = { ShadcnCardHeader(title = "Account", description = "Manage your settings") },
            ) {
                ShadcnText("Card body content")
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        ShadcnCard(
                            header = { ShadcnCardHeader(title = "Account", description = "Manage your settings") },
                            footer = { ShadcnButton(onClick = {}) { ShadcnText("Save") } },
                        ) {
                            ShadcnText("Card body content")
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnCard(
                            header = { ShadcnCardHeader(title = "Account", description = "Manage your settings") },
                            footer = { ShadcnButton(onClick = {}) { ShadcnText("Save") } },
                        ) {
                            ShadcnText("Card body content")
                        }
                    },
                ),
                ComponentExample(
                    title = "Filled",
                    code =
                        """
                        ShadcnCard(variant = CardVariant.Filled) {
                            ShadcnText("Filled card")
                        }
                        """.trimIndent(),
                    preview = { ShadcnCard(variant = CardVariant.Filled) { ShadcnText("Filled card") } },
                ),
            ),
    )
