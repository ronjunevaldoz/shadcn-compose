@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnToggleGroup
import io.github.ronjunevaldoz.shadcncompose.components.ToggleGroupItem
import io.github.ronjunevaldoz.shadcncompose.styles.ToggleVariant

private val demoItems =
    listOf(
        ToggleGroupItem("bold", "B"),
        ToggleGroupItem("italic", "I"),
        ToggleGroupItem("underline", "U"),
    )

val toggleGroupDoc =
    ComponentDoc(
        id = "toggle-group",
        title = "Toggle Group",
        description = "A segmented row of toggles for single- or multi-select, e.g. text formatting.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnToggleGroup
            import io.github.ronjunevaldoz.shadcncompose.components.ToggleGroupItem

            var selected by remember { mutableStateOf(setOf("bold")) }
            ShadcnToggleGroup(
                items = listOf(ToggleGroupItem("bold", "B"), ToggleGroupItem("italic", "I")),
                selected = selected,
                onSelectedChange = { value -> selected = if (value in selected) selected - value else selected + value },
            )
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        var selected by remember { mutableStateOf(setOf("bold")) }
                        ShadcnToggleGroup(
                            items = demoItems,
                            selected = selected,
                            onSelectedChange = { value -> selected = if (value in selected) selected - value else selected + value },
                        )
                        """.trimIndent(),
                    preview = {
                        var selected by remember { mutableStateOf(setOf("bold")) }
                        ShadcnToggleGroup(
                            items = demoItems,
                            selected = selected,
                            onSelectedChange = {
                                    value ->
                                selected = if (value in selected) selected - value else selected + value
                            },
                        )
                    },
                ),
                ComponentExample(
                    title = "Outline",
                    code =
                        """
                        var selected by remember { mutableStateOf(setOf("italic")) }
                        ShadcnToggleGroup(
                            items = demoItems,
                            selected = selected,
                            onSelectedChange = { value -> selected = if (value in selected) selected - value else selected + value },
                            variant = ToggleVariant.Outline,
                        )
                        """.trimIndent(),
                    preview = {
                        var selected by remember { mutableStateOf(setOf("italic")) }
                        ShadcnToggleGroup(
                            items = demoItems,
                            selected = selected,
                            onSelectedChange = {
                                    value ->
                                selected = if (value in selected) selected - value else selected + value
                            },
                            variant = ToggleVariant.Outline,
                        )
                    },
                ),
            ),
    )
