package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * A placeholder block for loading content. Matches real shadcn/ui's `skeleton.tsx`
 * (`animate-pulse rounded-md bg-accent`) -- alpha pulses between 1.0 and 0.5 on a
 * 2s loop, matching Tailwind's default `animate-pulse` keyframe timing.
 *
 * Usage:
 * ```
 * ShadcnSkeleton(modifier = Modifier.size(width = 200.dp, height = 20.dp))
 * ```
 */
@Composable
fun ShadcnSkeleton(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "skeleton-pulse")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 1000),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "skeleton-alpha",
    )
    Box(
        modifier =
            modifier
                .alpha(alpha)
                .background(shadcnTheme.colors.muted, RoundedCornerShape(shadcnTheme.shapes.md)),
    )
}
