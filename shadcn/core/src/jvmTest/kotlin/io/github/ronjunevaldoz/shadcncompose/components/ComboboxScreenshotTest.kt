@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

/**
 * Idle (closed) trigger states, plus one open-panel capture for grouping -- everything
 * else about the open panel (search field, row/checkmark rendering, group heading +
 * separator) reuses the same visuals already proven by [DropdownMenuScreenshotTest] and
 * [CommandScreenshotTest].
 */
class ComboboxScreenshotTest : ShadcnScreenshotTest() {
    private fun idle(darkTheme: Boolean) {
        snapshot("combobox_idle", darkTheme = darkTheme) {
            ShadcnCombobox(
                value = null,
                options = listOf("Next.js", "SvelteKit", "Nuxt.js"),
                onValueChange = {},
            )
        }
    }

    @Test fun idle_light() = idle(darkTheme = false)

    @Test fun idle_dark() = idle(darkTheme = true)

    // Real shadcn/ui's current `combobox.tsx` (Base UI) `showClear` prop.
    private fun clearable(darkTheme: Boolean) {
        snapshot("combobox_clearable", darkTheme = darkTheme) {
            ShadcnCombobox(
                value = "Next.js",
                options = listOf("Next.js", "SvelteKit", "Nuxt.js"),
                onValueChange = {},
                onClear = {},
            )
        }
    }

    @Test fun clearable_light() = clearable(darkTheme = false)

    @Test fun clearable_dark() = clearable(darkTheme = true)

    // Real shadcn/ui's `multiple` prop -- empty state still shows the placeholder.
    private fun multiEmpty(darkTheme: Boolean) {
        snapshot("combobox_multi_empty", darkTheme = darkTheme) {
            ShadcnCombobox(
                values = emptyList(),
                options = listOf("Next.js", "SvelteKit", "Nuxt.js"),
                onValuesChange = {},
            )
        }
    }

    @Test fun multi_empty_light() = multiEmpty(darkTheme = false)

    @Test fun multi_empty_dark() = multiEmpty(darkTheme = true)

    // Real shadcn/ui's `ComboboxChips` -- picked options render as removable chips, plus
    // the clear button once there's a selection to clear.
    private fun multiSelected(darkTheme: Boolean) {
        snapshot("combobox_multi_selected", darkTheme = darkTheme) {
            ShadcnCombobox(
                values = listOf("Next.js", "SvelteKit"),
                options = listOf("Next.js", "SvelteKit", "Nuxt.js"),
                onValuesChange = {},
                onClear = {},
            )
        }
    }

    @Test fun multi_selected_light() = multiSelected(darkTheme = false)

    @Test fun multi_selected_dark() = multiSelected(darkTheme = true)

    // Real shadcn/ui's `ComboboxGroup`/`ComboboxLabel` -- options bucketed by `groupOf`.
    private fun grouped(darkTheme: Boolean) {
        setThemedContent(darkTheme = darkTheme) {
            ShadcnCombobox(
                value = null,
                options = listOf("Next.js", "SvelteKit", "Nuxt.js", "React", "Vue"),
                onValueChange = {},
                groupOf = { framework ->
                    if (framework in setOf("Next.js", "Nuxt.js")) "Meta-frameworks" else "Libraries"
                },
            )
        }
        composeRule.onNodeWithText("Select...").performClick()
        composeRule.waitForIdle()
        captureNamed("combobox_grouped_open", darkTheme = darkTheme)
    }

    @Test fun grouped_light() = grouped(darkTheme = false)

    @Test fun grouped_dark() = grouped(darkTheme = true)
}
