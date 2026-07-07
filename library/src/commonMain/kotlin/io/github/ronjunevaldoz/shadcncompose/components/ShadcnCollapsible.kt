package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * An expand/collapse container. Matches real shadcn/ui's `collapsible.tsx` -- a bare
 * state/animation primitive with no styling of its own (real shadcn's version has none
 * either; visuals come entirely from what you put in [trigger]/[content]).
 *
 * Usage:
 * ```
 * var expanded by remember { mutableStateOf(false) }
 * ShadcnCollapsible(
 *     expanded = expanded,
 *     onExpandedChange = { expanded = it },
 *     trigger = { isExpanded, toggle -> ShadcnButton(onClick = toggle) { ShadcnText(if (isExpanded) "Hide" else "Show") } },
 * ) {
 *     ShadcnText("Hidden content")
 * }
 * ```
 */
@Composable
fun ShadcnCollapsible(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    trigger: @Composable (expanded: Boolean, toggle: () -> Unit) -> Unit,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        trigger(expanded) { onExpandedChange(!expanded) }
        AnimatedVisibility(visible = expanded) {
            content()
        }
    }
}
