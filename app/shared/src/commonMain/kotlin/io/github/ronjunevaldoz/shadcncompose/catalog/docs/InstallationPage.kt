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

val installationPage =
    GuidePage(
        id = "installation",
        title = "Installation",
        content = { InstallationContent() },
    )

@Composable
private fun InstallationContent() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(shadcnTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.lg),
    ) {
        ShadcnText("Installation", style = ShadcnTextStyle.TitleLarge)
        GuideParagraph("Add the Maven Central dependency to your commonMain source set:")
        CodeBlock(
            code =
                """
                // gradle/libs.versions.toml
                [versions]
                shadcn-compose = "0.1.0-SNAPSHOT"

                [libraries]
                shadcn-compose = { module = "io.github.ronjunevaldoz:shadcn-compose", version.ref = "shadcn-compose" }
                """.trimIndent(),
            modifier = Modifier.fillMaxWidth(),
        )
        CodeBlock(
            code =
                """
                // build.gradle.kts
                kotlin {
                    sourceSets {
                        commonMain.dependencies {
                            implementation(libs.shadcn.compose)
                        }
                    }
                }
                """.trimIndent(),
            modifier = Modifier.fillMaxWidth(),
        )
        GuideHeading("Opt in to the Styles API")
        GuideParagraph(
            "The library is built on the experimental Compose Styles API. Any file that references " +
                "a component's `style` parameter needs an opt-in:",
        )
        CodeBlock(
            code =
                """
                @file:OptIn(ExperimentalFoundationStyleApi::class)
                """.trimIndent(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
