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

// Two axes, two different scopes:
// - isDark (light/light-with-manual-override) applies everywhere -- top bar, sidebar,
//   and content pane all share the same isDarkMode below, since dark/light is a real
//   accessibility preference, not a "preview" setting.
// - Style/Base Color/Accent apply to the detail content pane ONLY. This outer
//   ShadcnTheme deliberately uses fixed constants (not the live stylePreset/baseColor/
//   accent state), so the top bar keeps a stable brand identity regardless of what a
//   reader is previewing; CatalogNavHost nests a second ShadcnTheme (same isDarkMode,
//   but the live picker values) around the sidebar + content pane.
@Composable
@Preview
fun App() {
    var darkModeOverride by remember { mutableStateOf<Boolean?>(null) }
    var stylePreset by remember { mutableStateOf(ShadcnStylePreset.Vega) }
    var baseColor by remember { mutableStateOf(ShadcnBaseColor.Zinc) }
    var accent by remember { mutableStateOf(ShadcnAccent.Base) }
    val isDarkMode = darkModeOverride ?: isSystemInDarkTheme()

    ShadcnTheme(
        // Fixed chrome identity -- not stylePreset/baseColor/accent, see doc comment above.
        preset = ShadcnStylePreset.Vega,
        baseColor = ShadcnBaseColor.Zinc,
        accent = ShadcnAccent.Base,
        baseRadius = ShadcnRadius(4.dp),
        isDark = isDarkMode,
    ) {
        CatalogNavHost(
            stylePreset = stylePreset,
            onStylePresetChange = { stylePreset = it },
            baseColor = baseColor,
            onBaseColorChange = { baseColor = it },
            accent = accent,
            onAccentChange = { accent = it },
            isDarkMode = isDarkMode,
            onToggleDarkMode = { darkModeOverride = it },
        )
    }
}
