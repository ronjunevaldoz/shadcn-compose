@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnToggle
import io.github.ronjunevaldoz.shadcncompose.styles.ToggleVariant

val toggleDoc =
    ComponentDoc(
        id = "toggle",
        title = "Toggle",
        description = "A two-state button, commonly used for toolbar toggles like bold or italic.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnToggle

            var bold by remember { mutableStateOf(false) }
            ShadcnToggle(pressed = bold, onPressedChange = { bold = it }) { ShadcnText("B") }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        var bold by remember { mutableStateOf(false) }
                        ShadcnToggle(pressed = bold, onPressedChange = { bold = it }) { ShadcnText("Bold") }
                        """.trimIndent(),
                    preview = {
                        var bold by remember { mutableStateOf(false) }
                        ShadcnToggle(pressed = bold, onPressedChange = { bold = it }) { ShadcnText("Bold") }
                    },
                ),
                ComponentExample(
                    title = "Outline",
                    code =
                        """
                        var italic by remember { mutableStateOf(false) }
                        ShadcnToggle(pressed = italic, onPressedChange = { italic = it }, variant = ToggleVariant.Outline) {
                            ShadcnText("Italic")
                        }
                        """.trimIndent(),
                    preview = {
                        var italic by remember { mutableStateOf(false) }
                        ShadcnToggle(
                            pressed = italic,
                            onPressedChange = { italic = it },
                            variant = ToggleVariant.Outline,
                        ) { ShadcnText("Italic") }
                    },
                ),
            ),
    )
