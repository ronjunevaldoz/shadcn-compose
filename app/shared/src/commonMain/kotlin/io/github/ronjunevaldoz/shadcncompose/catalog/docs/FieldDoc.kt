@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnField
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnFieldDescription
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnFieldGroup
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnFieldLabel
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextField

val fieldDoc =
    ComponentDoc(
        id = "field",
        title = "Field",
        description =
            "A labeled control + description layout for building forms. Real shadcn/ui's form.tsx is a thin " +
                "wrapper of these same primitives around react-hook-form; this library has no form-state " +
                "dependency, so compose Field directly with your own hoisted state.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.*

            ShadcnFieldGroup {
                ShadcnField {
                    ShadcnFieldLabel("Email")
                    ShadcnTextField(value = email, onValueChange = { email = it })
                    ShadcnFieldDescription("We'll never share your email.")
                }
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        var email by remember { mutableStateOf("") }
                        ShadcnFieldGroup {
                            ShadcnField {
                                ShadcnFieldLabel("Email", required = true)
                                ShadcnTextField(value = email, onValueChange = { email = it }, placeholder = "you@example.com")
                                ShadcnFieldDescription("We'll never share your email.")
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        var email by remember { mutableStateOf("") }
                        ShadcnFieldGroup {
                            ShadcnField {
                                ShadcnFieldLabel("Email", required = true)
                                ShadcnTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    placeholder = "you@example.com",
                                )
                                ShadcnFieldDescription("We'll never share your email.")
                            }
                        }
                    },
                ),
            ),
    )
