@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

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
import androidx.compose.ui.platform.LocalUriHandler
import io.github.ronjunevaldoz.shadcncompose.catalog.docs.Breadcrumb
import io.github.ronjunevaldoz.shadcncompose.catalog.docs.CodeBlock
import io.github.ronjunevaldoz.shadcncompose.catalog.docs.ComponentDoc
import io.github.ronjunevaldoz.shadcncompose.catalog.docs.PreviewCodeSection
import io.github.ronjunevaldoz.shadcncompose.catalog.docs.componentDocsById
import io.github.ronjunevaldoz.shadcncompose.catalog.docs.guidePagesById
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonSize
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
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
    val uriHandler = LocalUriHandler.current
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
            // Only shown for components with a real shadcn/ui equivalent -- this library's
            // own additions (Chip, Stepper, ...) leave referenceUrl null rather than link
            // somewhere misleading.
            doc.referenceUrl?.let { url ->
                ShadcnButton(
                    onClick = { uriHandler.openUri(url) },
                    variant = ButtonVariant.Link,
                    size = ButtonSize.Sm,
                ) {
                    ShadcnText("View on shadcn/ui")
                }
            }
        }

        Section(title = "Preview & Code") {
            PreviewCodeSection(
                code = doc.examples.first().code,
                title = doc.examples.first().title,
                modifier = Modifier.fillMaxWidth(),
                preview = doc.examples.first().preview,
            )
        }

        Section(title = "Usage") {
            CodeBlock(code = doc.usageCode, modifier = Modifier.fillMaxWidth())
        }

        // doc.examples.first() is already shown above under "Preview & Code" -- re-showing
        // it here too was displaying the exact same preview+code a second time on every
        // single component page (a third time, near-identically, counting "Usage"), most
        // visibly on the many components with only one example, where "Examples" rendered
        // nothing but a word-for-word repeat of "Preview & Code". Only the *remaining*
        // examples belong here, and only when there's more than one to show.
        val additionalExamples = doc.examples.drop(1)
        if (additionalExamples.isNotEmpty()) {
            Section(title = "Examples") {
                Column(verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xl)) {
                    additionalExamples.forEach { example ->
                        PreviewCodeSection(
                            code = example.code,
                            title = example.title,
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
