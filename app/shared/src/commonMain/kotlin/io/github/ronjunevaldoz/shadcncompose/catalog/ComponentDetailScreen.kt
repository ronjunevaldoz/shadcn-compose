package io.github.ronjunevaldoz.shadcncompose.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.ronjunevaldoz.shadcncompose.catalog.docs.Breadcrumb
import io.github.ronjunevaldoz.shadcncompose.catalog.docs.CodeBlock
import io.github.ronjunevaldoz.shadcncompose.catalog.docs.ComponentDoc
import io.github.ronjunevaldoz.shadcncompose.catalog.docs.PreviewCodeSection
import io.github.ronjunevaldoz.shadcncompose.catalog.docs.componentDocsById
import io.github.ronjunevaldoz.shadcncompose.catalog.docs.guidePagesById
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/** Dispatches a sidebar entry id to either a Getting Started guide page or a component demo. */
@Composable
fun ComponentDetailScreen(componentId: String) {
    guidePagesById[componentId]?.let { guidePage ->
        guidePage.content()
        return
    }
    val doc = componentDocsById[componentId] ?: return
    ComponentDetailContent(doc)
}

@Composable
private fun ComponentDetailContent(doc: ComponentDoc) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(shadcnTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xxl),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm)) {
            Breadcrumb("Components", doc.title)
            ShadcnText(doc.title, style = ShadcnTextStyle.TitleLarge)
            ShadcnText(doc.description, style = ShadcnTextStyle.BodyLarge, muted = true)
        }

        Section(title = "Preview & Code") {
            PreviewCodeSection(
                code = doc.examples.first().code,
                modifier = Modifier.fillMaxWidth(),
                preview = doc.examples.first().preview,
            )
        }

        Section(title = "Usage") {
            CodeBlock(code = doc.usageCode, modifier = Modifier.fillMaxWidth())
        }

        Section(title = "Examples") {
            Column(verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xl)) {
                doc.examples.forEach { example ->
                    Column(verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm)) {
                        ShadcnText(example.title, style = ShadcnTextStyle.TitleSmall)
                        PreviewCodeSection(
                            code = example.code,
                            modifier = Modifier.fillMaxWidth(),
                            preview = example.preview,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.md)) {
        ShadcnText(title, style = ShadcnTextStyle.TitleMedium)
        content()
    }
}
