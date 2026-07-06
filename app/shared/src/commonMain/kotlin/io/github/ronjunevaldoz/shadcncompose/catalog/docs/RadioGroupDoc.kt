@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnLabel
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnRadioButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnRadioGroup

val radioGroupDoc =
    ComponentDoc(
        id = "radio-group",
        title = "Radio Group",
        description = "A set of mutually exclusive options. Combine ShadcnRadioButton items inside ShadcnRadioGroup.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnRadioButton
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnRadioGroup

            var choice by remember { mutableStateOf("a") }
            ShadcnRadioGroup {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ShadcnRadioButton(selected = choice == "a", onClick = { choice = "a" })
                    ShadcnLabel("Option A")
                }
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        var choice by remember { mutableStateOf("a") }
                        ShadcnRadioGroup {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ShadcnRadioButton(selected = choice == "a", onClick = { choice = "a" })
                                ShadcnLabel("Option A")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ShadcnRadioButton(selected = choice == "b", onClick = { choice = "b" })
                                ShadcnLabel("Option B")
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        var choice by remember { mutableStateOf("a") }
                        ShadcnRadioGroup {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                ShadcnRadioButton(selected = choice == "a", onClick = { choice = "a" })
                                ShadcnLabel("Option A")
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                ShadcnRadioButton(selected = choice == "b", onClick = { choice = "b" })
                                ShadcnLabel("Option B")
                            }
                        }
                    },
                ),
            ),
    )
