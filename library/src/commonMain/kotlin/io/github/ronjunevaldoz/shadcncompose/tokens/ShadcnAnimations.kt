package io.github.ronjunevaldoz.shadcncompose.tokens

import androidx.compose.animation.core.TweenSpec
import androidx.compose.runtime.Immutable

@Immutable
data class ShadcnAnimations(
    val defaultTransition: TweenSpec<Float>,
    val visibilityTransition: TweenSpec<Float>,
)
