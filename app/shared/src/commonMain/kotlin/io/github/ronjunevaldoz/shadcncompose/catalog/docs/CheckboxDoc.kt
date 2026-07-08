@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCheckbox
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnField
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnFieldDescription
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnFieldLabel
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnFieldOrientation

val checkboxDoc =
    ComponentDoc(
        id = "checkbox",
        title = "Checkbox",
        description = "A tri-state input control: unchecked, checked, or indeterminate.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCheckbox

            var checked by remember { mutableStateOf(false) }
            ShadcnCheckbox(checked = checked, onCheckedChange = { checked = it })
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        var checked by remember { mutableStateOf(false) }
                        ShadcnCheckbox(checked = checked, onCheckedChange = { checked = it })
                        """.trimIndent(),
                    preview = {
                        var checked by remember { mutableStateOf(false) }
                        ShadcnCheckbox(checked = checked, onCheckedChange = { checked = it })
                    },
                ),
                ComponentExample(
                    title = "With text",
                    code =
                        """
                        var checked by remember { mutableStateOf(true) }
                        ShadcnField(orientation = ShadcnFieldOrientation.Horizontal) {
                            ShadcnCheckbox(checked = checked, onCheckedChange = { checked = it })
                            Column {
                                ShadcnFieldLabel("Accept terms and conditions")
                                ShadcnFieldDescription(
                                    "By clicking this checkbox, you agree to the terms and conditions.",
                                )
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        var checked by remember { mutableStateOf(true) }
                        ShadcnField(orientation = ShadcnFieldOrientation.Horizontal) {
                            ShadcnCheckbox(checked = checked, onCheckedChange = { checked = it })
                            Column {
                                ShadcnFieldLabel("Accept terms and conditions")
                                ShadcnFieldDescription(
                                    "By clicking this checkbox, you agree to the terms and conditions.",
                                )
                            }
                        }
                    },
                ),
                ComponentExample(
                    title = "Indeterminate",
                    code = """ShadcnCheckbox(checked = false, indeterminate = true, onCheckedChange = {})""",
                    preview = { ShadcnCheckbox(checked = false, indeterminate = true, onCheckedChange = {}) },
                ),
                ComponentExample(
                    title = "Disabled",
                    code = """ShadcnCheckbox(checked = true, onCheckedChange = null, enabled = false)""",
                    preview = { ShadcnCheckbox(checked = true, onCheckedChange = null, enabled = false) },
                ),
            ),
    )
