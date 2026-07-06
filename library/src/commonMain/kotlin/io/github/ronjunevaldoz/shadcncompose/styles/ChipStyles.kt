@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.hovered
import androidx.compose.foundation.style.pressed
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ronjunevaldoz.shadcncompose.theme.colors
import io.github.ronjunevaldoz.shadcncompose.theme.shapes
import io.github.ronjunevaldoz.shadcncompose.theme.spacing

sealed interface ChipVariant {
    val style: Style

    data object Default : ChipVariant {
        override val style =
            Style {
                background(colors.secondary)
                contentColor(colors.onSecondary)
                borderWidth(1.dp)
                borderColor(colors.border)
                shape(RoundedCornerShape(shapes.full))
                contentPadding(horizontal = spacing.md, vertical = spacing.xs)
                fontSize(13.sp)
                hovered { background(colors.secondaryHover) }
                pressed { background(colors.secondaryHover) }
            }
    }

    data object Selected : ChipVariant {
        // Same 1.dp border as every other variant (color matches the fill so it's
        // invisible) -- otherwise switching Outline <-> Selected changes the
        // measured size, exactly the bug fixed in ButtonStyles.kt/TextFieldStyles.kt.
        override val style =
            Style {
                background(colors.primary)
                contentColor(colors.onPrimary)
                borderWidth(1.dp)
                borderColor(colors.primary)
                shape(RoundedCornerShape(shapes.full))
                contentPadding(horizontal = spacing.md, vertical = spacing.xs)
                fontSize(13.sp)
            }
    }

    data object Outline : ChipVariant {
        override val style =
            Style {
                borderWidth(1.dp)
                borderColor(colors.border)
                contentColor(colors.onSurface)
                shape(RoundedCornerShape(shapes.full))
                contentPadding(horizontal = spacing.md, vertical = spacing.xs)
                fontSize(13.sp)
                hovered { background(colors.secondary) }
            }
    }
}
