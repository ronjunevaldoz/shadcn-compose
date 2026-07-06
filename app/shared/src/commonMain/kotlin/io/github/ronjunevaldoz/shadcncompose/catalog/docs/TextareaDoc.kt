@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextarea

val textareaDoc =
    ComponentDoc(
        id = "textarea",
        title = "Textarea",
        description = "A multi-line text input, sharing the same border/focus spec as Text Field.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextarea

            var bio by remember { mutableStateOf("") }
            ShadcnTextarea(value = bio, onValueChange = { bio = it }, placeholder = "Tell us about yourself")
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        var bio by remember { mutableStateOf("") }
                        ShadcnTextarea(
                            value = bio,
                            onValueChange = { bio = it },
                            label = "Bio",
                            placeholder = "Tell us about yourself",
                        )
                        """.trimIndent(),
                    preview = {
                        var bio by remember { mutableStateOf("") }
                        ShadcnTextarea(
                            value = bio,
                            onValueChange = { bio = it },
                            label = "Bio",
                            placeholder = "Tell us about yourself",
                        )
                    },
                ),
            ),
    )
