package io.github.ronjunevaldoz.shadcncompose.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class ShadcnShapes(
    val none: Dp = 0.dp,
    val xs: Dp = 2.dp,
    val sm: Dp = 4.dp,
    val md: Dp = 6.dp,
    val lg: Dp = 8.dp,
    val xl: Dp = 12.dp,
    val xxl: Dp = 16.dp,
    val full: Dp = 9999.dp,
) {
    companion object {
        /**
         * Derives a scale from one base radius, mirroring real shadcn/ui v4's actual
         * `--radius-*` formula (verified against `apps/v4/app/globals.css`, not memory):
         * `sm = base * 0.6`, `md = base * 0.8`, `lg = base` (the 1.0x anchor -- real
         * shadcn's `--radius-lg: var(--radius)`), `xl = base * 1.4`, `2xl = base * 1.8`.
         * `none` and `full` are never derived -- real shadcn's `rounded-none`/`rounded-full`
         * aren't part of the `--radius` scale either, they're separate fixed utilities.
         *
         * Only use this for presets with no distinct shape "personality" of their own
         * (currently Vega/Nova). Presets like Lyra ("boxy and sharp" -- all corners
         * pinned to 0 regardless of any base value) or Mira (an independently-tuned
         * compact scale) are not expressible as a scalar multiple of one base radius and
         * must keep their own explicit [ShadcnShapes] constructor call.
         */
        fun fromBaseRadius(baseRadius: Dp): ShadcnShapes =
            ShadcnShapes(
                xs = baseRadius * 0.4f,
                sm = baseRadius * 0.6f,
                md = baseRadius * 0.8f,
                lg = baseRadius,
                xl = baseRadius * 1.4f,
                xxl = baseRadius * 1.8f,
            )
    }
}
