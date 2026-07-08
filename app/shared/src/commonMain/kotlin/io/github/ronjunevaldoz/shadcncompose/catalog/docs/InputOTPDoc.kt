package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnInputOTP

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
                    title = "Default",
                    code =
                        """
                        var code by remember { mutableStateOf("") }
                        ShadcnInputOTP(value = code, onValueChange = { code = it }, length = 6)
                        """.trimIndent(),
                    preview = {
                        var code by remember { mutableStateOf("") }
                        ShadcnInputOTP(value = code, onValueChange = { code = it }, length = 6)
                    },
                ),
            ),
    )
