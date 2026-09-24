@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.contentPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnColors

sealed interface BadgeVariant {
    data object Default : BadgeVariant

    data object Secondary : BadgeVariant

    data object Destructive : BadgeVariant

    data object Outline : BadgeVariant

    data object Ghost : BadgeVariant
}

fun BadgeVariant.contentColor(colors: ShadcnColors): Color =
    when (this) {
        BadgeVariant.Default -> colors.onPrimary
        BadgeVariant.Secondary -> colors.onSecondary
        BadgeVariant.Destructive -> colors.onDestructive
        BadgeVariant.Outline -> colors.onSurface
        BadgeVariant.Ghost -> colors.onMuted
    }

@Composable
fun BadgeVariant.rememberStyle(): Style =
    rememberShadcnStyle(this) {
        when (this@rememberStyle) {
            BadgeVariant.Default ->
                Style {
                    background(colors.primary)
                    contentColor(colors.onPrimary)
                    shape(RoundedCornerShape(shapes.full))
                    contentPadding(horizontal = spacing.sm, vertical = spacing.xxs)
                    fontSize(12.sp)
                    fontWeight(FontWeight.SemiBold)
                }

            BadgeVariant.Secondary ->
                Style {
                    background(colors.secondary)
                    contentColor(colors.onSecondary)
                    shape(RoundedCornerShape(shapes.full))
                    contentPadding(horizontal = spacing.sm, vertical = spacing.xxs)
                    fontSize(12.sp)
                    fontWeight(FontWeight.SemiBold)
                }

            BadgeVariant.Destructive ->
                Style {
                    background(colors.destructive)
                    contentColor(colors.onDestructive)
                    shape(RoundedCornerShape(shapes.full))
                    contentPadding(horizontal = spacing.sm, vertical = spacing.xxs)
                    fontSize(12.sp)
                    fontWeight(FontWeight.SemiBold)
                }

            BadgeVariant.Outline ->
                Style {
                    contentColor(colors.onSurface)
                    border(colors.border)
                    shape(RoundedCornerShape(shapes.full))
                    contentPadding(horizontal = spacing.sm, vertical = spacing.xxs)
                    fontSize(12.sp)
                    fontWeight(FontWeight.SemiBold)
                }

            BadgeVariant.Ghost ->
                Style {
                    background(colors.muted)
                    contentColor(colors.onMuted)
                    shape(RoundedCornerShape(shapes.full))
                    contentPadding(horizontal = spacing.sm, vertical = spacing.xxs)
                    fontSize(12.sp)
                }
        }
    }
