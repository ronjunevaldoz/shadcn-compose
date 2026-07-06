@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.disabled
import androidx.compose.foundation.style.focused
import androidx.compose.foundation.style.hovered
import androidx.compose.foundation.style.pressed
import androidx.compose.foundation.style.then
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ronjunevaldoz.shadcncompose.theme.colors
import io.github.ronjunevaldoz.shadcncompose.theme.shapes
import io.github.ronjunevaldoz.shadcncompose.theme.spacing

// The focus ring must never change the button's measured size, so every variant
// below reserves the same 2.dp border width up front (transparent when there's no
// visible border) and this style only swaps the border *color* on focus.
private const val FOCUS_RING_WIDTH_DP = 2

internal val buttonInteractionStyle: Style =
    Style {
        hovered { alpha(0.90f) }
        pressed { alpha(0.80f) }
        disabled { alpha(0.38f) }
        focused { borderColor(colors.borderFocus) }
    }

sealed interface ButtonVariant {
    val style: Style

    data object Default : ButtonVariant {
        override val style =
            Style {
                background(colors.primary)
                contentColor(colors.onPrimary)
                borderWidth(FOCUS_RING_WIDTH_DP.dp)
                borderColor(Color.Transparent)
                shape(RoundedCornerShape(shapes.md))
            } then buttonInteractionStyle
    }

    data object Outline : ButtonVariant {
        override val style =
            Style {
                background(colors.background)
                contentColor(colors.onSurface)
                borderWidth(FOCUS_RING_WIDTH_DP.dp)
                borderColor(colors.border)
                shape(RoundedCornerShape(shapes.md))
                hovered { background(colors.secondary) }
                pressed { background(colors.secondary) }
            } then buttonInteractionStyle
    }

    data object Secondary : ButtonVariant {
        override val style =
            Style {
                background(colors.secondary)
                contentColor(colors.onSecondary)
                borderWidth(FOCUS_RING_WIDTH_DP.dp)
                borderColor(Color.Transparent)
                shape(RoundedCornerShape(shapes.md))
                hovered { background(colors.secondaryHover) }
            } then buttonInteractionStyle
    }

    data object Ghost : ButtonVariant {
        override val style =
            Style {
                contentColor(colors.onSurface)
                borderWidth(FOCUS_RING_WIDTH_DP.dp)
                borderColor(Color.Transparent)
                shape(RoundedCornerShape(shapes.md))
                hovered { background(colors.secondary) }
                pressed { background(colors.secondary) }
            } then buttonInteractionStyle
    }

    data object Destructive : ButtonVariant {
        override val style =
            Style {
                background(colors.destructive)
                contentColor(colors.onDestructive)
                borderWidth(FOCUS_RING_WIDTH_DP.dp)
                borderColor(Color.Transparent)
                shape(RoundedCornerShape(shapes.md))
                hovered { background(colors.destructiveHover) }
            } then buttonInteractionStyle
    }

    data object Link : ButtonVariant {
        override val style =
            Style {
                contentColor(colors.primary)
                hovered { alpha(0.70f) }
            }
    }
}

sealed interface ButtonSize {
    val style: Style

    data object Xs : ButtonSize {
        override val style =
            Style {
                contentPadding(horizontal = spacing.sm, vertical = spacing.xs)
                fontSize(12.sp)
                height(28.dp)
            }
    }

    data object Sm : ButtonSize {
        override val style =
            Style {
                contentPadding(horizontal = spacing.md, vertical = spacing.xs)
                fontSize(14.sp)
                height(32.dp)
            }
    }

    data object Md : ButtonSize {
        override val style =
            Style {
                contentPadding(horizontal = spacing.lg, vertical = spacing.sm)
                fontSize(14.sp)
                height(40.dp)
            }
    }

    data object Lg : ButtonSize {
        override val style =
            Style {
                contentPadding(horizontal = spacing.xl, vertical = spacing.md)
                fontSize(16.sp)
                height(48.dp)
            }
    }

    data object Icon : ButtonSize {
        override val style =
            Style {
                contentPadding(spacing.sm)
                width(40.dp)
                height(40.dp)
            }
    }
}
