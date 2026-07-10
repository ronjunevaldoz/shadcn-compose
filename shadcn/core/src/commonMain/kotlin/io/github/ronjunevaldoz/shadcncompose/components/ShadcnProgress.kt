package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * A linear progress bar. Matches real shadcn/ui's `progress.tsx`
 * (`h-2 w-full rounded-full bg-primary/20`, indicator `bg-primary`).
 *
 * Usage:
 * ```
 * ShadcnProgress(value = 0.6f)
 * ```
 */
@Composable
fun ShadcnProgress(
    value: Float,
    modifier: Modifier = Modifier,
) {
    val animatedValue by animateFloatAsState(value.coerceIn(0f, 1f), label = "progress")
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(shadcnTheme.colors.primary.copy(alpha = 0.2f), RoundedCornerShape(shadcnTheme.shapes.full)),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedValue)
                    .align(Alignment.CenterStart)
                    .background(shadcnTheme.colors.primary, RoundedCornerShape(shadcnTheme.shapes.full)),
        )
    }
}
