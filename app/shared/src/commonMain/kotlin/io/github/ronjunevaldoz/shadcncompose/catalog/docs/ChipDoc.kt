@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnChip
import io.github.ronjunevaldoz.shadcncompose.styles.ChipVariant

val chipDoc =
    ComponentDoc(
        id = "chip",
        title = "Chip",
        description = "A selectable tag, commonly used for filters or multi-select choices.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnChip

            var selected by remember { mutableStateOf(false) }
            ShadcnChip(label = "Kotlin", selected = selected, onClick = { selected = !selected })
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Selectable",
                    code =
                        """
                        var selected by remember { mutableStateOf(false) }
                        ShadcnChip(label = "Kotlin", selected = selected, onClick = { selected = !selected })
                        """.trimIndent(),
                    preview = {
                        var selected by remember { mutableStateOf(false) }
                        ShadcnChip(label = "Kotlin", selected = selected, onClick = { selected = !selected })
                    },
                ),
                ComponentExample(
                    title = "Outline",
                    code = """ShadcnChip(label = "Outline", variant = ChipVariant.Outline, onClick = {})""",
                    preview = { ShadcnChip(label = "Outline", variant = ChipVariant.Outline, onClick = {}) },
                ),
                ComponentExample(
                    title = "Disabled",
                    code = """ShadcnChip(label = "Disabled", onClick = {}, enabled = false)""",
                    preview = { ShadcnChip(label = "Disabled", onClick = {}, enabled = false) },
                ),
            ),
    )
