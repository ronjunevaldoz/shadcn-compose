@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCheckbox

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
