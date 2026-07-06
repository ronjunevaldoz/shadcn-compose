@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSwitch

val switchDoc =
    ComponentDoc(
        id = "switch",
        title = "Switch",
        description = "An on/off control for a single setting, applied immediately.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSwitch

            var enabled by remember { mutableStateOf(false) }
            ShadcnSwitch(checked = enabled, onCheckedChange = { enabled = it })
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        var enabled by remember { mutableStateOf(false) }
                        ShadcnSwitch(checked = enabled, onCheckedChange = { enabled = it })
                        """.trimIndent(),
                    preview = {
                        var enabled by remember { mutableStateOf(false) }
                        ShadcnSwitch(checked = enabled, onCheckedChange = { enabled = it })
                    },
                ),
                ComponentExample(
                    title = "Disabled",
                    code = """ShadcnSwitch(checked = true, onCheckedChange = null, enabled = false)""",
                    preview = { ShadcnSwitch(checked = true, onCheckedChange = null, enabled = false) },
                ),
            ),
    )
