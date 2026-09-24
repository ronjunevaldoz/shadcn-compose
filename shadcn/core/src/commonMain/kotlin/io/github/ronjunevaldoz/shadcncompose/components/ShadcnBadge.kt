package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.CompositionLocalProvider
import io.github.ronjunevaldoz.shadcncompose.styles.BadgeVariant
import io.github.ronjunevaldoz.shadcncompose.styles.contentColor
import io.github.ronjunevaldoz.shadcncompose.styles.rememberStyle
import io.github.ronjunevaldoz.shadcncompose.theme.LocalShadcnContentColor
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * Label/tag component.
 *
 * Usage:
 * ```
 * ShadcnBadge { ShadcnText("New") }
 * ShadcnBadge(variant = BadgeVariant.Destructive) { ShadcnText("Error") }
 * ```
 */
@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
fun ShadcnBadge(
    modifier: Modifier = Modifier,
    variant: BadgeVariant = BadgeVariant.Default,
    style: Style = Style,
    content: @Composable () -> Unit,
) {
    // Non-interactive — no InteractionSource events are ever emitted, but the
    // StyleState API still requires one to be supplied at construction time.
    val styleState = remember { MutableStyleState(MutableInteractionSource()) }

    Box(
        modifier = modifier.styleable(styleState, variant.rememberStyle(), style),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalShadcnContentColor provides variant.contentColor(shadcnTheme.colors)) {
            content()
        }
    }
}
