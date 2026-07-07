package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * A keyboard-shortcut label, e.g. `⌘K`. Matches real shadcn/ui's `kbd.tsx`
 * (`h-5 w-fit min-w-5 rounded-sm bg-muted px-1 text-xs font-medium`).
 *
 * Usage:
 * ```
 * ShadcnKbd("⌘")
 * ShadcnKbdGroup { ShadcnKbd("Ctrl"); ShadcnKbd("K") }
 * ```
 */
@Composable
fun ShadcnKbd(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .height(20.dp)
                .defaultMinSize(minWidth = 20.dp)
                .background(shadcnTheme.colors.muted, RoundedCornerShape(shadcnTheme.shapes.sm))
                .padding(horizontal = shadcnTheme.spacing.xxs),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShadcnText(text, style = ShadcnTextStyle.LabelSmall, muted = true)
    }
}

/** Groups multiple [ShadcnKbd] labels for a multi-key shortcut, e.g. `Ctrl` `K`. */
@Composable
fun ShadcnKbdGroup(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}
