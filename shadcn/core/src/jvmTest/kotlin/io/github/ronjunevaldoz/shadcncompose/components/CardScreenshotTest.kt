@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonSize
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.styles.CardVariant
import kotlin.test.Test

class CardScreenshotTest : ShadcnScreenshotTest() {
    private fun allVariants(darkTheme: Boolean) {
        snapshot("card_variants", darkTheme = darkTheme) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf(CardVariant.Default, CardVariant.Elevated, CardVariant.Filled).forEach { variant ->
                    ShadcnCard(
                        modifier = Modifier.width(180.dp),
                        variant = variant,
                        header = {
                            ShadcnCardHeader(
                                title = variant::class.simpleName ?: "?",
                                description = "Card description",
                            )
                        },
                        footer = { ShadcnButton(onClick = {}, size = ButtonSize.Sm) { ShadcnText("Action") } },
                    ) {
                        ShadcnText("Card body content goes here.")
                    }
                }
            }
        }
    }

    private fun createAccount(darkTheme: Boolean) {
        snapshot("card_create_account", darkTheme = darkTheme) {
            ShadcnCard(
                modifier = Modifier.width(320.dp),
                header = {
                    ShadcnCardHeader(
                        title = "Create an account",
                        description = "Enter your email below to create your account",
                    )
                },
                footer = {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        ShadcnTextField(value = "", onValueChange = {}, placeholder = "m@example.com")
                    }
                    ShadcnField {
                        ShadcnFieldLabel("Password", required = true)
                        ShadcnTextField(value = "", onValueChange = {})
                    }
                }
            }
        }
    }

    @Test fun variants_light() = allVariants(darkTheme = false)

    @Test fun variants_dark() = allVariants(darkTheme = true)

    @Test fun create_account_light() = createAccount(darkTheme = false)

    @Test fun create_account_dark() = createAccount(darkTheme = true)
}
