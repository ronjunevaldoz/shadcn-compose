package io.github.ronjunevaldoz.shadcncompose.styles

import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.StyleScope
import androidx.compose.foundation.style.disabled

/**
 * Real shadcn/ui's shared `disabled:opacity-50` treatment -- every disableable variant
 * across this library dims to the same 50% alpha, so this is the one place that value
 * lives instead of being repeated verbatim in every `Style { }` block.
 */
@OptIn(ExperimentalFoundationStyleApi::class)
fun StyleScope.disabledDim() {
    disabled { alpha(0.5f) }
}
