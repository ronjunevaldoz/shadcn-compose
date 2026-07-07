package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * A spinning loading indicator. Real shadcn/ui's `spinner.tsx` renders Lucide's
 * `Loader2Icon` (a circle with one open gap) with `animate-spin` (1s linear, infinite) --
 * this library has no bundled icon set, so the same silhouette is drawn directly with
 * [Canvas]/[Stroke] instead of depending on one just for this.
 *
 * Usage:
 * ```
 * ShadcnSpinner()
 * ShadcnSpinner(modifier = Modifier.size(24.dp))
 * ```
 */
@Composable
fun ShadcnSpinner(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "spinner-rotate")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1000, easing = LinearEasing)),
        label = "spinner-rotation",
    )
    val color = shadcnTheme.colors.onSurface
    Canvas(modifier = modifier.size(16.dp)) {
        rotate(rotation) {
            // A ~270 degree arc (Loader2Icon's open-gap silhouette), not a full circle.
            drawArc(
                color = color,
                startAngle = 0f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = size.minDimension * 0.18f, cap = StrokeCap.Round),
            )
        }
    }
}
