@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ronjunevaldoz.shadcncompose.theme.colors
import io.github.ronjunevaldoz.shadcncompose.theme.shapes
import io.github.ronjunevaldoz.shadcncompose.theme.spacing

sealed interface BadgeVariant {
    val style: Style

    data object Default : BadgeVariant {
        override val style =
            Style {
                background(colors.primary)
                contentColor(colors.onPrimary)
                shape(RoundedCornerShape(shapes.full))
                contentPadding(horizontal = spacing.sm, vertical = spacing.xxs)
                fontSize(12.sp)
                fontWeight(FontWeight.SemiBold)
            }
    }

    data object Secondary : BadgeVariant {
        override val style =
            Style {
                background(colors.secondary)
                contentColor(colors.onSecondary)
                shape(RoundedCornerShape(shapes.full))
                contentPadding(horizontal = spacing.sm, vertical = spacing.xxs)
                fontSize(12.sp)
                fontWeight(FontWeight.SemiBold)
            }
    }

    data object Destructive : BadgeVariant {
        override val style =
            Style {
                background(colors.destructive)
                contentColor(colors.onDestructive)
                shape(RoundedCornerShape(shapes.full))
                contentPadding(horizontal = spacing.sm, vertical = spacing.xxs)
                fontSize(12.sp)
                fontWeight(FontWeight.SemiBold)
            }
    }

    data object Outline : BadgeVariant {
        override val style =
            Style {
                contentColor(colors.onSurface)
                borderWidth(1.dp)
                borderColor(colors.border)
                shape(RoundedCornerShape(shapes.full))
                contentPadding(horizontal = spacing.sm, vertical = spacing.xxs)
                fontSize(12.sp)
                fontWeight(FontWeight.SemiBold)
            }
    }

    data object Ghost : BadgeVariant {
        override val style =
            Style {
                background(colors.muted)
                contentColor(colors.onMuted)
                shape(RoundedCornerShape(shapes.full))
                contentPadding(horizontal = spacing.sm, vertical = spacing.xxs)
                fontSize(12.sp)
            }
    }
}
