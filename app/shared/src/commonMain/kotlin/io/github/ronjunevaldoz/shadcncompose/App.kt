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

// No manual dark-mode override here -- the catalog always follows the system's own
// light/dark setting. A consumer app that wants a manual toggle can still provide
// io.github.ronjunevaldoz.shadcncompose.theme.LocalShadcnDarkTheme above ShadcnTheme (see
// catalog/docs/DarkModePage.kt for that pattern); the catalog itself just doesn't wire one
// up as a live control anymore.
@Composable
@Preview
fun App() {
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
        )
    }
}
