package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTabItem
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTabsList
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText

val tabsDoc =
    ComponentDoc(
        id = "tabs",
        title = "Tabs",
        description = "A segmented control for switching between related views.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTabItem
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTabsList

            var tab by remember { mutableStateOf("account") }
            ShadcnTabsList(
                items = listOf(ShadcnTabItem("account", "Account"), ShadcnTabItem("password", "Password")),
                selected = tab,
                onSelectedChange = { tab = it },
            )
            when (tab) {
                "account" -> ShadcnText("Account settings")
                "password" -> ShadcnText("Password settings")
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        var tab by remember { mutableStateOf("account") }
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            ShadcnTabsList(
                                items = listOf(ShadcnTabItem("account", "Account"), ShadcnTabItem("password", "Password")),
                                selected = tab,
                                onSelectedChange = { tab = it },
                            )
                            when (tab) {
                                "account" -> ShadcnText("Make changes to your account here.")
                                "password" -> ShadcnText("Change your password here.")
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        var tab by remember { mutableStateOf("account") }
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            ShadcnTabsList(
                                items =
                                    listOf(
                                        ShadcnTabItem("account", "Account"),
                                        ShadcnTabItem("password", "Password"),
                                    ),
                                selected = tab,
                                onSelectedChange = { tab = it },
                            )
                            when (tab) {
                                "account" -> ShadcnText("Make changes to your account here.")
                                "password" -> ShadcnText("Change your password here.")
                            }
                        }
                    },
                ),
            ),
    )
