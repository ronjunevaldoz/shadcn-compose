package io.github.ronjunevaldoz.shadcncompose.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * shadcn's real focus ring (`focus-visible:ring-<N> focus-visible:ring-ring/<opacity>`)
 * hardcodes width and opacity as literal Tailwind classes repeated in every component --
 * shadcn/ui's own theming only exposes the ring *color* (`--ring`) as an overridable token.
 * We go one step further and make width/opacity/offset themeable too, consistent with how every
 * other token in this library (colors, shapes, spacing) is already override-able.
 *
 * Real shadcn genuinely varies width/opacity **per named style** -- verified directly against
 * `shadcn-ui/ui`'s `apps/v4/registry/styles/style-<name>.css` files (2026-07-09), not assumed.
 * `ShadcnStylePreset` carries the real per-style values; this class's own defaults (below)
 * happen to equal Vega's (the default preset), matching the values used when no preset-specific
 * override applies. `offset` is 0 for all 8 real styles -- no `ring-offset-*` class appears
 * anywhere in any of them.
 *
 * [offset] is the gap between the component's own edge and the ring. Real shadcn's *retired*
 * two-style split (`shadcn init`'s "Default" vs "New York") differed here specifically --
 * Default used `ring-2 ring-offset-2`, New York used `ring-1` with no offset -- but that split
 * is unrelated to the current 8-style system and its real per-style ring values above; a prior
 * version of this project confused the two axes, modeling the retired split as `Default`/
 * `NewYork` companion presets and (briefly, wrongly) auto-applying them to two of the eight
 * `ShadcnStylePreset` entries. Removed entirely once neither axis check panned out for those
 * specific values -- the *separate*, real per-style axis discovered afterward is what
 * `ShadcnStylePreset.ring` now carries instead.
 */
@Immutable
data class ShadcnRing(
    // Vega's real values (the default preset): ring-[3px] ring-ring/50, no offset.
    // width=3.dp uses the same numeral as real shadcn's 3px (this project's px-to-dp
    // convention -- see AGENTS.md's "Component styling rules" #0 -- not a physically-
    // accurate unit conversion). opacity=0.5f, not 1.0f -- the /50 above is
    // Tailwind's opacity modifier; a prior version of this default hardcoded 1.0f (fully
    // opaque), directly contradicting the real class this doc comment itself cites,
    // making every focus ring look noticeably heavier/more solid than real shadcn's
    // actual translucent one.
    val width: Dp = 3.dp,
    val opacity: Float = 0.5f,
    val offset: Dp = 0.dp,
    // Global kill switch for `focusRing()`, checked by every focusable component in the
    // library. `focusRing()` still calls `shape(shape)` unconditionally when this is false,
    // so the shape-pairing fix (see FocusRing.kt's doc comment) stays intact either way --
    // only the ring itself is skipped. Defaults to off: the ring reads as visual noise on
    // most non-input controls (buttons, switches, tabs, ...) outside of active keyboard
    // navigation, and a consumer can opt back in with `ring = theme.ring.copy(enabled =
    // true)`. Text inputs are unaffected either way -- `focusRingAlways()`
    // (`ShadcnTextField`/`ShadcnInputGroup`/`ShadcnInputOTP`) never reads this flag, since
    // the ring is the primary cue that a field is receiving keystrokes.
    val enabled: Boolean = false,
)
