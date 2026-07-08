package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItem
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItemActions
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItemContent
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItemDescription
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItemGroup
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItemMedia
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItemMediaVariant
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItemSeparator
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItemTitle
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnItemVariant
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText

val itemDoc =
    ComponentDoc(
        id = "item",
        title = "Item",
        description = "A single row in a list of interactive/informational rows.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.*

            ShadcnItem(variant = ShadcnItemVariant.Outline) {
                ShadcnItemContent {
                    ShadcnItemTitle("Jane Doe")
                    ShadcnItemDescription("jane@example.com")
                }
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        ShadcnItemGroup {
                            ShadcnItem(variant = ShadcnItemVariant.Outline) {
                                ShadcnItemMedia(variant = ShadcnItemMediaVariant.Icon) { ShadcnText("📦") }
                                ShadcnItemContent {
                                    ShadcnItemTitle("Order #1234")
                                    ShadcnItemDescription("Shipped on March 12")
                                }
                                ShadcnItemActions { ShadcnText("Track") }
                            }
                            ShadcnItemSeparator()
                            ShadcnItem(variant = ShadcnItemVariant.Outline) {
                                ShadcnItemMedia(variant = ShadcnItemMediaVariant.Icon) { ShadcnText("📦") }
                                ShadcnItemContent {
                                    ShadcnItemTitle("Order #1235")
                                    ShadcnItemDescription("Processing")
                                }
                                ShadcnItemActions { ShadcnText("Track") }
                            }
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnItemGroup {
                            ShadcnItem(variant = ShadcnItemVariant.Outline) {
                                ShadcnItemMedia(variant = ShadcnItemMediaVariant.Icon) { ShadcnText("📦") }
                                ShadcnItemContent {
                                    ShadcnItemTitle("Order #1234")
                                    ShadcnItemDescription("Shipped on March 12")
                                }
                                ShadcnItemActions { ShadcnText("Track") }
                            }
                            ShadcnItemSeparator()
                            ShadcnItem(variant = ShadcnItemVariant.Outline) {
                                ShadcnItemMedia(variant = ShadcnItemMediaVariant.Icon) { ShadcnText("📦") }
                                ShadcnItemContent {
                                    ShadcnItemTitle("Order #1235")
                                    ShadcnItemDescription("Processing")
                                }
                                ShadcnItemActions { ShadcnText("Track") }
                            }
                        }
                    },
                ),
            ),
    )
