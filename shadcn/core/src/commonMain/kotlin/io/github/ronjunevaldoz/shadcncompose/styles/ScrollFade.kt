package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class ShadcnScrollFadeOrientation { Vertical, Horizontal }

/** Whether the leading edge should fade -- there's more content to scroll back to. */
internal fun shouldFadeLeadingEdge(scrollValue: Int): Boolean = scrollValue > 0

/** Whether the trailing edge should fade -- there's more content to scroll forward to. */
internal fun shouldFadeTrailingEdge(
    scrollValue: Int,
    maxValue: Int,
): Boolean = scrollValue < maxValue

/** Clamps the requested fade size so opposite-edge fades never overlap on a short container. */
internal fun clampedFadeSizePx(
    requestedPx: Float,
    containerExtentPx: Float,
): Float = requestedPx.coerceIn(0f, containerExtentPx / 2f)

/**
 * A scroll-position-aware fade at the edges of a scrollable container, matching real
 * shadcn/ui's `scroll-fade` utility (`mask-image` + `animation-timeline: scroll(self)`).
 * At rest, the leading edge (already scrolled past) stays crisp while the trailing edge
 * (more content ahead) fades -- hinting overflow without a hard cut. Both edges fade
 * mid-scroll; the trailing edge sharpens once the container reaches its end.
 *
 * Unlike a scrim overlay, this genuinely fades the content's own alpha to transparent
 * at the edges ([BlendMode.DstIn] against a [Brush] alpha ramp) -- the same effect as
 * CSS `mask-image`, so it looks correct over any background, not just a known solid
 * color. Requires an offscreen compositing layer for [BlendMode.DstIn] to composite
 * correctly, which this modifier adds itself.
 *
 * Usage:
 * ```
 * val scrollState = rememberScrollState()
 * Column(Modifier.verticalScroll(scrollState).shadcnScrollFade(scrollState)) { ... }
 * ```
 */
fun Modifier.shadcnScrollFade(
    scrollState: ScrollState,
    orientation: ShadcnScrollFadeOrientation = ShadcnScrollFadeOrientation.Vertical,
    edgeSize: Dp = 24.dp,
): Modifier =
    composed {
        val density = LocalDensity.current
        val requestedFadePx = with(density) { edgeSize.toPx() }
        val isVertical = orientation == ShadcnScrollFadeOrientation.Vertical

        this
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                if (scrollState.maxValue <= 0) return@drawWithContent

                val extent = if (isVertical) size.height else size.width
                val fadePx = clampedFadeSizePx(requestedFadePx, extent)
                if (fadePx <= 0f) return@drawWithContent

                if (shouldFadeLeadingEdge(scrollState.value)) {
                    val brush =
                        if (isVertical) {
                            Brush.verticalGradient(listOf(Color.Transparent, Color.Black), startY = 0f, endY = fadePx)
                        } else {
                            Brush.horizontalGradient(listOf(Color.Transparent, Color.Black), startX = 0f, endX = fadePx)
                        }
                    val fadeSize = if (isVertical) Size(size.width, fadePx) else Size(fadePx, size.height)
                    drawRect(brush = brush, blendMode = BlendMode.DstIn, size = fadeSize)
                }

                if (shouldFadeTrailingEdge(scrollState.value, scrollState.maxValue)) {
                    val topLeft = if (isVertical) Offset(0f, size.height - fadePx) else Offset(size.width - fadePx, 0f)
                    val brush =
                        if (isVertical) {
                            Brush.verticalGradient(
                                listOf(Color.Black, Color.Transparent),
                                startY = size.height - fadePx,
                                endY = size.height,
                            )
                        } else {
                            Brush.horizontalGradient(
                                listOf(Color.Black, Color.Transparent),
                                startX = size.width - fadePx,
                                endX = size.width,
                            )
                        }
                    val fadeSize = if (isVertical) Size(size.width, fadePx) else Size(fadePx, size.height)
                    drawRect(brush = brush, blendMode = BlendMode.DstIn, topLeft = topLeft, size = fadeSize)
                }
            }
    }
