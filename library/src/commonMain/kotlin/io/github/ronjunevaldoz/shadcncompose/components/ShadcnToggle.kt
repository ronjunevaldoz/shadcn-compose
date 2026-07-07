package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import io.github.ronjunevaldoz.shadcncompose.styles.ToggleVariant
import io.github.ronjunevaldoz.shadcncompose.styles.shadcnFocusRing
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * A two-state button, e.g. for toolbar toggles (bold/italic). Distinct from
 * [ShadcnSwitch] (on/off preference) and [ShadcnChip] (pill-shaped filter tag).
 *
 * The `ring*Corner` params default to `shapes.lg` on all four corners (a standalone
 * toggle's real shape) but let [ShadcnToggleGroup] pass each item's actual asymmetric
 * corners (only the outer edge of the group is rounded) so the focus ring traces the
 * item's real silhouette instead of a uniformly-rounded box that doesn't match it.
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
    ringTopStartCorner: Dp? = null,
    ringTopEndCorner: Dp? = null,
    ringBottomEndCorner: Dp? = null,
    ringBottomStartCorner: Dp? = null,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val styleState =
        rememberUpdatedStyleState(interactionSource) {
            it.isChecked = pressed
            it.isEnabled = enabled
        }
    val defaultRingCorner = shadcnTheme.shapes.lg

    Box(
        modifier =
            modifier
                .shadcnFocusRing(
                    focused = isFocused,
                    color = shadcnTheme.colors.borderFocus.copy(alpha = shadcnTheme.ring.opacity),
                    width = shadcnTheme.ring.width,
                    offset = shadcnTheme.ring.offset,
                    topStart = ringTopStartCorner ?: defaultRingCorner,
                    topEnd = ringTopEndCorner ?: defaultRingCorner,
                    bottomEnd = ringBottomEndCorner ?: defaultRingCorner,
                    bottomStart = ringBottomStartCorner ?: defaultRingCorner,
                )
                .toggleable(
                    value = pressed,
                    onValueChange = { onPressedChange?.invoke(it) },
                    enabled = enabled && onPressedChange != null,
                    interactionSource = interactionSource,
                    indication = null,
                )
                .styleable(styleState, variant.style, style),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
