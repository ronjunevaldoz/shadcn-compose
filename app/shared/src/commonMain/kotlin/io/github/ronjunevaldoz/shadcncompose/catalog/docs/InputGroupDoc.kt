@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.style.Style
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnInputGroup
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnInputGroupText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextField
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonSize
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import io.github.ronjunevaldoz.heroicons.outline.ArrowUp
import io.github.ronjunevaldoz.heroicons.outline.Plus

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
                ShadcnTextField(value = amount, onValueChange = { amount = it })
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
                            ShadcnTextField(value = amount, onValueChange = { amount = it })
                        }
                        """.trimIndent(),
                    preview = {
                        var amount by remember { mutableStateOf("") }
                        ShadcnInputGroup(leading = { ShadcnInputGroupText("$") }) {
                            ShadcnTextField(value = amount, onValueChange = { amount = it })
                        }
                    },
                ),
                ComponentExample(
                    title = "Trailing addon",
                    code =
                        """
                        var domain by remember { mutableStateOf("") }
                        ShadcnInputGroup(trailing = { ShadcnInputGroupText(".com") }) {
                            ShadcnTextField(value = domain, onValueChange = { domain = it })
                        }
                        """.trimIndent(),
                    preview = {
                        var domain by remember { mutableStateOf("") }
                        ShadcnInputGroup(trailing = { ShadcnInputGroupText(".com") }) {
                            ShadcnTextField(value = domain, onValueChange = { domain = it })
                        }
                    },
                ),
                ComponentExample(
                    title = "Chat composer (bottomBar)",
                    code =
                        """
                        // bottomBar matches real shadcn's InputGroupAddon align="block-end" --
                        // a full-width toolbar row below a growing multi-line field, instead
                        // of an inline leading/trailing addon beside it.
                        var message by remember { mutableStateOf("") }
                        ShadcnInputGroup(
                            bottomBar = {
                                ShadcnButton(
                                    onClick = {},
                                    variant = ButtonVariant.Ghost,
                                    size = ButtonSize.Icon,
                                    style = Style { shape(CircleShape) },
                                ) {
                                    DocIcon(Plus)
                                }
                                Spacer(Modifier.weight(1f))
                                ShadcnButton(
                                    onClick = {},
                                    size = ButtonSize.Icon,
                                    enabled = message.isNotBlank(),
                                    style = Style { shape(CircleShape) },
                                ) {
                                    DocIcon(ArrowUp, tint = shadcnTheme.colors.onPrimary)
                                }
                            },
                        ) {
                            ShadcnTextField(
                                value = message,
                                onValueChange = { message = it },
                                placeholder = "Ask anything...",
                                singleLine = false,
                            )
                        }
                        """.trimIndent(),
                    preview = {
                        var message by remember { mutableStateOf("") }
                        ShadcnInputGroup(
                            bottomBar = {
                                ShadcnButton(
                                    onClick = {},
                                    variant = ButtonVariant.Ghost,
                                    size = ButtonSize.Icon,
                                    style = Style { shape(CircleShape) },
                                ) {
                                    DocIcon(Plus)
                                }
                                Spacer(Modifier.weight(1f))
                                ShadcnButton(
                                    onClick = {},
                                    size = ButtonSize.Icon,
                                    enabled = message.isNotBlank(),
                                    style = Style { shape(CircleShape) },
                                ) {
                                    DocIcon(ArrowUp, tint = shadcnTheme.colors.onPrimary)
                                }
                            },
                        ) {
                            ShadcnTextField(
                                value = message,
                                onValueChange = { message = it },
                                placeholder = "Ask anything...",
                                singleLine = false,
                            )
                        }
                    },
                ),
            ),
    )
