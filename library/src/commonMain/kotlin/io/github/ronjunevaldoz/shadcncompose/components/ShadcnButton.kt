package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.foundation.style.then
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonSize
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.styles.shadcnFocusRing
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * shadcn-inspired button.
 *
 * The `ring*Corner` params default to `shapes.lg` on all four corners (a standalone
 * button's real shape) but let [ShadcnButtonGroup] pass each item's actual asymmetric
 * corners (only the outer edge of the group is rounded) so the focus ring traces the
 * item's real silhouette instead of a uniformly-rounded box that doesn't match it --
 * the same pattern [ShadcnToggleGroup] already uses for [ShadcnToggle].
 *
 * Usage:
 * ```
 * ShadcnButton(onClick = {}) { ShadcnText("Click me") }
 * ShadcnButton(onClick = {}, variant = ButtonVariant.Outline, size = ButtonSize.Sm) { ShadcnText("Outline") }
 * ShadcnButton(onClick = {}, variant = ButtonVariant.Destructive) { ShadcnText("Delete") }
 * ```
 */
@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
fun ShadcnButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Default,
    size: ButtonSize = ButtonSize.Md,
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
            it.isEnabled = enabled
        }
    val defaultRingCorner = shadcnTheme.shapes.lg

    Box(
        modifier =
            modifier
                .shadcnFocusRing(
                    focused = isFocused,
                    topStart = ringTopStartCorner ?: defaultRingCorner,
                    topEnd = ringTopEndCorner ?: defaultRingCorner,
                    bottomEnd = ringBottomEndCorner ?: defaultRingCorner,
                    bottomStart = ringBottomStartCorner ?: defaultRingCorner,
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                )
                .styleable(styleState, variant.style then size.style, style),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()
        }
    }
}
