package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Constrains its content to a fixed width/height ratio. Matches real shadcn/ui's
 * `aspect-ratio.tsx`, which is a thin Radix wrapper with no styling of its own --
 * `Modifier.aspectRatio` already does exactly that in Compose, so this is a thin
 * pass-through kept for API parity with the rest of the catalog.
 *
 * Usage:
 * ```
 * ShadcnAspectRatio(ratio = 16f / 9f) {
 *     Image(painter, contentDescription = null, modifier = Modifier.fillMaxSize())
 * }
 * ```
 */
@Composable
fun ShadcnAspectRatio(
    ratio: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.aspectRatio(ratio)) {
        content()
    }
}
