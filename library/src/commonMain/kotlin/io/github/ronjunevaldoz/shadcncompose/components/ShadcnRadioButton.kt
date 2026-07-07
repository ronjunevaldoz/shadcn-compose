package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.styles.radioButtonStyle
import io.github.ronjunevaldoz.shadcncompose.styles.shadcnFocusRing
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * A single radio indicator. Combine several inside [ShadcnRadioGroup] -- this
 * mirrors shadcn/ui's RadioGroup + RadioGroupItem split rather than a monolithic
 * "options list" API, so callers keep full control over row layout and labels.
 *
 * Usage:
 * ```
 * ShadcnRadioGroup {
 *     Row(verticalAlignment = Alignment.CenterVertically) {
 *         ShadcnRadioButton(selected = choice == "a", onClick = { choice = "a" })
 *         ShadcnLabel("Option A")
 *     }
 * }
 * ```
 */
@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
fun ShadcnRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: Style = Style,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val styleState =
        rememberUpdatedStyleState(interactionSource) {
            it.isSelected = selected
            it.isEnabled = enabled
        }

    Box(
        modifier =
            modifier
                .size(16.dp)
                .shadcnFocusRing(
                    focused = isFocused,
                    color = shadcnTheme.colors.borderFocus.copy(alpha = shadcnTheme.ring.opacity),
                    width = shadcnTheme.ring.width,
                    offset = shadcnTheme.ring.offset,
                    cornerRadius = shadcnTheme.shapes.full,
                )
                .selectable(
                    selected = selected,
                    onClick = { onClick?.invoke() },
                    enabled = enabled && onClick != null,
                    interactionSource = interactionSource,
                    indication = null,
                )
                .styleable(styleState, radioButtonStyle, style),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            val dotColor = shadcnTheme.colors.onPrimary
            Canvas(modifier = Modifier.size(8.dp)) {
                drawRoundRect(
                    color = dotColor,
                    topLeft = Offset.Zero,
                    size = size,
                    cornerRadius = CornerRadius(size.minDimension / 2f),
                )
            }
        }
    }
}

/** Groups [ShadcnRadioButton]s for accessibility semantics (single-selection). */
@Composable
fun ShadcnRadioGroup(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.selectableGroup()) {
        content()
    }
}
