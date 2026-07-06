package io.github.ronjunevaldoz.shadcncompose

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.ronjunevaldoz.shadcncompose.navigation.CatalogNavHost
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme

@Composable
@Preview
fun App() {
    ShadcnTheme {
        CatalogNavHost()
    }
}
