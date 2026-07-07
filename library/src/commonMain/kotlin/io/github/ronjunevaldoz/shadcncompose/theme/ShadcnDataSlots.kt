package io.github.ronjunevaldoz.shadcncompose.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Emulates Tailwind v4's data-slot architecture.
 * Safely passes compound layout sizes down from parent views to child primitives
 * without cluttering component constructors.
 */
@Immutable
data class ShadcnDataSlots(
    val iconSize: Dp = 16.dp,
    val paddingHorizontal: Dp = 12.dp,
    val paddingVertical: Dp = 6.dp
)

/**
 * The CompositionLocal cascading engine hook.
 * Initialized to null because child primitives should automatically look to their
 * parent container for structural overrides, falling back to theme defaults if absent.
 */
val LocalShadcnDataSlots = compositionLocalOf<ShadcnDataSlots?> { null }