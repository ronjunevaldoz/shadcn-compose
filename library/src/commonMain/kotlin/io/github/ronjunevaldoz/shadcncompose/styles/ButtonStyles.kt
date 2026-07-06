@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.disabled
import androidx.compose.foundation.style.focused
import androidx.compose.foundation.style.hovered
import androidx.compose.foundation.style.then
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ronjunevaldoz.shadcncompose.theme.colors
import io.github.ronjunevaldoz.shadcncompose.theme.shapes
import io.github.ronjunevaldoz.shadcncompose.theme.spacing

// Matches shadcn/ui's real button.tsx (github.com/shadcn-ui/ui) as closely as our
// Style API allows: most variants have zero border (only Outline has one), hover
// is an alpha-blended background rather than a whole-node dim, and focus uses
// focusRingStyle's dropShadow ring instead of thickening a border -- so none of
// this ever changes a button's measured size.

sealed interface ButtonVariant {
    val style: Style

    data object Default : ButtonVariant {
        // bg-primary text-primary-foreground hover:bg-primary/90
        override val style =
            Style {
                background(colors.primary)
                contentColor(colors.onPrimary)
                shape(RoundedCornerShape(shapes.md))
                hovered { background(colors.primary.copy(alpha = 0.9f)) }
                disabled { alpha(0.5f) }
            } then focusRingStyle
    }

    data object Outline : ButtonVariant {
        // border bg-background hover:bg-accent hover:text-accent-foreground
        override val style =
            Style {
                background(colors.background)
                contentColor(colors.onSurface)
                borderWidth(1.dp)
                borderColor(colors.border)
                shape(RoundedCornerShape(shapes.md))
                hovered {
                    background(colors.secondary)
                    contentColor(colors.onSecondary)
                }
                focused { borderColor(colors.borderFocus) }
                disabled { alpha(0.5f) }
            } then focusRingStyle
    }

    data object Secondary : ButtonVariant {
        // bg-secondary text-secondary-foreground hover:bg-secondary/80
        override val style =
            Style {
                background(colors.secondary)
                contentColor(colors.onSecondary)
                shape(RoundedCornerShape(shapes.md))
                hovered { background(colors.secondary.copy(alpha = 0.8f)) }
                disabled { alpha(0.5f) }
            } then focusRingStyle
    }

    data object Ghost : ButtonVariant {
        // hover:bg-accent hover:text-accent-foreground
        override val style =
            Style {
                contentColor(colors.onSurface)
                shape(RoundedCornerShape(shapes.md))
                hovered {
                    background(colors.secondary)
                    contentColor(colors.onSecondary)
                }
                disabled { alpha(0.5f) }
            } then focusRingStyle
    }

    data object Destructive : ButtonVariant {
        // bg-destructive text-white hover:bg-destructive/90
        override val style =
            Style {
                background(colors.destructive)
                contentColor(colors.onDestructive)
                shape(RoundedCornerShape(shapes.md))
                hovered { background(colors.destructive.copy(alpha = 0.9f)) }
                disabled { alpha(0.5f) }
            } then focusRingStyle
    }

    data object Link : ButtonVariant {
        // text-primary underline-offset-4 hover:underline
        override val style =
            Style {
                contentColor(colors.primary)
                hovered { textDecoration(TextDecoration.Underline) }
                disabled { alpha(0.5f) }
            } then focusRingStyle
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
