package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.ronjunevaldoz.shadcncompose.styles.ToggleVariant
import io.github.ronjunevaldoz.shadcncompose.styles.rememberStyle

/**
 * A two-state button, e.g. for toolbar toggles (bold/italic). Distinct from
 * [ShadcnSwitch] (on/off preference) and [ShadcnChip] (pill-shaped filter tag).
 *
 * Its focus ring automatically follows whatever shape [ShadcnToggleGroup] (or another
 * grouped container) overrides via its own `style` parameter -- `dropShadow` always
 * follows the final resolved `shape()`, not just whatever this composable's own
 * variant style declares.
 *
 * Usage:
 * ```
 * var bold by remember { mutableStateOf(false) }
 * ShadcnToggle(pressed = bold, onPressedChange = { bold = it }) { ShadcnText("B") }
 * ```
 */
@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
fun ShadcnToggle(
    pressed: Boolean,
    onPressedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ToggleVariant = ToggleVariant.Default,
    style: Style = Style,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val styleState =
        rememberUpdatedStyleState(interactionSource) {
            it.isChecked = pressed
            it.isEnabled = enabled
        }

    Box(
        modifier =
            modifier
                .toggleable(
                    value = pressed,
                    onValueChange = { onPressedChange?.invoke(it) },
                    enabled = enabled && onPressedChange != null,
                    interactionSource = interactionSource,
                    indication = null,
                )
                .styleable(styleState, variant.rememberStyle(), style),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
