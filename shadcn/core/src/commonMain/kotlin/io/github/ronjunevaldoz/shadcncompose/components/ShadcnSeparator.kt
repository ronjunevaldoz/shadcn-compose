package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

enum class ShadcnSeparatorOrientation { Horizontal, Vertical }

/**
 * A thin dividing line. Matches real shadcn/ui's `separator.tsx`: `h-px w-full` when
 * horizontal, `h-full w-px` when vertical, painted with the `border` token.
 *
 * Usage:
 * ```
 * ShadcnSeparator()
 * ShadcnSeparator(orientation = ShadcnSeparatorOrientation.Vertical)
 * ```
 */
@Composable
fun ShadcnSeparator(
    modifier: Modifier = Modifier,
    orientation: ShadcnSeparatorOrientation = ShadcnSeparatorOrientation.Horizontal,
) {
    val sizeModifier =
        when (orientation) {
            ShadcnSeparatorOrientation.Horizontal -> Modifier.fillMaxWidth().height(1.dp)
            ShadcnSeparatorOrientation.Vertical -> Modifier.fillMaxHeight().width(1.dp)
        }
    Box(
        modifier = modifier.then(sizeModifier).background(shadcnTheme.colors.border),
    )
}
