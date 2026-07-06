package io.github.ronjunevaldoz.shadcncompose.theme

import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.StyleScope
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnColors
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnShapes
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnSpacing
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnTypography

// The only correct way to read a CompositionLocal inside a Style block: Styles run
// outside Composition, so `ShadcnTheme.LocalShadcnTheme.current` would capture a stale
// value at definition time. StyleScope extends CompositionLocalAccessorScope, whose
// `currentValue` reads the CompositionLocal at consume time instead.

@OptIn(ExperimentalFoundationStyleApi::class)
val StyleScope.shadcnTheme: ShadcnTheme
    get() = ShadcnTheme.LocalShadcnTheme.currentValue

@OptIn(ExperimentalFoundationStyleApi::class)
val StyleScope.colors: ShadcnColors
    get() = ShadcnTheme.LocalShadcnTheme.currentValue.colors

@OptIn(ExperimentalFoundationStyleApi::class)
val StyleScope.typography: ShadcnTypography
    get() = ShadcnTheme.LocalShadcnTheme.currentValue.typography

@OptIn(ExperimentalFoundationStyleApi::class)
val StyleScope.shapes: ShadcnShapes
    get() = ShadcnTheme.LocalShadcnTheme.currentValue.shapes

@OptIn(ExperimentalFoundationStyleApi::class)
val StyleScope.spacing: ShadcnSpacing
    get() = ShadcnTheme.LocalShadcnTheme.currentValue.spacing
