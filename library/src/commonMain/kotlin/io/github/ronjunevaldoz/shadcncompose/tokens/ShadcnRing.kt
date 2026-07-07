package io.github.ronjunevaldoz.shadcncompose.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * shadcn's real focus ring (`focus-visible:ring-[3px] focus-visible:ring-ring/50`) hardcodes
 * both the width and the opacity as literal Tailwind classes repeated in every component --
 * shadcn/ui's own theming only exposes the ring *color* (`--ring`) as an overridable token.
 * We go one step further and make width/opacity/offset themeable too, consistent with how every
 * other token in this library (colors, shapes, spacing) is already override-able.
 *
 * [offset] is the gap between the component's own edge and the ring -- shadcn/ui's older
 * two-style split (`shadcn init`'s "Default" vs "New York") differed here specifically:
 * Default used `ring-2 ring-offset-2` (a 2dp ring, held 2dp off the edge), while New York
 * used `ring-1` with no offset at all (a thinner ring flush against the edge). The current/
 * unified shadcn style (this class's own defaults) uses neither split -- `ring-[3px]`, no
 * offset -- so [Default] and [NewYork] below are opt-in presets for either historical style,
 * not what [ShadcnTheme] applies unless you pass one explicitly.
 */
@Immutable
data class ShadcnRing(
    val width: Dp = 3.dp,
    val opacity: Float = 0.5f,
    val offset: Dp = 0.dp,
) {
    companion object {
        /** shadcn/ui's classic "Default" style: `ring-2 ring-offset-2`. */
        val Default = ShadcnRing(width = 2.dp, opacity = 0.5f, offset = 2.dp)

        /** shadcn/ui's classic "New York" style: `ring-1`, no offset. */
        val NewYork = ShadcnRing(width = 1.dp, opacity = 0.5f, offset = 0.dp)
    }
}
