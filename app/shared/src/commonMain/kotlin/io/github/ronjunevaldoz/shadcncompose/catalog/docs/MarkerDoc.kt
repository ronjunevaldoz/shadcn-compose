package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMarker
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMarkerContent
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMarkerIcon
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnMarkerVariant
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText

val markerDoc =
    ComponentDoc(
        id = "marker",
        title = "Marker",
        description = "A labeled divider for a chat transcript, e.g. a date separator or a pinned-messages banner.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.*

            ShadcnMarker(variant = ShadcnMarkerVariant.Separator) {
                ShadcnMarkerContent { ShadcnText("Today") }
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        ShadcnMarker {
                            ShadcnMarkerContent { ShadcnText("Today") }
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnMarker {
                            ShadcnMarkerContent { ShadcnText("Today") }
                        }
                    },
                ),
                ComponentExample(
                    title = "Separator",
                    code =
                        """
                        ShadcnMarker(variant = ShadcnMarkerVariant.Separator) {
                            ShadcnMarkerContent { ShadcnText("March 3") }
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnMarker(variant = ShadcnMarkerVariant.Separator) {
                            ShadcnMarkerContent { ShadcnText("March 3") }
                        }
                    },
                ),
                ComponentExample(
                    title = "Border, with icon",
                    code =
                        """
                        ShadcnMarker(variant = ShadcnMarkerVariant.Border) {
                            ShadcnMarkerIcon { ShadcnText("📌") }
                            ShadcnMarkerContent { ShadcnText("Pinned messages") }
                        }
                        """.trimIndent(),
                    preview = {
                        ShadcnMarker(variant = ShadcnMarkerVariant.Border) {
                            ShadcnMarkerIcon { ShadcnText("📌") }
                            ShadcnMarkerContent { ShadcnText("Pinned messages") }
                        }
                    },
                ),
            ),
    )
