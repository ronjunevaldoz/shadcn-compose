@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.focused
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.styles.focusRingShadow
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme

sealed interface SelectVariant {
    data object Default : SelectVariant
}

/**
 * The trigger's style. Matches real shadcn/ui's `select.tsx` `SelectTrigger`
 * (`border border-input bg-transparent`) -- a bordered field, not a filled button.
 * Also matches real shadcn's `focus-visible:border-ring focus-visible:ring-[3px]
 * focus-visible:ring-ring/50` -- verified missing here previously (this trigger never
 * showed a focus ring, unlike [ShadcnCombobox]'s trigger which already had one via
 * [ButtonVariant.Outline]).
 */
@Composable
fun SelectVariant.rememberStyle(): Style {
    val theme = ShadcnTheme.LocalShadcnTheme.current
    val colors = theme.colors
    val shapes = theme.shapes

    return remember(this, theme, colors, shapes) {
        Style {
            background(colors.background)
            borderWidth(1.dp)
            borderColor(colors.border)
            shape(RoundedCornerShape(shapes.lg))
            focused {
                borderColor(colors.borderFocus)
                dropShadow(theme.focusRingShadow())
            }
        }
    }
}

/**
 * The dropdown panel's style. Matches real shadcn's `SelectContent`
 * (`bg-popover text-popover-foreground`) -- the floating-panel role, not the generic
 * `surface` token (see [io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnColors]).
 */
@Composable
fun SelectVariant.rememberPanelStyle(): Style {
    val theme = ShadcnTheme.LocalShadcnTheme.current
    val colors = theme.colors
    val shapes = theme.shapes

    return remember(this, colors, shapes) {
        Style {
            background(colors.popover)
            borderWidth(1.dp)
            borderColor(colors.border)
            shape(RoundedCornerShape(shapes.md))
        }
    }
}

/** Matches real shadcn's `SelectItem` (`focus:bg-accent`) -- selected rows highlight via [io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnColors.secondary]. */
@Composable
fun rememberSelectItemStyle(isSelected: Boolean): Style {
    val theme = ShadcnTheme.LocalShadcnTheme.current
    val colors = theme.colors
    val shapes = theme.shapes

    return remember(isSelected, colors, shapes) {
        Style {
            background(if (isSelected) colors.secondary else colors.popover)
            contentColor(if (isSelected) colors.onSecondary else colors.onPopover)
            shape(RoundedCornerShape(shapes.sm))
        }
    }
}
