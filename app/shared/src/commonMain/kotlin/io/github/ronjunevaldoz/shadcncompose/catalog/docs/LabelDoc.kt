@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCheckbox
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnField
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnFieldOrientation
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnLabel

val labelDoc =
    ComponentDoc(
        id = "label",
        title = "Label",
        description = "A form-field label, optionally marked required.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnLabel

            ShadcnLabel("Email", required = true)
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code = """ShadcnLabel("Email")""",
                    preview = { ShadcnLabel("Email") },
                ),
                ComponentExample(
                    title = "Required",
                    code = """ShadcnLabel("Email", required = true)""",
                    preview = { ShadcnLabel("Email", required = true) },
                ),
                ComponentExample(
                    title = "Disabled",
                    code = """ShadcnLabel("Email", disabled = true)""",
                    preview = { ShadcnLabel("Email", disabled = true) },
                ),
                ComponentExample(
                    title = "With a checkbox",
                    code =
                        """
                        var checked by remember { mutableStateOf(false) }
                        ShadcnField(orientation = ShadcnFieldOrientation.Horizontal) {
                            ShadcnCheckbox(checked = checked, onCheckedChange = { checked = it })
                            ShadcnLabel("Accept terms and conditions")
                        }
                        """.trimIndent(),
                    preview = {
                        var checked by remember { mutableStateOf(false) }
                        ShadcnField(orientation = ShadcnFieldOrientation.Horizontal) {
                            ShadcnCheckbox(checked = checked, onCheckedChange = { checked = it })
                            ShadcnLabel("Accept terms and conditions")
                        }
                    },
                ),
            ),
    )
