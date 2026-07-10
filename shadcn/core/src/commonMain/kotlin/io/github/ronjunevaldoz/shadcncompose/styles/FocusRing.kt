package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnThemeData
import io.github.ronjunevaldoz.tailwind.style.ringShadow

/**
 * shadcn's real focus indicator (`focus-visible:ring-<N> focus-visible:ring-ring/<opacity>`,
 * verified per-style against real `shadcn-ui/ui` CSS -- see `ShadcnStylePreset.kt`'s doc
 * comment) is a crisp ring drawn entirely outside the element, plus (where the element has a
 * border) `focus-visible:border-ring` recoloring that border. Built via the Style API's own
 * `dropShadow` inside each component's `focused { }` block -- called directly alongside the
 * existing `borderColor(...)` swap, not a separate `Modifier`.
 *
 * A prior version of this used a hand-rolled `Modifier.drawWithContent` + manual per-corner
 * `RoundRect` reimplementation instead, justified by a claim that `dropShadow` "always
 * rasterizes through an offscreen bitmap... visibly blurs a ring this thin" even at
 * `radius = 0.dp`. That claim was never actually re-verified and turned out false: a direct
 * screenshot test of `dropShadow(radius = 0.dp, spread = 3.dp)` showed a perfectly crisp
 * ring (only ordinary single-pixel edge anti-aliasing, the same any vector draw call has),
 * with zero effect on the element's measured layout size -- exactly matching real CSS
 * `box-shadow`. The custom modifier's ~90 lines were solving a problem that didn't exist.
 *
 * `spread = width + offset` folds shadcn's `ring-offset-*` (a transparent gap before the
 * ring, only present in the *retired* two-style `Default`/`New York` split -- see
 * `ShadcnRing.kt`) into the shadow's single `spread` value, since a nonzero offset would
 * technically need a second stacked shadow (an invisible spacer, then the colored ring) to
 * render a true gap. Every currently reachable `ShadcnStylePreset` has `offset = 0.dp`, so
 * this simplification is exact today; revisit if a future preset ever sets a nonzero offset.
 *
 * `dropShadow` always follows the *same* `Style { }` block's own `shape(...)` -- exactly like
 * real CSS `box-shadow` always follows the element's own `border-radius` -- so `ShadcnButton`/
 * `ShadcnToggle`'s `LocalGroupCorners`-aware shape resolution (for `ShadcnButtonGroup`/
 * `ShadcnToggleGroup` flush corners) automatically applies to the ring too as long as it's
 * resolved into that same `shape(...)` call; no separate shape parameter needed here.
 *
 * Built via `tailwind-style`'s [ringShadow] rather than constructing the [Shadow] by hand --
 * that function exists specifically for callers like this one that need the raw ring value to
 * drop inside their own state block (`focused { }`), instead of `tailwind-style`'s `ringStyle()`
 * which applies unconditionally and has no per-state variant.
 */
@OptIn(ExperimentalFoundationStyleApi::class)
fun ShadcnThemeData.focusRingShadow(color: Color = colors.borderFocus): Shadow =
    ringShadow(color = color.copy(alpha = ring.opacity), width = ring.width + ring.offset)
