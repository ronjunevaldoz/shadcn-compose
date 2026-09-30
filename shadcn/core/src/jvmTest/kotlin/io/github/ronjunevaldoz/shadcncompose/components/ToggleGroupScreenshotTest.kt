package io.github.ronjunevaldoz.shadcncompose.components

import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnStylePreset
import io.github.ronjunevaldoz.shadcncompose.styles.ToggleVariant
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class ToggleGroupScreenshotTest : ShadcnScreenshotTest() {
    private val items =
        listOf(
            ToggleGroupItem("bold", "B"),
            ToggleGroupItem("italic", "I"),
            ToggleGroupItem("underline", "U"),
        )

    // default: items apart (shadcn's spacing={2}), the selected one filled
    private fun states(darkTheme: Boolean) {
        snapshot("toggle_group_states", darkTheme = darkTheme) {
            ShadcnToggleGroup(items = items, selected = setOf("bold"), onSelectedChange = {})
        }
    }

    @Test fun states_light() = states(darkTheme = false)

    @Test fun states_dark() = states(darkTheme = true)

    // spacing = 0.dp: one segmented control, only the outer ends rounded; Outline shares one border
    @Test
    fun joined_light() =
        snapshot("toggle_group_joined") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ShadcnToggleGroup(items = items, selected = setOf("bold"), onSelectedChange = {}, spacing = 0.dp)
                ShadcnToggleGroup(
                    items = items,
                    selected = setOf("italic"),
                    onSelectedChange = {},
                    variant = ToggleVariant.Outline,
                    spacing = 0.dp,
                )
            }
        }

    @Test
    fun outline_light() =
        snapshot("toggle_group_outline") {
            ShadcnToggleGroup(
                items = items,
                selected = setOf("bold"),
                onSelectedChange = {},
                variant = ToggleVariant.Outline,
            )
        }

    // Maia's pill toggles, as the GoLearn CMS uses them
    @Test
    fun maia_light() =
        snapshot("toggle_group_maia", stylePreset = ShadcnStylePreset.Maia) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ShadcnToggleGroup(items = items, selected = setOf("bold"), onSelectedChange = {})
                ShadcnToggleGroup(items = items, selected = setOf("bold"), onSelectedChange = {}, spacing = 0.dp)
            }
        }
}
