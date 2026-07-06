package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

val introductionPage =
    GuidePage(
        id = "introduction",
        title = "Introduction",
        content = { IntroductionContent() },
    )

@Composable
private fun IntroductionContent() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(shadcnTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.lg),
    ) {
        ShadcnText("shadcn-compose", style = ShadcnTextStyle.TitleLarge)
        GuideParagraph(
            "shadcn-compose is a shadcn/ui-inspired component library for Compose Multiplatform. " +
                "It ships token-based theming and sealed variant systems (ButtonVariant, CardVariant, " +
                "BadgeVariant, ...) built on the Compose Styles API -- there is no Material dependency " +
                "anywhere in the library.",
        )
        GuideHeading("Why not Material?")
        GuideParagraph(
            "Material3 bundles its own opinionated tokens, shapes, and component behavior. " +
                "shadcn-compose instead gives you full ownership of the token layer -- colors, " +
                "typography, shape, and spacing are plain data classes you can override completely, " +
                "the same way shadcn/ui hands you owned component source instead of a black-box npm package.",
        )
        GuideHeading("Platforms")
        GuideParagraph(
            "Every component targets Android, iOS (arm64 + simulator), Desktop (JVM), and Web " +
                "(JS + WasmJS) from a single commonMain source set.",
        )
        GuideHeading("Where to go next")
        GuideParagraph(
            "See Installation for the Gradle dependency, Theming for how ShadcnTheme and design " +
                "tokens work, and Dark Mode for the light/dark switching model. Then browse the " +
                "component list in the sidebar below.",
        )
    }
}
