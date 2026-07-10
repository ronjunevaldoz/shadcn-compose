package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAccordion
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAccordionItem
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.heroicons.outline.ChevronDown

val accordionDoc =
    ComponentDoc(
        id = "accordion",
        title = "Accordion",
        description = "A vertically-stacked set of collapsible sections.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAccordion
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAccordionItem

            var expanded by remember { mutableStateOf(setOf("item-1")) }
            ShadcnAccordion(
                items = listOf(
                    ShadcnAccordionItem("item-1", "Is it accessible?") {
                        ShadcnText("Yes. It adheres to the WAI-ARIA design pattern.")
                    },
                    ShadcnAccordionItem("item-2", "Is it styled?") {
                        ShadcnText("Yes. It comes with default styles.")
                    },
                ),
                expandedIds = expanded,
                onExpandedIdsChange = { expanded = it },
            )
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        var expanded by remember { mutableStateOf(setOf("item-1")) }
                        ShadcnAccordion(
                            items = listOf(
                                ShadcnAccordionItem("item-1", "Is it accessible?") {
                                    ShadcnText("Yes. It adheres to the WAI-ARIA design pattern.")
                                },
                                ShadcnAccordionItem("item-2", "Is it styled?") {
                                    ShadcnText("Yes. It comes with default styles.")
                                },
                                ShadcnAccordionItem("item-3", "Is it animated?") {
                                    ShadcnText("Yes. It's animated by default.")
                                },
                            ),
                            expandedIds = expanded,
                            onExpandedIdsChange = { expanded = it },
                        )
                        """.trimIndent(),
                    preview = {
                        var expanded by remember { mutableStateOf(setOf("item-1")) }
                        ShadcnAccordion(
                            items =
                                listOf(
                                    ShadcnAccordionItem("item-1", "Is it accessible?") {
                                        ShadcnText("Yes. It adheres to the WAI-ARIA design pattern.")
                                    },
                                    ShadcnAccordionItem("item-2", "Is it styled?") {
                                        ShadcnText("Yes. It comes with default styles.")
                                    },
                                    ShadcnAccordionItem("item-3", "Is it animated?") {
                                        ShadcnText("Yes. It's animated by default.")
                                    },
                                ),
                            expandedIds = expanded,
                            onExpandedIdsChange = { expanded = it },
                        )
                    },
                ),
                ComponentExample(
                    title = "Custom icon",
                    code =
                        """
                        // ShadcnAccordion's chevron is a plain glyph by default (this library
                        // takes no icon-library dependency) -- override the icon slot with a
                        // real vector from any icon set, e.g. heroicons-outline. The slot
                        // receives isOpen so the override can drive its own rotation too.
                        var expanded by remember { mutableStateOf(setOf("item-1")) }
                        ShadcnAccordion(
                            items = listOf(
                                ShadcnAccordionItem("item-1", "Is it accessible?") {
                                    ShadcnText("Yes. It adheres to the WAI-ARIA design pattern.")
                                },
                                ShadcnAccordionItem("item-2", "Is it styled?") {
                                    ShadcnText("Yes. It comes with default styles.")
                                },
                            ),
                            expandedIds = expanded,
                            onExpandedIdsChange = { expanded = it },
                            icon = { isOpen ->
                                val rotation by animateFloatAsState(if (isOpen) 180f else 0f)
                                DocIcon(ChevronDown, modifier = Modifier.rotate(rotation))
                            },
                        )
                        """.trimIndent(),
                    preview = {
                        var expanded by remember { mutableStateOf(setOf("item-1")) }
                        ShadcnAccordion(
                            items =
                                listOf(
                                    ShadcnAccordionItem("item-1", "Is it accessible?") {
                                        ShadcnText("Yes. It adheres to the WAI-ARIA design pattern.")
                                    },
                                    ShadcnAccordionItem("item-2", "Is it styled?") {
                                        ShadcnText("Yes. It comes with default styles.")
                                    },
                                ),
                            expandedIds = expanded,
                            onExpandedIdsChange = { expanded = it },
                            icon = { isOpen ->
                                val rotation by animateFloatAsState(if (isOpen) 180f else 0f)
                                DocIcon(ChevronDown, modifier = Modifier.rotate(rotation))
                            },
                        )
                    },
                ),
            ),
    )
