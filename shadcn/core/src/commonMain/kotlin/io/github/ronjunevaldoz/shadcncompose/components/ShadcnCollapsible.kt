package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * An expand/collapse container. Matches real shadcn/ui's `collapsible.tsx` -- a bare
 * state/animation primitive with no styling of its own (real shadcn's version has none
 * either; visuals come entirely from what you put in [trigger]/[content]).
 *
 * One deliberate deviation: a small default gap between [trigger] and [content],
 * unlike [ShadcnSeparator]'s zero-margin-by-design. Real shadcn can rely on the
 * caller's own `className="mt-2"` on their content element, but Compose's default
 * `AnimatedVisibility` enter transition (`fadeIn() + expandVertically()`) finishes its
 * height animation well before its opacity animation -- verified by freezing the
 * animation clock mid-transition (see the deleted CollapsibleAnimationSpikeTest) -- so
 * for a few frames the still-fading-in content sits at its full final height flush
 * against the trigger, reading as a visual overlap glitch rather than a fade. Since
 * *every* caller would otherwise need to remember to pad their own content to avoid
 * that glitch (unlike Separator's zero-margin, which has legitimate uses), this bakes
 * in a small structural gap instead of leaving it fully bare.
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
        // The gap lives *inside* AnimatedVisibility, not as Arrangement.spacedBy on
        // this Column: spacedBy would reserve its gap unconditionally based on child
        // count, leaving a dangling empty gap below the trigger even fully collapsed
        // (AnimatedVisibility measures 0x0 once its exit animation settles, but it's
        // still "a child" as far as spacedBy is concerned). Padding inside the
        // animating subtree collapses away together with the content instead.
        AnimatedVisibility(visible = expanded) {
            Box(modifier = Modifier.padding(top = shadcnTheme.spacing.sm)) {
                content()
            }
        }
    }
}
