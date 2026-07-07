package io.github.ronjunevaldoz.shadcncompose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.navigation.CatalogNavHost
import io.github.ronjunevaldoz.shadcncompose.theme.LocalShadcnDarkTheme
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnAccent
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnBaseColor
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnRadius
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnStylePreset

@Composable
@Preview
fun App() {
    var darkModeOverride by remember { mutableStateOf<Boolean?>(null) }
    var stylePreset by remember { mutableStateOf(ShadcnStylePreset.Vega) }
    var baseColor by remember { mutableStateOf(ShadcnBaseColor.Zinc) }
    var accent by remember { mutableStateOf(ShadcnAccent.Base) }
    val isDarkMode = darkModeOverride ?: isSystemInDarkTheme()

    val isDark = isSystemInDarkTheme()

//    val resolvedColors = remember(baseColor, accent, isDark) {
//        val rawBaseColors = if (isDark) baseColor.dark else baseColor.light
//        accent.applyTo(base = rawBaseColors, dark = isDark)
//    }
    CompositionLocalProvider(LocalShadcnDarkTheme provides darkModeOverride) {
        ShadcnTheme(
            preset = stylePreset, // Snappy animations, tight padding metrics
            baseColor = baseColor, // Subtle cool zinc grays
            accent = accent, // Electric blue primary buttons and rings
            baseRadius = ShadcnRadius(4.dp), // Sleek, tighter corner shapes
            isDark = isDarkMode,
        ) {
            CatalogNavHost(
                isDarkMode = isDarkMode,
                onToggleDarkMode = { darkModeOverride = it },
                stylePreset = stylePreset,
                onStylePresetChange = { stylePreset = it },
                baseColor = baseColor,
                onBaseColorChange = { baseColor = it },
                accent = accent,
                onAccentChange = { accent = it },
            )
        }
    }
}
