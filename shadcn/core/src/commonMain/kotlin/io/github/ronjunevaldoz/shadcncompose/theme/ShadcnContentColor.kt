package io.github.ronjunevaldoz.shadcncompose.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The content color a [io.github.ronjunevaldoz.shadcncompose.components.ShadcnIcon] should
 * inherit from its nearest ancestor, mirroring the ambient `contentColor` that the Compose
 * Foundation Style API already gives [io.github.ronjunevaldoz.shadcncompose.components.ShadcnText]
 * -- that mechanism has no public reader (`StyleScope.contentColor` is write-only, resolved
 * internally by the Style API's own text-rendering pipeline), so non-text content can't hook
 * into it directly. Components that already call `contentColor(...)` in their `Style { }` block
 * (`ShadcnButton`, etc.) additionally provide this CompositionLocal with the same resolved color
 * so [ShadcnIcon] can match without every call site passing an explicit `tint`.
 *
 * Defaults to [Color.Unspecified] -- [ShadcnIcon] falls back to `colors.onSurface` when nothing
 * provides this, the same default [io.github.ronjunevaldoz.shadcncompose.components.ShadcnText]
 * uses.
 */
val LocalShadcnContentColor = compositionLocalOf { Color.Unspecified }
