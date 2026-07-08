package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.ronjunevaldoz.shadcncompose.styles.ChipVariant
import io.github.ronjunevaldoz.shadcncompose.styles.rememberStyle
import io.github.ronjunevaldoz.shadcncompose.styles.shadcnFocusRing
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme

/**
 * Selectable chip / filter tag.
 *
 * Icon slot sizing (`leadingIcon`) reads `theme.icons.standardSize` directly at the
 * call site -- it does NOT go through a `CompositionLocal` (the previous
 * `LocalShadcnDataSlots` mechanism was provided here but never actually read by
 * anything reachable, and has been removed) or through the `Style` object itself
 * (`androidx.compose.foundation.style.Style`/`StyleScope` are sealed third-party
 * interfaces from Compose Foundation -- we cannot add an `iconSize` field/DSL function
 * to them). Reading the already-existing, already-preset-aware `theme.icons` token
 * directly is simpler, is genuinely parameter-free at call sites, and was already the
 * pattern every other themed value in this library uses.
 *
 * Usage:
 * ```
 * ShadcnChip(label = "Kotlin", selected = true, onClick = { toggle() })
 * ShadcnChip(label = "Swift", variant = ChipVariant.Outline, onClick = {})
 * ShadcnChip(label = "Verified", leadingIcon = { modifier -> Icon(CheckIcon, modifier = modifier) })
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
    leadingIcon: (@Composable (Modifier) -> Unit)? = null,
    style: Style = Style,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val baseVariantStyle = variant.rememberStyle()
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

    val theme = ShadcnTheme.current

    Row(
        modifier =
            modifier
                // Explicit shape: Chip is always pill-shaped (shapes.full below), never
                // shapes.lg -- without this, shadcnFocusRing()'s own default corner
                // fallback (shapes.lg) draws a ring with visibly sharper corners than the
                // pill it's meant to trace.
                .shadcnFocusRing(isFocused = isFocused, shape = RoundedCornerShape(theme.shapes.full))
                .then(clickableModifier)
                .styleable(styleState, baseVariantStyle, style),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(theme.spacing.xxs),
    ) {
        if (leadingIcon != null) {
            leadingIcon(Modifier.size(theme.icons.standardSize))
        }
        // Still missing the explicit `color = variant.contentColor` pass-through
        // flagged as a pending dark-mode regression in a separate audit -- out of
        // scope for this pass (dead ShadcnRadius/ShadcnDataSlots cleanup only).
        ShadcnText(text = label)
    }
}
