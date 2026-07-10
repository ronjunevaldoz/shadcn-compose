@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import io.github.ronjunevaldoz.shadcncompose.styles.focusRingShadow
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnStylePreset
import kotlin.test.Test

/**
 * Documented, repeatable proof that [ShadcnStylePreset] actually drives per-style
 * shape/ring differences end to end -- this is the durable replacement for eyeballing
 * a live browser toggle, which can't be committed to git or diffed in a PR.
 *
 * Two kinds of capture per theme:
 * 1. `style_matrix_ring_swatch`: all 8 presets' ring token (width/offset/corner) drawn
 *    forced-on in one composite image, side by side -- each swatch renders inside its
 *    own `ShadcnTheme(preset = ..., isDark = ...)` subtree so `theme.focusRingShadow()`
 *    resolves that preset's own ring, not a shared one.
 * 2. `style_matrix_button_focused_<preset>`: the *real* `ShadcnButton`, focused via real
 *    `requestFocus()`, one capture per preset -- proves the wiring through the actual
 *    component, not just a swatch.
 */
class StylePresetMatrixTest : ShadcnScreenshotTest() {
    @Test
    fun ring_swatch_light() = ringSwatch(darkTheme = false)

    @Test
    fun ring_swatch_dark() = ringSwatch(darkTheme = true)

    @Test
    fun button_focused_light() = buttonFocusedPerPreset(darkTheme = false)

    @Test
    fun button_focused_dark() = buttonFocusedPerPreset(darkTheme = true)

    private fun ringSwatch(darkTheme: Boolean) {
        snapshot("style_matrix_ring_swatch", darkTheme = darkTheme) {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                ShadcnStylePreset.entries.forEach { preset -> PresetRingSwatch(preset, darkTheme) }
            }
        }
    }

    /** One composition holding all 8 presets' own real, independently-themed [ShadcnButton]s. */
    private fun buttonFocusedPerPreset(darkTheme: Boolean) {
        composeRule.setContent {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                ShadcnStylePreset.entries.forEach { preset ->
                    ThemedPresetBox(preset, darkTheme) {
                        ShadcnButton(
                            onClick = {},
                            modifier = Modifier.testTag("style_matrix_btn_${preset.name}"),
                        ) { ShadcnText("Focus") }
                    }
                }
            }
        }
        ShadcnStylePreset.entries.forEach { preset ->
            composeRule.onNodeWithTag("style_matrix_btn_${preset.name}").requestFocus()
            composeRule.waitForIdle()
            captureNamed("style_matrix_button_focused_${preset.name.lowercase()}", darkTheme)
        }
    }
}

@Composable
private fun PresetRingSwatch(
    preset: ShadcnStylePreset,
    darkTheme: Boolean,
) {
    ShadcnTheme(preset = preset, isDark = darkTheme) {
        val theme = shadcnTheme
        val swatchStyle =
            Style {
                background(theme.colors.primary)
                shape(RoundedCornerShape(theme.shapes.lg))
                dropShadow(theme.focusRingShadow())
            }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ShadcnText(preset.label, style = ShadcnTextStyle.LabelSmall, muted = true)
            Box(
                modifier =
                    Modifier
                        .padding(top = 8.dp)
                        .size(64.dp, 36.dp)
                        .styleable(remember { MutableStyleState(MutableInteractionSource()) }, swatchStyle),
            )
        }
    }
}

@Composable
private fun ThemedPresetBox(
    preset: ShadcnStylePreset,
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    ShadcnTheme(preset = preset, isDark = darkTheme) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ShadcnText(preset.label, style = ShadcnTextStyle.LabelSmall, muted = true)
            Box(modifier = Modifier.padding(top = 8.dp).width(90.dp)) { content() }
        }
    }
}
