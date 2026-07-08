@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCard
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCardHeader
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnField
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnFieldGroup
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnFieldLabel
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextField
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.styles.CardVariant

val cardDoc =
    ComponentDoc(
        id = "card",
        title = "Card",
        description = "Displays content in a bordered container, with optional header and footer slots.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCard
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCardHeader

            ShadcnCard(
                header = { ShadcnCardHeader(title = "Account", description = "Manage your settings") },
            ) {
                ShadcnText("Card body content")
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Create an account",
                    code =
                        """
                        var email by remember { mutableStateOf("") }
                        var password by remember { mutableStateOf("") }
                        ShadcnCard(
                            modifier = Modifier.width(320.dp),
                            header = {
                                ShadcnCardHeader(
                                    title = "Create an account",
                                    description = "Enter your email below to create your account",
                                )
                            },
                            footer = {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    ShadcnButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                                        ShadcnText("Create account")
                                    }
                                    ShadcnButton(
                                        onClick = {},
                                        variant = ButtonVariant.Outline,
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        ShadcnText("Sign up with Google")
                                    }
                                }
                            },
                        ) {
                            ShadcnFieldGroup {
                                ShadcnField {
                                    ShadcnFieldLabel("Email", required = true)
                                    ShadcnTextField(
                                        value = email,
                                        onValueChange = { email = it },
                                        placeholder = "m@example.com",
                                    )
                                }
                                ShadcnField {
                                    ShadcnFieldLabel("Password", required = true)
                                    ShadcnTextField(value = password, onValueChange = { password = it })
                                }
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        var email by remember { mutableStateOf("") }
                        var password by remember { mutableStateOf("") }
                        ShadcnCard(
                            modifier = Modifier.width(320.dp),
                            header = {
                                ShadcnCardHeader(
                                    title = "Create an account",
                                    description = "Enter your email below to create your account",
                                )
                            },
                            footer = {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    ShadcnButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                                        ShadcnText("Create account")
                                    }
                                    ShadcnButton(
                                        onClick = {},
                                        variant = ButtonVariant.Outline,
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        ShadcnText("Sign up with Google")
                                    }
                                }
                            },
                        ) {
                            ShadcnFieldGroup {
                                ShadcnField {
                                    ShadcnFieldLabel("Email", required = true)
                                    ShadcnTextField(
                                        value = email,
                                        onValueChange = { email = it },
                                        placeholder = "m@example.com",
                                    )
                                }
                                ShadcnField {
                                    ShadcnFieldLabel("Password", required = true)
                                    ShadcnTextField(value = password, onValueChange = { password = it })
                                }
                            }
                        }
                    },
                ),
                ComponentExample(
                    title = "Filled",
                    code =
                        """
                        ShadcnCard(variant = CardVariant.Filled) {
                            ShadcnText("Filled card")
                        }
                        """.trimIndent(),
                    preview = { ShadcnCard(variant = CardVariant.Filled) { ShadcnText("Filled card") } },
                ),
            ),
    )
