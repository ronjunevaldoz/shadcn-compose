package io.github.ronjunevaldoz.shadcncompose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import io.github.ronjunevaldoz.shadcncompose.navigation.CatalogNavHost
import io.github.ronjunevaldoz.shadcncompose.theme.LocalShadcnDarkTheme
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnAccent
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnBaseColor
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnStylePreset

@Composable
@Preview
fun App() {
    var darkModeOverride by remember { mutableStateOf<Boolean?>(null) }
    var stylePreset by remember { mutableStateOf(ShadcnStylePreset.Vega) }
    var baseColor by remember { mutableStateOf(ShadcnBaseColor.Zinc) }
    var accent by remember { mutableStateOf(ShadcnAccent.Base) }
    val isDarkMode = darkModeOverride ?: isSystemInDarkTheme()

    val resolvedColors =
        accent.applyTo(base = if (isDarkMode) baseColor.dark else baseColor.light, dark = isDarkMode)

    CompositionLocalProvider(LocalShadcnDarkTheme provides darkModeOverride) {
        ShadcnTheme(
            darkTheme = isDarkMode,
            theme =
                if (isDarkMode) {
                    ShadcnTheme.dark(
                        colors = resolvedColors,
                        preset = stylePreset,
                    )
                } else {
                    ShadcnTheme.light(
                        colors = resolvedColors,
                        preset = stylePreset,
                    )
                },
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
