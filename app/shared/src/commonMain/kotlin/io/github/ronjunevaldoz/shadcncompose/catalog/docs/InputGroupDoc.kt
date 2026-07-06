@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnInputGroup
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnInputGroupText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextField
import io.github.ronjunevaldoz.shadcncompose.styles.TextFieldVariant

val inputGroupDoc =
    ComponentDoc(
        id = "input-group",
        title = "Input Group",
        description = "Groups a text field with leading or trailing addons -- icons, static text, or buttons.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnInputGroup
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnInputGroupText

            var amount by remember { mutableStateOf("") }
            ShadcnInputGroup(leading = { ShadcnInputGroupText("${'$'}") }) {
                ShadcnTextField(value = amount, onValueChange = { amount = it }, variant = TextFieldVariant.Ghost)
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Leading addon",
                    code =
                        """
                        var amount by remember { mutableStateOf("") }
                        ShadcnInputGroup(leading = { ShadcnInputGroupText("${'$'}") }) {
                            ShadcnTextField(value = amount, onValueChange = { amount = it }, variant = TextFieldVariant.Ghost)
                        }
                        """.trimIndent(),
                    preview = {
                        var amount by remember { mutableStateOf("") }
                        ShadcnInputGroup(leading = { ShadcnInputGroupText("$") }) {
                            ShadcnTextField(
                                value = amount,
                                onValueChange = { amount = it },
                                variant = TextFieldVariant.Ghost,
                            )
                        }
                    },
                ),
                ComponentExample(
                    title = "Trailing addon",
                    code =
                        """
                        var domain by remember { mutableStateOf("") }
                        ShadcnInputGroup(trailing = { ShadcnInputGroupText(".com") }) {
                            ShadcnTextField(value = domain, onValueChange = { domain = it }, variant = TextFieldVariant.Ghost)
                        }
                        """.trimIndent(),
                    preview = {
                        var domain by remember { mutableStateOf("") }
                        ShadcnInputGroup(trailing = { ShadcnInputGroupText(".com") }) {
                            ShadcnTextField(
                                value = domain,
                                onValueChange = { domain = it },
                                variant = TextFieldVariant.Ghost,
                            )
                        }
                    },
                ),
            ),
    )
