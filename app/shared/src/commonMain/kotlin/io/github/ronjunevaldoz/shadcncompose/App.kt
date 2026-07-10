package io.github.ronjunevaldoz.shadcncompose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.navigation.CatalogNavHost
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnAccent
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnBaseColor
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnRadius
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnStylePreset

// The dark-mode toggle's effect is scoped to the detail content pane only -- chrome
// (top bar + sidebar) always follows the system's own light/dark setting and never
// reacts to it. This outer ShadcnTheme (isDark = isSystemInDarkTheme(), no override)
// governs the chrome; CatalogNavHost nests a second ShadcnTheme around just its content
// pane using darkModeOverride, so toggling only re-themes what's inside that pane.
@Composable
@Preview
fun App() {
    var darkModeOverride by remember { mutableStateOf<Boolean?>(null) }
    var stylePreset by remember { mutableStateOf(ShadcnStylePreset.Vega) }
    var baseColor by remember { mutableStateOf(ShadcnBaseColor.Zinc) }
    var accent by remember { mutableStateOf(ShadcnAccent.Base) }

    ShadcnTheme(
        // Snappy animations, tight padding metrics
        preset = stylePreset,
        // Subtle cool zinc grays
        baseColor = baseColor,
        // Electric blue primary buttons and rings
        accent = accent,
        // Sleek, tighter corner shapes
        baseRadius = ShadcnRadius(4.dp),
        isDark = isSystemInDarkTheme(),
    ) {
        CatalogNavHost(
            stylePreset = stylePreset,
            onStylePresetChange = { stylePreset = it },
            baseColor = baseColor,
            onBaseColorChange = { baseColor = it },
            accent = accent,
            onAccentChange = { accent = it },
            isDarkMode = darkModeOverride ?: isSystemInDarkTheme(),
            onToggleDarkMode = { darkModeOverride = it },
        )
    }
}
