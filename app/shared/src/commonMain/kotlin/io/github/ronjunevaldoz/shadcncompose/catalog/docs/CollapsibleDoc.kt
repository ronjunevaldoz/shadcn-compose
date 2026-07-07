@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCollapsible
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant

val collapsibleDoc =
    ComponentDoc(
        id = "collapsible",
        title = "Collapsible",
        description = "An expand/collapse container for showing or hiding content.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCollapsible

            var expanded by remember { mutableStateOf(false) }
            ShadcnCollapsible(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                trigger = { isOpen, toggle ->
                    ShadcnButton(onClick = toggle, variant = ButtonVariant.Outline) {
                        ShadcnText(if (isOpen) "Hide details" else "Show details")
                    }
                },
            ) {
                ShadcnText("@peduarte starred 3 repositories")
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        var expanded by remember { mutableStateOf(false) }
                        ShadcnCollapsible(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            trigger = { isOpen, toggle ->
                                ShadcnButton(onClick = toggle, variant = ButtonVariant.Outline) {
                                    ShadcnText(if (isOpen) "Hide details" else "Show details")
                                }
                            },
                        ) {
                            ShadcnText("@peduarte starred 3 repositories")
                        }
                        """.trimIndent(),
                    preview = {
                        var expanded by remember { mutableStateOf(false) }
                        ShadcnCollapsible(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            trigger = { isOpen, toggle ->
                                ShadcnButton(onClick = toggle, variant = ButtonVariant.Outline) {
                                    ShadcnText(if (isOpen) "Hide details" else "Show details")
                                }
                            },
                        ) {
                            ShadcnText("@peduarte starred 3 repositories")
                        }
                    },
                ),
            ),
    )
