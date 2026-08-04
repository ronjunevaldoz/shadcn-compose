# Step 3: Loading state components

Part of `kmp-compose-design-system-extended`. Load this file when implementing the components below.

---

## Step 3: Loading state components

### `components/AppSpinner.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme

sealed interface SpinnerSize {
    val dp: Dp
    val stroke: Float
    data object Sm : SpinnerSize { override val dp = 16.dp; override val stroke = 2f }
    data object Md : SpinnerSize { override val dp = 24.dp; override val stroke = 2.5f }
    data object Lg : SpinnerSize { override val dp = 32.dp; override val stroke = 3f }
}

/**
 * Circular indeterminate spinner. Uses rememberInfiniteTransition (Styles API
 * doesn't support infinite animations).
 *
 * Usage:
 * ```
 * AppSpinner()
 * AppSpinner(size = SpinnerSize.Sm, color = colors.onPrimary)
 * ```
 */
@Composable
fun AppSpinner(
    modifier: Modifier = Modifier,
    size: SpinnerSize = SpinnerSize.Md,
    color: Color = appTheme.colors.primary,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spinner")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "spinnerRotation",
    )

    Canvas(modifier = modifier.size(size.dp)) {
        val padding = size.stroke / 2
        drawArc(
            color = color.copy(alpha = 0.2f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = size.stroke, cap = StrokeCap.Round),
        )
        drawArc(
            color = color,
            startAngle = rotation,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = size.stroke, cap = StrokeCap.Round),
        )
    }
}
```

### `components/AppProgress.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme

/**
 * Linear progress bar. Pass null for indeterminate.
 *
 * Usage:
 * ```
 * AppProgress(progress = 0.75f)                // 75% filled
 * AppProgress(progress = null)                 // indeterminate — sweeping bar animation
 * AppProgress(progress = 0.5f, height = 8.dp)
 * ```
 */
@Composable
fun AppProgress(
    progress: Float?,
    modifier: Modifier = Modifier,
    height: Dp = 4.dp,
    color: Color = appTheme.colors.primary,
    trackColor: Color = appTheme.colors.secondary,
) {
    if (progress == null) {
        // Indeterminate — animate a sweeping bar
        val infiniteTransition = rememberInfiniteTransition(label = "progress")
        val offsetFraction by infiniteTransition.animateFloat(
            initialValue = -0.5f,
            targetValue = 1.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200),
                repeatMode = RepeatMode.Restart,
            ),
            label = "progressOffset",
        )
        // Capture the track's pixel width so graphicsLayer can translate across the full track,
        // not just across the inner indicator box (which is only 40% wide).
        var containerWidth by remember { mutableStateOf(0) }
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .clip(CircleShape)
                .background(trackColor)
                .onSizeChanged { containerWidth = it.width },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(height)
                    .clip(CircleShape)
                    .background(color)
                    .graphicsLayer { translationX = containerWidth * offsetFraction },
            )
        }
    } else {
        val animatedProgress by animateFloatAsState(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 300),
            label = "progressValue",
        )
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .clip(CircleShape)
                .background(trackColor),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(height)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}
```

### `components/AppCircularProgress.kt`

```kotlin
package GROUP_ID.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme

/**
 * Circular ring progress indicator. Pass null for indeterminate (continuously rotating
 * arc). The determinate variant is what `AppProgress` (linear) does not cover — use
 * this for compact/dashboard-style progress (upload %, download %, completion rings).
 *
 * Usage:
 * ```
 * AppCircularProgress(progress = 0.75f)                 // 75% ring
 * AppCircularProgress(progress = null)                  // indeterminate — rotating arc
 * AppCircularProgress(progress = 0.5f, size = 48.dp, strokeWidth = 4.dp)
 * ```
 */
@Composable
fun AppCircularProgress(
    progress: Float?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    strokeWidth: Dp = 3.dp,
    color: Color = appTheme.colors.primary,
    trackColor: Color = appTheme.colors.secondary,
) {
    if (progress == null) {
        // Indeterminate — rotating partial arc, same "infinite -> rememberInfiniteTransition"
        // rule as AppSpinner: Styles API does not support infinite animations.
        val infiniteTransition = rememberInfiniteTransition(label = "circularProgress")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "circularProgressRotation",
        )
        Canvas(modifier = modifier.size(size)) {
            rotate(rotation) {
                drawArc(
                    color = color,
                    startAngle = 0f,
                    sweepAngle = 90f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
                )
            }
        }
    } else {
        val animatedProgress by animateFloatAsState(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 300),
            label = "circularProgressValue",
        )
        Canvas(modifier = modifier.size(size)) {
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}
```

### `components/AppSkeleton.kt`

```kotlin
package GROUP_ID.core.designsystem.components

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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import GROUP_ID.core.designsystem.theme.appTheme

/**
 * Pulsing skeleton placeholder for loading states.
 *
 * Usage:
 * ```
 * AppSkeleton(Modifier.fillMaxWidth().height(20.dp))
 * AppSkeleton(Modifier.size(40.dp).clip(CircleShape))  // avatar skeleton
 * ```
 */
@Composable
fun AppSkeleton(
    modifier: Modifier = Modifier,
    baseColor: Color = appTheme.colors.secondary,
    highlightColor: Color = appTheme.colors.surfaceVariant,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skeletonAlpha",
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(baseColor.copy(alpha = alpha)),
    )
}
```

---

