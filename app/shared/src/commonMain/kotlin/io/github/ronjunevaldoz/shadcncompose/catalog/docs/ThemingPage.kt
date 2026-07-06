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

val themingPage =
    GuidePage(
        id = "theming",
        title = "Theming",
        content = { ThemingContent() },
    )

@Composable
private fun ThemingContent() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(shadcnTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.lg),
    ) {
        ShadcnText("Theming", style = ShadcnTextStyle.TitleLarge)
        GuideParagraph(
            "Every screen must be wrapped in ShadcnTheme once, at the app root. It provides the " +
                "current ShadcnColors, ShadcnTypography, ShadcnShapes, and ShadcnSpacing to the whole tree:",
        )
        CodeBlock(
            code =
                """
                setContent {
                    ShadcnTheme {
                        CatalogNavHost()
                    }
                }
                """.trimIndent(),
            modifier = Modifier.fillMaxWidth(),
        )
        GuideHeading("Reading tokens in a composable")
        GuideParagraph("Use the shadcnTheme accessor -- it's a @Composable property, not a static object:")
        CodeBlock(
            code =
                """
                val theme = shadcnTheme
                Box(Modifier.background(theme.colors.primary).padding(theme.spacing.lg))
                """.trimIndent(),
            modifier = Modifier.fillMaxWidth(),
        )
        GuideHeading("Reading tokens inside a Style")
        GuideParagraph(
            "Styles run outside Composition, so shadcnTheme is not available there. Use the " +
                "StyleScope extensions (colors, typography, shapes, spacing) instead -- they read " +
                "the CompositionLocal at the moment the style is applied, not at definition time:",
        )
        CodeBlock(
            code =
                """
                val myStyle = Style {
                    background(colors.primary)   // StyleScope.colors, not shadcnTheme.colors
                    shape(RoundedCornerShape(shapes.md))
                }
                """.trimIndent(),
            modifier = Modifier.fillMaxWidth(),
        )
        GuideHeading("Owning your own palette")
        GuideParagraph(
            "ShadcnColors, ShadcnTypography, ShadcnShapes, and ShadcnSpacing are plain data classes. " +
                "Swap ShadcnLightColors / ShadcnDarkColors for your own brand palette by constructing " +
                "ShadcnTheme.light(colors = yourColors) or ShadcnTheme.dark(colors = yourColors).",
        )
    }
}
