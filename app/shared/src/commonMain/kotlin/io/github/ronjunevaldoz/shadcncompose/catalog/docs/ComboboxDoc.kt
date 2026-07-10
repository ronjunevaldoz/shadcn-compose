package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCombobox
import io.github.ronjunevaldoz.heroicons.outline.ChevronDown

val comboboxDoc =
    ComponentDoc(
        id = "combobox",
        title = "Combobox",
        description = "A select with a filterable/searchable option list.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCombobox

            var framework by remember { mutableStateOf<String?>(null) }
            ShadcnCombobox(
                value = framework,
                options = listOf("Next.js", "SvelteKit", "Nuxt.js", "Remix", "Astro"),
                onValueChange = { framework = it },
            )
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        var framework by remember { mutableStateOf<String?>(null) }
                        ShadcnCombobox(
                            value = framework,
                            options = listOf("Next.js", "SvelteKit", "Nuxt.js", "Remix", "Astro"),
                            onValueChange = { framework = it },
                        )
                        """.trimIndent(),
                    preview = {
                        var framework by remember { mutableStateOf<String?>(null) }
                        ShadcnCombobox(
                            value = framework,
                            options = listOf("Next.js", "SvelteKit", "Nuxt.js", "Remix", "Astro"),
                            onValueChange = { framework = it },
                        )
                    },
                ),
                ComponentExample(
                    title = "Custom icon",
                    code =
                        """
                        // ShadcnCombobox's trigger chevron is a plain glyph by default (this
                        // library takes no icon-library dependency) -- override the icon slot
                        // with a real vector from any icon set, e.g. heroicons-outline.
                        var framework by remember { mutableStateOf<String?>(null) }
                        ShadcnCombobox(
                            value = framework,
                            options = listOf("Next.js", "SvelteKit", "Nuxt.js", "Remix", "Astro"),
                            onValueChange = { framework = it },
                            icon = { DocIcon(ChevronDown) },
                        )
                        """.trimIndent(),
                    preview = {
                        var framework by remember { mutableStateOf<String?>(null) }
                        ShadcnCombobox(
                            value = framework,
                            options = listOf("Next.js", "SvelteKit", "Nuxt.js", "Remix", "Astro"),
                            onValueChange = { framework = it },
                            icon = { DocIcon(ChevronDown) },
                        )
                    },
                ),
            ),
    )
