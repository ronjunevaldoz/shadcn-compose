package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnFieldDescription
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnFieldLabel
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnInputOTP
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

val inputOTPDoc =
    ComponentDoc(
        id = "input-otp",
        title = "Input OTP",
        description = "A one-time-passcode input with boxed slots that fill left-to-right.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnInputOTP

            var code by remember { mutableStateOf("") }
            ShadcnInputOTP(value = code, onValueChange = { code = it }, length = 6)
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Verify your email",
                    code =
                        """
                        var code by remember { mutableStateOf("") }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ShadcnFieldLabel("One-time password")
                            ShadcnInputOTP(value = code, onValueChange = { code = it }, length = 6)
                            ShadcnFieldDescription("Please enter the one-time password sent to your phone.")
                        }
                        """.trimIndent(),
                    preview = {
                        var code by remember { mutableStateOf("") }
                        Column(verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm)) {
                            ShadcnFieldLabel("One-time password")
                            ShadcnInputOTP(value = code, onValueChange = { code = it }, length = 6)
                            ShadcnFieldDescription("Please enter the one-time password sent to your phone.")
                        }
                    },
                ),
            ),
    )
