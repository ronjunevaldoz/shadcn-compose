@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.heroicons.outline.Check
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAlert
import io.github.ronjunevaldoz.shadcncompose.styles.AlertVariant

val alertDoc =
    ComponentDoc(
        id = "alert",
        title = "Alert",
        description = "A short, prominent callout for important information.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnAlert

            ShadcnAlert(title = "Heads up!", description = "You can add components to your app.")
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            ShadcnAlert(
                                title = "Success! Your changes have been saved.",
                                icon = { DocIcon(Check) },
                            )
                            ShadcnAlert(
                                title = "This Alert has a title and no icon.",
                                description = "No worries though, we've got you covered.",
                            )
                        }
                        """.trimIndent(),
                    preview = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            ShadcnAlert(
                                title = "Success! Your changes have been saved.",
                                icon = { DocIcon(Check) },
                            )
                            ShadcnAlert(
                                title = "This Alert has a title and no icon.",
                                description = "No worries though, we've got you covered.",
                            )
                        }
                    },
                ),
                ComponentExample(
                    title = "Destructive",
                    code =
                        """
                        ShadcnAlert(
                            variant = AlertVariant.Destructive,
                            title = "Error",
                            description = "Your session has expired. Please log in again.",
                        )
                        """.trimIndent(),
                    preview = {
                        ShadcnAlert(
                            variant = AlertVariant.Destructive,
                            title = "Error",
                            description = "Your session has expired. Please log in again.",
                        )
                    },
                ),
            ),
    )
