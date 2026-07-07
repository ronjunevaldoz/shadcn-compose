package io.github.ronjunevaldoz.shadcncompose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnAnimations
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnColors
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnDarkColors
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnIconStyles
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnLightColors
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnRadius
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnRing
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnShapes
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnSpacing
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnStylePreset
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnTypography

@Immutable
data class ShadcnTheme(
    val colors: ShadcnColors,
    val typography: ShadcnTypography,
    val shapes: ShadcnShapes,
    val spacing: ShadcnSpacing,
    val ring: ShadcnRing,
    val animations: ShadcnAnimations,
    val icons: ShadcnIconStyles,
    val baseRadius: ShadcnRadius
) {
    companion object {
        val LocalShadcnTheme: ProvidableCompositionLocal<ShadcnTheme> =
            staticCompositionLocalOf { light() }


        fun light(
            colors: ShadcnColors = ShadcnLightColors,
            preset: ShadcnStylePreset = ShadcnStylePreset.Vega,
            baseRadius: ShadcnRadius = ShadcnRadius(6.dp),
        ) = ShadcnTheme(
            colors = colors,
            typography = preset.typography,
            shapes = preset.shapes,
            spacing = preset.spacing,
            ring = preset.ring,
            animations = preset.animations,
            icons = preset.icons,
            baseRadius = baseRadius,
        )

        fun dark(
            colors: ShadcnColors = ShadcnDarkColors,
            preset: ShadcnStylePreset = ShadcnStylePreset.Vega,
            baseRadius: ShadcnRadius = ShadcnRadius(6.dp),
        ) = ShadcnTheme(
            colors = colors,
            typography = preset.typography,
            shapes = preset.shapes,
            spacing = preset.spacing,
            ring = preset.ring,
            animations = preset.animations,
            icons = preset.icons,
            baseRadius = baseRadius,
        )
    }
}

/**
 * In-app dark-mode override set by a user settings toggle. Null follows the system.
 */
val LocalShadcnDarkTheme = compositionLocalOf<Boolean?> { null }

@Composable
fun ShadcnTheme(
    darkTheme: Boolean = LocalShadcnDarkTheme.current ?: isSystemInDarkTheme(),
    theme: ShadcnTheme = if (darkTheme) ShadcnTheme.dark() else ShadcnTheme.light(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        ShadcnTheme.LocalShadcnTheme provides theme,
        content = content,
    )
}

val shadcnTheme: ShadcnTheme
    @Composable get() = ShadcnTheme.LocalShadcnTheme.current
