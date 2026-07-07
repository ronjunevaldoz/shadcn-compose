package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnProgress

val progressDoc =
    ComponentDoc(
        id = "progress",
        title = "Progress",
        description = "A linear progress bar.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnProgress

            ShadcnProgress(value = 0.6f)
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code = "ShadcnProgress(value = 0.6f, modifier = Modifier.width(240.dp))",
                    preview = { ShadcnProgress(value = 0.6f, modifier = Modifier.width(240.dp)) },
                ),
            ),
    )
