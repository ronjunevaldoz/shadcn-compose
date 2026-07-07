package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.github.ronjunevaldoz.shadcncompose.components.LocalGroupCorners
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

fun Modifier.shadcnFocusRing(
    isFocused: Boolean,
    // Change the default to null so the modifier resolves it automatically!
    shape: CornerBasedShape? = null,
    color: Color? = null,
): Modifier = composed {
    if (!isFocused) return@composed this

    val theme = ShadcnTheme.LocalShadcnTheme.current
    val resolvedColor = color ?: theme.colors.borderFocus.copy(alpha = theme.ring.opacity)
    val width = theme.ring.width
    val offset = theme.ring.offset

    // 1. Resolve your group and theme corners completely automatically!
    val resolvedShape = shape ?: run {
        val defaultRingCorner = theme.shapes.lg
        val groupCorners = LocalGroupCorners.current

        RoundedCornerShape(
            topStart = groupCorners?.topStart ?: defaultRingCorner,
            topEnd = groupCorners?.topEnd ?: defaultRingCorner,
            bottomEnd = groupCorners?.bottomEnd ?: defaultRingCorner,
            bottomStart = groupCorners?.bottomStart ?: defaultRingCorner
        )
    }

    this
        .zIndex(1f)
        .drawWithContent {
            drawContent()

            val strokePx = width.toPx()
            val offsetPx = offset.toPx()
            val growth = offsetPx + strokePx / 2f

            inset(-growth) {
                val maxRadiusPx = minOf(size.width, size.height) / 2f

                // 2. Use the dynamically resolved shape corners
                val ts = resolvedShape.topStart.toPx(size, this)
                val te = resolvedShape.topEnd.toPx(size, this)
                val be = resolvedShape.bottomEnd.toPx(size, this)
                val bs = resolvedShape.bottomStart.toPx(size, this)

                fun corner(px: Float) = CornerRadius((px + growth).coerceAtMost(maxRadiusPx))

                val roundRect = RoundRect(
                    left = 0f, top = 0f, right = size.width, bottom = size.height,
                    topLeftCornerRadius = corner(ts),
                    topRightCornerRadius = corner(te),
                    bottomRightCornerRadius = corner(be),
                    bottomLeftCornerRadius = corner(bs),
                )

                val path = Path().apply { addRoundRect(roundRect) }
                drawPath(path, color = resolvedColor, style = Stroke(width = strokePx))
            }
        }
}