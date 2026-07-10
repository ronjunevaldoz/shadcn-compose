package io.github.ronjunevaldoz.shadcncompose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnAccent
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnAnimations
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnBaseColor
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnColors
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnIconStyles
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnRadius
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnRing
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnShapes
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnSpacing
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnStylePreset
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnTypography

/**
 * 1. The Global Design System State Container.
 * Packs up every system primitive neatly to maintain single-token parity with Tailwind v4.
 */
@Immutable
data class ShadcnThemeData(
    val colors: ShadcnColors,
    val typography: ShadcnTypography,
    val shapes: ShadcnShapes,
    val spacing: ShadcnSpacing,
    val ring: ShadcnRing,
    val animations: ShadcnAnimations,
    val icons: ShadcnIconStyles,
    val baseRadius: ShadcnRadius,
    val activePreset: ShadcnStylePreset,
    val activeBaseColor: ShadcnBaseColor,
    val activeAccent: ShadcnAccent,
)

/**
 * 2. The Singleton Composition Local Access Point.
 */
object ShadcnTheme {
    val current: ShadcnThemeData
        @Composable get() = LocalShadcnTheme.current

    internal val LocalShadcnTheme: ProvidableCompositionLocal<ShadcnThemeData> =
        staticCompositionLocalOf {
            // Provide a secure default layout fallback to protect the canvas engine
            val defaultPreset = ShadcnStylePreset.Vega
            val defaultBase = ShadcnBaseColor.Neutral
            val defaultAccent = ShadcnAccent.Base

            ShadcnThemeData(
                colors = defaultBase.light,
                typography = defaultPreset.typography,
                shapes = defaultPreset.shapes,
                spacing = defaultPreset.spacing,
                ring = defaultPreset.ring,
                animations = defaultPreset.animations,
                icons = defaultPreset.icons,
                baseRadius = ShadcnRadius(6.dp),
                activePreset = defaultPreset,
                activeBaseColor = defaultBase,
                activeAccent = defaultAccent,
            )
        }
}

/**
 * In-app dark-mode override set by a user settings toggle. Null follows the system.
 */
val LocalShadcnDarkTheme = compositionLocalOf<Boolean?> { null }

@Composable
fun ShadcnTheme(
    preset: ShadcnStylePreset = ShadcnStylePreset.Vega,
    baseColor: ShadcnBaseColor = ShadcnBaseColor.Neutral,
    accent: ShadcnAccent = ShadcnAccent.Base,
    isDark: Boolean = isSystemInDarkTheme(),
    baseRadius: ShadcnRadius = ShadcnRadius(6.dp),
    // Defaults to the selected preset's own ring (verified per-style in real
    // shadcn-ui/ui CSS -- see ShadcnStylePreset.kt's doc comment), but stays
    // independently overridable -- same pattern as `baseRadius`.
    ring: ShadcnRing = preset.ring,
    content: @Composable () -> Unit,
) {
    // Dynamically compile tokens and resolve accents ONLY when parameters change
    val configuredThemeData =
        remember(preset, baseColor, accent, isDark, baseRadius, ring) {
            // A. Extract the raw base color sheet depending on dark/light status
            val rawBaseColors = if (isDark) baseColor.dark else baseColor.light

            // B. Cascade accent overrides over the base layer safely
            val finalizedColors = accent.applyTo(base = rawBaseColors, dark = isDark)

            ShadcnThemeData(
                colors = finalizedColors,
                typography = preset.typography,
                shapes = preset.shapes,
                spacing = preset.spacing,
                ring = ring,
                animations = preset.animations,
                icons = preset.icons,
                baseRadius = baseRadius,
                activePreset = preset,
                activeBaseColor = baseColor,
                activeAccent = accent,
            )
        }

    CompositionLocalProvider(
        ShadcnTheme.LocalShadcnTheme provides configuredThemeData,
        content = content,
    )
}

val shadcnTheme: ShadcnThemeData
    @Composable get() = ShadcnTheme.LocalShadcnTheme.current
