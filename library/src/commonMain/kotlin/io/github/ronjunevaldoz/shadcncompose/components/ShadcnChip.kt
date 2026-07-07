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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.ronjunevaldoz.shadcncompose.styles.ChipVariant
import io.github.ronjunevaldoz.shadcncompose.styles.rememberStyle
import io.github.ronjunevaldoz.shadcncompose.styles.shadcnFocusRing
import io.github.ronjunevaldoz.shadcncompose.theme.LocalShadcnDataSlots
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnDataSlots
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme

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

    // 1. Resolve the clean, dynamic base variant style for dark/light parity
    val baseVariantStyle = variant.rememberStyle()

    val styleState = rememberUpdatedStyleState(interactionSource) {
        it.isEnabled = enabled
    }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick,
        )
    } else {
        Modifier
    }

    // 2. Set up the Tailwind v4 data-slot rules for child layouts automatically!
    val theme = ShadcnTheme.current
    val slotDimensions = remember(theme) {
        ShadcnDataSlots(
            iconSize = theme.icons.standardSize,
            paddingHorizontal = theme.spacing.md,
            paddingVertical = theme.spacing.xs
        )
    }

    CompositionLocalProvider(LocalShadcnDataSlots provides slotDimensions) {
        Row(
            modifier = modifier
                // 3. Cleaner! No more passing theme shapes manually. Focus ring handles its own context.
                .shadcnFocusRing(isFocused = isFocused)
                .then(clickableModifier)
                .styleable(styleState, baseVariantStyle, style),
            verticalAlignment = Alignment.CenterVertically,
            // 4. Use your structural layout tokens instead of hardcoded numbers
            horizontalArrangement = Arrangement.spacedBy(theme.spacing.xxs),
        ) {
            ShadcnText(text = label)
        }
    }
}