package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.StyleScope
import androidx.compose.foundation.style.pressed

/**
 * A tactile "push" affordance: shrinks the component slightly while held down, springing
 * back on release. Real shadcn/ui's `button.tsx` has no active-state transform (verified
 * directly against the source CSS -- there's no `active:scale-*` class anywhere in it), so
 * this is a deliberate enhancement over the literal port, not a translated class -- the
 * same kind of tactile feedback native platforms give for free (iOS `UIButton` highlight
 * scale, Material's press ripple) and that a flat color-only hover/press state lacks.
 *
 * Wrapped in `animate { }` (Style's own spring-based default, per its own KDoc point 4) so
 * the scale eases in on press and springs back on release instead of snapping instantly --
 * an unanimated 1.0 -> 0.97 -> 1.0 jump reads as a rendering glitch, not a press.
 */
@OptIn(ExperimentalFoundationStyleApi::class)
fun StyleScope.pressedScale(scale: Float = 0.97f) {
    animate {
        pressed { scale(scale) }
    }
}
