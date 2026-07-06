@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextField

val textFieldDoc =
    ComponentDoc(
        id = "text-field",
        title = "Text Field",
        description = "A single-line or multi-line input with label, placeholder, and error states.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextField

            var email by remember { mutableStateOf("") }
            ShadcnTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "you@example.com",
            )
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        var email by remember { mutableStateOf("") }
                        ShadcnTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Email",
                            placeholder = "you@example.com",
                        )
                        """.trimIndent(),
                    preview = {
                        var email by remember { mutableStateOf("") }
                        ShadcnTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Email",
                            placeholder = "you@example.com",
                        )
                    },
                ),
                ComponentExample(
                    title = "Error",
                    code =
                        """
                        ShadcnTextField(
                            value = "bad-email",
                            onValueChange = {},
                            label = "Email",
                            isError = true,
                            supportingText = "Please enter a valid email address",
                        )
                        """.trimIndent(),
                    preview = {
                        ShadcnTextField(
                            value = "bad-email",
                            onValueChange = {},
                            label = "Email",
                            isError = true,
                            supportingText = "Please enter a valid email address",
                        )
                    },
                ),
            ),
    )
