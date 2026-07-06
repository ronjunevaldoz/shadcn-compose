package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

val darkModePage =
    GuidePage(
        id = "dark-mode",
        title = "Dark Mode",
        content = { DarkModeContent() },
    )

@Composable
private fun DarkModeContent() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(shadcnTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.lg),
    ) {
        ShadcnText("Dark Mode", style = ShadcnTextStyle.TitleLarge)
        GuideParagraph(
            "ShadcnTheme follows the system's dark mode setting by default on every platform " +
                "(isSystemInDarkTheme() on Android/iOS/Desktop/Web) -- no configuration required.",
        )
        GuideHeading("Overriding the system setting")
        GuideParagraph(
            "To let users pick light/dark independently of the OS (a settings screen toggle), " +
                "provide LocalShadcnDarkTheme above ShadcnTheme:",
        )
        CodeBlock(
            code =
                """
                val userPrefersDark: Boolean? by viewModel.darkModePreference.collectAsState()

                CompositionLocalProvider(LocalShadcnDarkTheme provides userPrefersDark) {
                    ShadcnTheme {
                        CatalogNavHost()
                    }
                }
                """.trimIndent(),
            modifier = Modifier.fillMaxWidth(),
        )
        GuideParagraph(
            "null follows the system, true forces dark, false forces light. Every component picks " +
                "up the change automatically since colors are read live from the Style API, not cached.",
        )
    }
}
