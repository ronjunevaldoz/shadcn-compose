@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import io.github.ronjunevaldoz.shadcncompose.styles.shadcnFocusRing
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnStylePreset
import kotlin.test.Test

/**
 * Documented, repeatable proof that [ShadcnStylePreset] actually drives per-style
 * shape/ring differences end to end (`App.kt` -> `ShadcnTheme.light/dark(ring = ...)` ->
 * `ShadcnButton` -> `shadcnFocusRing`) -- this is the durable replacement for eyeballing
 * a live browser toggle, which can't be committed to git or diffed in a PR.
 *
 * Two kinds of capture per theme:
 * 1. `style_matrix_ring_swatch`: all 8 presets' ring token (width/offset/corner) drawn
 *    forced-on in one composite image, side by side -- no interaction simulation needed
 *    since `shadcnFocusRing`'s `focused` param is a plain Boolean.
 * 2. `style_matrix_button_focused_<preset>`: the *real* `ShadcnButton`, focused via real
 *    `requestFocus()`, one capture per preset -- proves the wiring through the actual
 *    component, not just the shared modifier.
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
                ShadcnStylePreset.entries.forEach { preset -> PresetRingSwatch(preset) }
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
private fun PresetRingSwatch(preset: ShadcnStylePreset) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ShadcnText(preset.label, style = ShadcnTextStyle.LabelSmall, muted = true)
        Box(
            modifier =
                Modifier
                    .padding(top = 8.dp)
                    .size(64.dp, 36.dp)
                    .shadcnFocusRing(
                        focused = true,
                        color = shadcnTheme.colors.borderFocus.copy(alpha = preset.ring.opacity),
                        cornerRadius = preset.shapes.lg,
                        width = preset.ring.width,
                        offset = preset.ring.offset,
                    ).background(shadcnTheme.colors.primary, RoundedCornerShape(preset.shapes.lg)),
        )
    }
}

@Composable
private fun ThemedPresetBox(
    preset: ShadcnStylePreset,
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val theme =
        if (darkTheme) {
            ShadcnTheme.dark(
                shapes = preset.shapes,
                spacing = preset.spacing,
                typography = preset.typography,
                ring = preset.ring,
            )
        } else {
            ShadcnTheme.light(
                shapes = preset.shapes,
                spacing = preset.spacing,
                typography = preset.typography,
                ring = preset.ring,
            )
        }
    ShadcnTheme(darkTheme = darkTheme, theme = theme) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ShadcnText(preset.label, style = ShadcnTextStyle.LabelSmall, muted = true)
            Box(modifier = Modifier.padding(top = 8.dp).width(90.dp)) { content() }
        }
    }
}
