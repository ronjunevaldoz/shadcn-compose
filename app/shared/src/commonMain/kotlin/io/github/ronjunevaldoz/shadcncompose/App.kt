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

@Composable
@Preview
fun App() {
    var darkModeOverride by remember { mutableStateOf<Boolean?>(null) }
    val isDarkMode = darkModeOverride ?: isSystemInDarkTheme()

    CompositionLocalProvider(LocalShadcnDarkTheme provides darkModeOverride) {
        ShadcnTheme {
            CatalogNavHost(
                isDarkMode = isDarkMode,
                onToggleDarkMode = { darkModeOverride = it },
            )
        }
    }
}
