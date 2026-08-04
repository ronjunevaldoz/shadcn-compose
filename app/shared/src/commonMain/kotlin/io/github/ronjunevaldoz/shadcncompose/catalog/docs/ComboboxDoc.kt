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
        referenceUrl = "https://ui.shadcn.com/docs/components/base/combobox",
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
                ComponentExample(
                    title = "Clearable",
                    code =
                        """
                        // Real shadcn/ui's `showClear` prop -- opt in with onClear, which
                        // only renders once there's a value to clear.
                        var framework by remember { mutableStateOf<String?>("Next.js") }
                        ShadcnCombobox(
                            value = framework,
                            options = listOf("Next.js", "SvelteKit", "Nuxt.js", "Remix", "Astro"),
                            onValueChange = { framework = it },
                            onClear = { framework = null },
                        )
                        """.trimIndent(),
                    preview = {
                        var framework by remember { mutableStateOf<String?>("Next.js") }
                        ShadcnCombobox(
                            value = framework,
                            options = listOf("Next.js", "SvelteKit", "Nuxt.js", "Remix", "Astro"),
                            onValueChange = { framework = it },
                            onClear = { framework = null },
                        )
                    },
                ),
                ComponentExample(
                    title = "Multi-select",
                    code =
                        """
                        // Real shadcn/ui's `multiple` prop + `ComboboxChips` -- picked
                        // options render as removable chips in the trigger.
                        var frameworks by remember { mutableStateOf(listOf("Next.js")) }
                        ShadcnCombobox(
                            values = frameworks,
                            options = listOf("Next.js", "SvelteKit", "Nuxt.js", "Remix", "Astro"),
                            onValuesChange = { frameworks = it },
                            onClear = { frameworks = emptyList() },
                        )
                        """.trimIndent(),
                    preview = {
                        var frameworks by remember { mutableStateOf(listOf("Next.js")) }
                        ShadcnCombobox(
                            values = frameworks,
                            options = listOf("Next.js", "SvelteKit", "Nuxt.js", "Remix", "Astro"),
                            onValuesChange = { frameworks = it },
                            onClear = { frameworks = emptyList() },
                        )
                    },
                ),
                ComponentExample(
                    title = "Grouped",
                    code =
                        """
                        // Real shadcn/ui's `ComboboxGroup`/`ComboboxLabel` -- bucket a flat
                        // options list into headed groups with `groupOf`.
                        var framework by remember { mutableStateOf<String?>(null) }
                        ShadcnCombobox(
                            value = framework,
                            options = listOf("Next.js", "Nuxt.js", "SvelteKit", "Remix", "Astro"),
                            onValueChange = { framework = it },
                            groupOf = { if (it in setOf("Next.js", "Nuxt.js")) "Meta-frameworks" else "Other" },
                        )
                        """.trimIndent(),
                    preview = {
                        var framework by remember { mutableStateOf<String?>(null) }
                        ShadcnCombobox(
                            value = framework,
                            options = listOf("Next.js", "Nuxt.js", "SvelteKit", "Remix", "Astro"),
                            onValueChange = { framework = it },
                            groupOf = { if (it in setOf("Next.js", "Nuxt.js")) "Meta-frameworks" else "Other" },
                        )
                    },
                ),
            ),
    )
