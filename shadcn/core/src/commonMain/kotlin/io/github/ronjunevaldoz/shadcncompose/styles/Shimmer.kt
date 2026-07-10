package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme

/**
 * A sweeping highlight animation, matching real shadcn/ui's `shimmer` utility
 * (`background-clip: text; animation: tw-shimmer ...`) -- used for "generating
 * response"/"thinking" text states. Real shadcn derives the highlight from the
 * element's own `currentColor`; this does the same by defaulting [color] to the
 * theme's `onSurface`, then compositing a moving gradient band over the already-drawn
 * content with [BlendMode.SrcAtop] so the highlight only ever paints where the content
 * itself is opaque -- exactly what `background-clip: text` does on the web.
 *
 * Usage:
 * ```
 * ShadcnText("Generating response…", muted = true, modifier = Modifier.shadcnShimmer())
 * ```
 */
fun Modifier.shadcnShimmer(
    enabled: Boolean = true,
    color: Color? = null,
    durationMillis: Int = 2000,
): Modifier =
    composed {
        if (!enabled) return@composed this

        val theme = ShadcnTheme.LocalShadcnTheme.current
        val highlightColor = color ?: theme.colors.onSurface
        val transition = rememberInfiniteTransition(label = "shadcn-shimmer")
        val progress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(animation = tween(durationMillis, easing = LinearEasing)),
            label = "shadcn-shimmer-progress",
        )

        // BlendMode.SrcAtop needs an offscreen compositing layer to composite only
        // against this modifier's own drawContent() output -- without it, the blend
        // reads whatever's already on the shared canvas (background/siblings drawn
        // earlier), so the sweep visibly tints/punches through the surrounding
        // background instead of stopping exactly at the text's own glyph shapes.
        graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                val bandWidth = size.width * 0.6f
                val travel = size.width + bandWidth
                val center = -bandWidth + progress * travel
                val brush =
                    Brush.linearGradient(
                        colors = listOf(Color.Transparent, highlightColor, Color.Transparent),
                        start = Offset(center - bandWidth / 2f, 0f),
                        end = Offset(center + bandWidth / 2f, 0f),
                    )
                drawRect(brush = brush, blendMode = BlendMode.SrcAtop)
            }
    }
