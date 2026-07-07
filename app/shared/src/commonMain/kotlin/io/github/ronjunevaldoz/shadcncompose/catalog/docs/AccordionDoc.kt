package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAccordion
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAccordionItem
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText

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
            ),
    )
