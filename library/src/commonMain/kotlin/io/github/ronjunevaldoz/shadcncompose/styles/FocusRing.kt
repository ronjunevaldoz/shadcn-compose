package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme

/**
 * shadcn's real focus indicator (`focus-visible:ring-[3px] focus-visible:ring-ring/50`) is a
 * crisp, hard-edged ring drawn entirely outside the box -- CSS box-shadow with a zero blur
 * radius never softens the edge. The Style API's `dropShadow`, however, always rasterizes
 * through an offscreen bitmap even when `radius = 0.dp`, which visibly blurs a ring this thin.
 * Drawing it directly with `drawOutline`/[Stroke] bypasses that bitmap path entirely, so the
 * ring renders crisp on every target the same way CSS box-shadow does.
 *
 * The ring is drawn *before* [drawContent], not after: at `offset = 0` (the default, and
 * shadcn's classic "New York" style), a centered [Stroke] straddles its own path
 * half-inward, half-outward, and CSS box-shadow relies on the element's own opaque
 * background painting over that inward half so only the outward half survives. Drawing the
 * ring on top of the content (the initial, wrong version of this function) leaves that inward
 * half visible as a translucent overlay on the content's own edge, making the ring look roughly
 * twice as thick as intended. At `offset > 0` (shadcn's classic "Default" style, `ring-offset-2`)
 * the whole stroke is pushed outside the component by `offset + width / 2`, so it never overlaps
 * [drawContent] at all -- the draw order stops mattering there, but keeping the ring first is
 * still correct and simpler than branching.
 */

/**
 * Convenience overload that reads width/opacity/offset from the current [ShadcnTheme].
 */
@Composable
fun Modifier.shadcnFocusRing(
    focused: Boolean,
    cornerRadius: Dp,
    color: Color = ShadcnTheme.LocalShadcnTheme.current.colors.borderFocus
        .copy(alpha = ShadcnTheme.LocalShadcnTheme.current.ring.opacity),
): Modifier {
    val theme = ShadcnTheme.LocalShadcnTheme.current
    return shadcnFocusRing(
        focused = focused,
        color = color,
        cornerRadius = cornerRadius,
        width = theme.ring.width,
        offset = theme.ring.offset
    )
}

fun Modifier.shadcnFocusRing(
    focused: Boolean,
    color: Color,
    cornerRadius: Dp,
    width: Dp = 3.dp,
    offset: Dp = 0.dp,
): Modifier =
    shadcnFocusRing(
        focused = focused,
        color = color,
        topStart = cornerRadius,
        topEnd = cornerRadius,
        bottomEnd = cornerRadius,
        bottomStart = cornerRadius,
        width = width,
        offset = offset,
    )

/**
 * Per-corner variant -- needed for grouped items (e.g. [io.github.ronjunevaldoz.shadcncompose.components.ShadcnToggleGroup])
 * whose real shape only rounds the outer edges of the group, not all four corners uniformly.
 * Passing the group item's own asymmetric corners here keeps the ring flush with its actual
 * silhouette instead of tracing a uniformly-rounded box that doesn't match.
 */
fun Modifier.shadcnFocusRing(
    focused: Boolean,
    color: Color,
    topStart: Dp,
    topEnd: Dp,
    bottomEnd: Dp,
    bottomStart: Dp,
    width: Dp = 3.dp,
    offset: Dp = 0.dp,
): Modifier =
    if (!focused) {
        this
    } else {
        this.drawWithContent {
            val strokePx = width.toPx()
            val offsetPx = offset.toPx()
            val growth = offsetPx + strokePx / 2f
            inset(-growth) {
                val maxRadiusPx = minOf(size.width, size.height) / 2f

                fun corner(dp: Dp) = CornerRadius((dp.toPx() + growth).coerceAtMost(maxRadiusPx))
                val roundRect =
                    RoundRect(
                        left = 0f,
                        top = 0f,
                        right = size.width,
                        bottom = size.height,
                        topLeftCornerRadius = corner(topStart),
                        topRightCornerRadius = corner(topEnd),
                        bottomRightCornerRadius = corner(bottomEnd),
                        bottomLeftCornerRadius = corner(bottomStart),
                    )
                val path = Path().apply { addRoundRect(roundRect) }
                drawPath(path, color = color, style = Stroke(width = strokePx))
            }
            drawContent()
        }
    }
