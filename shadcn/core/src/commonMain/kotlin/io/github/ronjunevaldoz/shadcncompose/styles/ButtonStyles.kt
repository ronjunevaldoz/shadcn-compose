@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.hovered
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnColors

// Matches shadcn/ui's real button.tsx (github.com/shadcn-ui/ui) as closely as our
// Style API allows: most variants have zero border (only Outline has one), hover
// is an alpha-blended background rather than a whole-node dim. Every variant gets
// the same `focusRing(...)` ring -- real shadcn's shared `.cn-button` class applies
// focus-visible:ring-* regardless of variant, including Link.

sealed interface ButtonVariant {
    data object Default : ButtonVariant

    data object Outline : ButtonVariant

    data object Secondary : ButtonVariant

    data object Ghost : ButtonVariant

    data object Destructive : ButtonVariant

    data object Link : ButtonVariant
}

/**
 * The same color each variant passes to `contentColor(...)` below, factored out so
 * [io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton] can also provide it via
 * [io.github.ronjunevaldoz.shadcncompose.theme.LocalShadcnContentColor] for
 * [io.github.ronjunevaldoz.shadcncompose.components.ShadcnIcon] to read -- the Style API's own
 * `contentColor` has no public reader, only [rememberStyle]'s `Style { }` block can set it.
 * Ignores hover/disabled state (unlike the `Style` block below): a static per-variant color is
 * enough for icon tinting, and tracking those dynamically here would duplicate this same `when`
 * a second time for no real visual benefit.
 */
fun ButtonVariant.iconTint(colors: ShadcnColors): Color =
    when (this) {
        ButtonVariant.Default -> colors.onPrimary
        ButtonVariant.Outline -> colors.onSurface
        ButtonVariant.Secondary -> colors.onSecondary
        ButtonVariant.Ghost -> colors.onSurface
        ButtonVariant.Destructive -> colors.onDestructive
        ButtonVariant.Link -> colors.primary
    }

@Composable
fun ButtonVariant.rememberStyle(): Style =
    rememberShadcnStyle(this) {
        when (this@rememberStyle) {
            ButtonVariant.Default ->
                Style {
                    background(colors.primary)
                    contentColor(colors.onPrimary)
                    hovered { background(colors.primary.copy(alpha = 0.9f)) }
                    focusRing(RoundedCornerShape(shapes.lg))
                    pressedMoveDown()
                    disabledDim()
                }

            ButtonVariant.Outline ->
                Style {
                    background(colors.background)
                    contentColor(colors.onSurface)
                    border(colors.border)
                    hovered {
                        background(colors.secondary)
                        contentColor(colors.onSecondary)
                    }
                    focusRing(RoundedCornerShape(shapes.lg))
                    pressedMoveDown()
                    disabledDim()
                }

            ButtonVariant.Secondary ->
                Style {
                    background(colors.secondary)
                    contentColor(colors.onSecondary)
                    hovered { background(colors.secondary.copy(alpha = 0.8f)) }
                    focusRing(RoundedCornerShape(shapes.lg))
                    pressedMoveDown()
                    disabledDim()
                }

            ButtonVariant.Ghost ->
                Style {
                    contentColor(colors.onSurface)
                    hovered {
                        background(colors.secondary)
                        contentColor(colors.onSecondary)
                    }
                    focusRing(RoundedCornerShape(shapes.lg))
                    pressedMoveDown()
                    disabledDim()
                }

            ButtonVariant.Destructive ->
                Style {
                    background(colors.destructive)
                    contentColor(colors.onDestructive)
                    hovered { background(colors.destructive.copy(alpha = 0.9f)) }
                    focusRing(RoundedCornerShape(shapes.lg))
                    pressedMoveDown()
                    disabledDim()
                }

            // No pressedMoveDown() -- Link renders as inline text, not a boxed control, so
            // nudging it on press (rather than the underline real shadcn's own .cn-button
            // gives it) would look like the surrounding text is glitching, not a button push.
            ButtonVariant.Link ->
                Style {
                    contentColor(colors.primary)
                    hovered { textDecoration(TextDecoration.Underline) }
                    focusRing(RoundedCornerShape(shapes.lg))
                    disabledDim()
                }
        }
    }

sealed interface ButtonSize {
    data object Xs : ButtonSize

    data object Sm : ButtonSize

    data object Md : ButtonSize

    data object Lg : ButtonSize

    data object Icon : ButtonSize
}

@Composable
fun ButtonSize.rememberStyle(): Style =
    rememberShadcnStyle(this) {
        when (this@rememberStyle) {
            ButtonSize.Xs ->
                Style {
                    contentPadding(horizontal = spacing.sm, vertical = spacing.xs)
                    fontSize(12.sp)
                    height(28.dp)
                }

            ButtonSize.Sm ->
                Style {
                    contentPadding(horizontal = spacing.md, vertical = spacing.xs)
                    fontSize(14.sp)
                    height(32.dp)
                }

            ButtonSize.Md ->
                Style {
                    contentPadding(horizontal = spacing.lg, vertical = spacing.sm)
                    fontSize(14.sp)
                    height(36.dp)
                }

            ButtonSize.Lg ->
                Style {
                    contentPadding(horizontal = spacing.xxl, vertical = spacing.md)
                    fontSize(16.sp)
                    height(40.dp)
                }

            ButtonSize.Icon ->
                Style {
                    contentPadding(spacing.sm)
                    width(36.dp)
                    height(36.dp)
                }
        }
    }
