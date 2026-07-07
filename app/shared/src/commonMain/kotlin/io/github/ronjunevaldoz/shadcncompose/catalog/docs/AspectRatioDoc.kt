package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAspectRatio
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

val aspectRatioDoc =
    ComponentDoc(
        id = "aspect-ratio",
        title = "Aspect Ratio",
        description = "Constrains content to a fixed width/height ratio.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAspectRatio

            ShadcnAspectRatio(ratio = 16f / 9f) {
                // your image / content
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "16:9",
                    code =
                        """
                        ShadcnAspectRatio(ratio = 16f / 9f, modifier = Modifier.width(240.dp)) {
                            Box(modifier = Modifier.fillMaxSize().background(shadcnTheme.colors.muted))
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnAspectRatio(ratio = 16f / 9f, modifier = Modifier.width(240.dp)) {
                            Box(
                                modifier = Modifier.fillMaxSize().background(shadcnTheme.colors.muted),
                                contentAlignment = Alignment.Center,
                            ) {
                                ShadcnText("16:9", style = ShadcnTextStyle.LabelSmall, muted = true)
                            }
                        }
                    },
                ),
            ),
    )
