package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSpinner

val spinnerDoc =
    ComponentDoc(
        id = "spinner",
        title = "Spinner",
        description = "A spinning loading indicator.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSpinner

            ShadcnSpinner()
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code = "ShadcnSpinner()",
                    preview = { ShadcnSpinner() },
                ),
                ComponentExample(
                    title = "Large",
                    code = "ShadcnSpinner(modifier = Modifier.size(32.dp))",
                    preview = { ShadcnSpinner(modifier = Modifier.size(32.dp)) },
                ),
            ),
    )
