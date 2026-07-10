package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.StyleScope
import androidx.compose.foundation.style.focused
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnThemeData
import io.github.ronjunevaldoz.tailwind.style.ringShadow

/**
 * shadcn's real focus indicator (`focus-visible:ring-<N> focus-visible:ring-ring/<opacity>`,
 * verified per-style against real `shadcn-ui/ui` CSS -- see `ShadcnStylePreset.kt`'s doc
 * comment) is a crisp ring drawn entirely outside the element, plus (where the element has a
 * border) `focus-visible:border-ring` recoloring that border.
 *
 * This is a `dropShadow`, not a `borderWidth` change -- deliberately, after actually measuring
 * the alternative: a `borderWidth(theme.ring.width)` swap on focus was tried and reverted,
 * because border width is part of the box model (`contentPadding` is defined as the gap
 * *between* border and content), so growing it on focus grows the whole element and pushes
 * every sibling after it. `dropShadow(radius = 0.dp, spread = ring.width)` renders as a
 * perfectly crisp, solid-looking ring -- not a blurred shadow, confirmed by screenshot test --
 * with zero effect on measured layout size, exactly matching real CSS `box-shadow`'s behavior
 * (which is what real shadcn's `ring-*` utilities actually compile to, not `border`).
 *
 * `spread = width + offset` folds shadcn's `ring-offset-*` (a transparent gap before the ring,
 * only present in the *retired* two-style `Default`/`New York` split -- see `ShadcnRing.kt`)
 * into the shadow's single `spread` value, since a nonzero offset would technically need a
 * second stacked shadow (an invisible spacer, then the colored ring) to render a true gap.
 * Every currently reachable `ShadcnStylePreset` has `offset = 0.dp`, so this simplification is
 * exact today; revisit if a future preset ever sets a nonzero offset.
 */
@OptIn(ExperimentalFoundationStyleApi::class)
fun ShadcnThemeData.focusRingShadow(color: Color = colors.borderFocus): Shadow =
    ringShadow(color = color.copy(alpha = ring.opacity), width = ring.width + ring.offset)

/**
 * Sets [shape] unconditionally *and* rings it on focus, in one call -- the fix for a real bug
 * class, not just a convenience wrapper. `dropShadow` carries no shape of its own ([ringShadow]
 * is a plain radius/spread/color [Shadow]); it always follows whatever this same `Style { }`
 * block's own `shape(...)` says, and falls back to a sharp `RectangleShape` if that call is
 * never made. `AccordionTrigger` shipped for a while calling
 * `focused { dropShadow(theme.focusRingShadow()) }` with no `shape(...)` anywhere in its
 * `Style { }` -- easy to miss because the component still *looked* right unfocused, and nothing
 * caught the omission until someone actually tabbed to it. Every other focusable component in
 * this library independently reinvented "remember to pair `shape(...)` with the ring" by hand;
 * this makes that pairing structural instead of a convention that can silently rot: call this
 * once and there is no longer a way to add a ring without a matching shape.
 *
 * No `theme: ShadcnThemeData` parameter -- reads [ShadcnTheme.LocalShadcnTheme] straight off
 * `this` via [StyleScope]'s own `CompositionLocalAccessorScope.currentValue`
 * ([androidx.compose.runtime.CompositionLocalAccessorScope], inherited through
 * `StyleScope : ... CustomStyleScope : Density, CompositionLocalAccessorScope`). This is *not*
 * the "read a CompositionLocal inside a captured lambda" anti-pattern flagged elsewhere in this
 * codebase (`ShadcnInputGroup.kt`/`ShadcnTextField.kt`'s comments on capturing `shadcnTheme`
 * inside a `remember { Style { ... } }` closure, which freezes at whatever theme was active on
 * first composition): `.currentValue` is a real snapshot-tracked read that `Style { }` blocks are
 * explicitly documented to support -- see `Style`'s own KDoc point 2: "CompositionLocals can be
 * read inside of them... without adding to the capture scope of the lambda."
 *
 * Also swaps `borderColor` to `theme.colors.borderFocus` on focus, where present -- a pure
 * draw-time color change, unlike `borderWidth`, so it doesn't reflow layout either.
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
 *
 * `theme.ring.enabled` gates the ring itself, not `shape(shape)` -- flipping it off in a
 * `ShadcnThemeData` copy silences the ring on every focusable component at once without
 * reopening the shape-pairing bug this function exists to prevent.
 */
@OptIn(ExperimentalFoundationStyleApi::class)
fun StyleScope.focusRing(shape: Shape) {
    val theme = ShadcnTheme.LocalShadcnTheme.currentValue
    shape(shape)
    if (!theme.ring.enabled) return
    focused {
        borderColor(theme.colors.borderFocus)
        dropShadow(theme.focusRingShadow())
    }
}
