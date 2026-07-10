@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSelect
import io.github.ronjunevaldoz.heroicons.outline.ChevronDown

val selectDoc =
    ComponentDoc(
        id = "select",
        title = "Select",
        description =
            "A plain (non-searchable) dropdown select. Unlike Combobox, the option list isn't filterable by typing.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSelect

            var theme by remember { mutableStateOf<String?>(null) }
            ShadcnSelect(
                value = theme,
                options = listOf("Light", "Dark", "System"),
                onValueChange = { theme = it },
                placeholder = "Theme",
            )
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        var theme by remember { mutableStateOf<String?>(null) }
                        ShadcnSelect(
                            value = theme,
                            options = listOf("Light", "Dark", "System"),
                            onValueChange = { theme = it },
                            placeholder = "Theme",
                        )
                        """.trimIndent(),
                    preview = {
                        var theme by remember { mutableStateOf<String?>(null) }
                        ShadcnSelect(
                            value = theme,
                            options = listOf("Light", "Dark", "System"),
                            onValueChange = { theme = it },
                            placeholder = "Theme",
                        )
                    },
                ),
                ComponentExample(
                    title = "Custom icon",
                    code =
                        """
                        // ShadcnSelect's trigger chevron is a plain glyph by default (this
                        // library takes no icon-library dependency) -- override the icon slot
                        // with a real vector from any icon set, e.g. heroicons-outline.
                        var theme by remember { mutableStateOf<String?>(null) }
                        ShadcnSelect(
                            value = theme,
                            options = listOf("Light", "Dark", "System"),
                            onValueChange = { theme = it },
                            placeholder = "Theme",
                            icon = { DocIcon(ChevronDown) },
                        )
                        """.trimIndent(),
                    preview = {
                        var theme by remember { mutableStateOf<String?>(null) }
                        ShadcnSelect(
                            value = theme,
                            options = listOf("Light", "Dark", "System"),
                            onValueChange = { theme = it },
                            placeholder = "Theme",
                            icon = { DocIcon(ChevronDown) },
                        )
                    },
                ),
            ),
    )
