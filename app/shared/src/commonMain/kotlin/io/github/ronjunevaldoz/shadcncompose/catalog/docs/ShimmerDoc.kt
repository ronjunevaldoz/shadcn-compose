package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.ui.Modifier
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.styles.shadcnShimmer

val shimmerDoc =
    ComponentDoc(
        id = "shimmer",
        title = "Shimmer",
        description =
            "A sweeping highlight animation for \"generating response\"/\"thinking\" text states -- " +
                "a modifier, not a standalone component.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.styles.shadcnShimmer

            ShadcnText("Generating response…", muted = true, modifier = Modifier.shadcnShimmer())
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code = """ShadcnText("Generating response…", muted = true, modifier = Modifier.shadcnShimmer())""",
                    preview = {
                        ShadcnText("Generating response…", muted = true, modifier = Modifier.shadcnShimmer())
                    },
                ),
            ),
    )
