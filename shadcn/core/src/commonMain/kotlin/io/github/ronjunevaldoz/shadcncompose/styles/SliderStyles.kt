@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.hovered
import androidx.compose.runtime.Composable

// Matches shadcn/ui's real slider.tsx: track bg-muted, range bg-primary, thumb
// border border-primary bg-background, ring on *both* hover and focus (unlike
// Button, which only rings on focus) -- real shadcn's own `hover:ring-4
// focus-visible:ring-4` on this specific component, two separate rules with the
// same properties (we don't have a per-component ring width override, so this
// still uses the shared theme.ring.width).
// disabled dimming is a single Modifier.alpha() on the whole slider in ShadcnSlider.kt,
// not a per-style disabledDim() here -- the track/range/thumb must stay fully opaque
// relative to *each other* (so the thumb still cleanly occludes the fill beneath it)
// and only the whole composited control fades against the page behind it, matching
// how CSS's disabled:opacity-50 behaves on a real DOM subtree. disabledDim() on each
// of these independently was tried first and made the thumb's translucent background
// blend with the also-translucent fill sitting behind it instead of occluding it.
@Composable
fun rememberSliderTrackStyle(): Style =
    rememberShadcnStyle {
        Style {
            background(colors.muted)
            shape(RoundedCornerShape(shapes.full))
        }
    }

@Composable
fun rememberSliderRangeStyle(): Style =
    rememberShadcnStyle {
        Style {
            background(colors.primary)
            shape(RoundedCornerShape(shapes.full))
        }
    }

@Composable
fun rememberSliderThumbStyle(): Style =
    rememberShadcnStyle {
        Style {
            background(colors.background)
            border(colors.primary)
            hovered { dropShadow(focusRingShadow()) }
            focusRing(RoundedCornerShape(shapes.full))
        }
    }
