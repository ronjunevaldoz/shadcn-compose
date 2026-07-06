@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSlider

val sliderDoc =
    ComponentDoc(
        id = "slider",
        title = "Slider",
        description = "An input for selecting a value from within a range by dragging a thumb.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSlider

            var volume by remember { mutableStateOf(50f) }
            ShadcnSlider(value = volume, onValueChange = { volume = it }, valueRange = 0f..100f)
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        var volume by remember { mutableStateOf(50f) }
                        ShadcnSlider(value = volume, onValueChange = { volume = it }, valueRange = 0f..100f)
                        """.trimIndent(),
                    preview = {
                        var volume by remember { mutableStateOf(50f) }
                        ShadcnSlider(value = volume, onValueChange = { volume = it }, valueRange = 0f..100f)
                    },
                ),
                ComponentExample(
                    title = "Disabled",
                    code = """ShadcnSlider(value = 30f, onValueChange = {}, valueRange = 0f..100f, enabled = false)""",
                    preview = { ShadcnSlider(value = 30f, onValueChange = {}, valueRange = 0f..100f, enabled = false) },
                ),
            ),
    )
