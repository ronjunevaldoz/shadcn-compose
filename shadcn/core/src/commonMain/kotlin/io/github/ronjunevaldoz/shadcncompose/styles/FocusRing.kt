package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.StyleScope
import androidx.compose.foundation.style.focused
import androidx.compose.ui.graphics.Shape
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme

/**
 * Sets [shape] unconditionally *and* rings it on focus, in one call -- the fix for a real bug
 * class, not just a convenience wrapper. Every focusable component in this library used to
 * reinvent "remember to pair `shape(...)` with the focus indicator" by hand; this makes that
 * pairing structural instead of a convention that can silently rot: call this once and there is
 * no longer a way to add a ring without a matching shape.
 *
 * The ring itself is a real `borderColor`/`borderWidth` swap on focus, not a `dropShadow`. Reads
 * [ShadcnTheme.LocalShadcnTheme] straight off `this` via [StyleScope]'s own
 * `CompositionLocalAccessorScope.currentValue` ([androidx.compose.runtime.CompositionLocalAccessorScope],
 * inherited through `StyleScope : ... CustomStyleScope : Density, CompositionLocalAccessorScope`)
 * instead of taking a `theme` parameter -- a real snapshot-tracked read that `Style { }` blocks
 * are documented to support, not the "capture `shadcnTheme` inside a `remember { Style { } }`
 * closure" anti-pattern flagged elsewhere in this codebase.
 *
 * Before (the actual shipped bug, `ShadcnAccordion.kt`):
 * ```
 * Style {
 *     background(theme.colors.background)
 *     focused { dropShadow(theme.focusRingShadow()) }
 * }
 * ```
 *
 * After:
 * ```
 * Style {
 *     background(theme.colors.background)
 *     focusRing(RoundedCornerShape(theme.shapes.md))
 * }
 * ```
 */
@OptIn(ExperimentalFoundationStyleApi::class)
fun StyleScope.focusRing(shape: Shape) {
    val theme = ShadcnTheme.LocalShadcnTheme.currentValue
    shape(shape)
    focused {
        borderColor(theme.colors.borderFocus)
        borderWidth(theme.ring.width)
    }
}
