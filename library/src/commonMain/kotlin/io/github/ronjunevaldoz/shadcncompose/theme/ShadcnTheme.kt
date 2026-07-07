package io.github.ronjunevaldoz.shadcncompose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnColors
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnDarkColors
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnLightColors
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnRing
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnShapes
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnSpacing
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnTypography

@Immutable
data class ShadcnTheme(
    val colors: ShadcnColors,
    val typography: ShadcnTypography,
    val shapes: ShadcnShapes,
    val spacing: ShadcnSpacing,
    val ring: ShadcnRing, // TODO please have a second option, is this necessary here?
) {
    companion object {
        val LocalShadcnTheme: ProvidableCompositionLocal<ShadcnTheme> =
            staticCompositionLocalOf { light() }

        fun light(
            colors: ShadcnColors = ShadcnLightColors,
            typography: ShadcnTypography = ShadcnTypography(),
            shapes: ShadcnShapes = ShadcnShapes(),
            spacing: ShadcnSpacing = ShadcnSpacing(),
            ring: ShadcnRing = ShadcnRing(),
        ) = ShadcnTheme(colors, typography, shapes, spacing, ring)

        fun dark(
            colors: ShadcnColors = ShadcnDarkColors,
            typography: ShadcnTypography = ShadcnTypography(),
            shapes: ShadcnShapes = ShadcnShapes(),
            spacing: ShadcnSpacing = ShadcnSpacing(),
            ring: ShadcnRing = ShadcnRing(),
        ) = ShadcnTheme(colors, typography, shapes, spacing, ring)
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
