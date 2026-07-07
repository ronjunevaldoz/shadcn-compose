package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.styles.ChipVariant
import io.github.ronjunevaldoz.shadcncompose.styles.contentColor
import io.github.ronjunevaldoz.shadcncompose.styles.shadcnFocusRing
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * Selectable chip / filter tag.
 *
 * Usage:
 * ```
 * ShadcnChip(label = "Kotlin", selected = true, onClick = { toggle() })
 * ShadcnChip(label = "Swift", variant = ChipVariant.Outline, onClick = {})
 * ```
 */
@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
fun ShadcnChip(
    label: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    variant: ChipVariant = if (selected) ChipVariant.Selected else ChipVariant.Default,
    style: Style = Style,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val styleState =
        rememberUpdatedStyleState(interactionSource) {
            it.isEnabled = enabled
        }

    val clickableModifier =
        if (onClick != null) {
            Modifier.clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
        } else {
            Modifier
        }

    Row(
        modifier =
            modifier
                .shadcnFocusRing(
                    focused = isFocused,
                    cornerRadius = shadcnTheme.shapes.full,
                )
                .then(clickableModifier)
                .styleable(styleState, variant.style, style),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ShadcnText(text = label, color = variant.contentColor)
    }
}
