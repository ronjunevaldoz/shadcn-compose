@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTooltip

val tooltipDoc =
    ComponentDoc(
        id = "tooltip",
        title = "Tooltip",
        description = "A small hover-triggered label for an interactive element.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTooltip

            ShadcnTooltip(text = "Add to library") {
                ShadcnButton(onClick = {}) { ShadcnText("Hover me") }
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        ShadcnTooltip(text = "Add to library") {
                            ShadcnButton(onClick = {}) { ShadcnText("Hover me") }
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnTooltip(text = "Add to library") {
                            ShadcnButton(onClick = {}) { ShadcnText("Hover me") }
                        }
                    },
                ),
            ),
    )
