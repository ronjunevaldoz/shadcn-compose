package io.github.ronjunevaldoz.shadcncompose.components

import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import kotlin.test.Test

class MarkerScreenshotTest : ShadcnScreenshotTest() {
    private fun default(darkTheme: Boolean) {
        snapshot("marker_default", darkTheme = darkTheme) {
            ShadcnMarker {
                ShadcnMarkerContent { ShadcnText("Today") }
            }
        }
    }

    private fun separator(darkTheme: Boolean) {
        snapshot("marker_separator", darkTheme = darkTheme) {
            ShadcnMarker(variant = ShadcnMarkerVariant.Separator) {
                ShadcnMarkerContent { ShadcnText("March 3") }
            }
        }
    }

    private fun border(darkTheme: Boolean) {
        snapshot("marker_border", darkTheme = darkTheme) {
            ShadcnMarker(variant = ShadcnMarkerVariant.Border) {
                ShadcnMarkerIcon { ShadcnEmojiText("📌") }
                ShadcnMarkerContent { ShadcnText("Pinned messages") }
            }
        }
    }

    @Test fun default_light() = default(darkTheme = false)

    @Test fun default_dark() = default(darkTheme = true)

    @Test fun separator_light() = separator(darkTheme = false)

    @Test fun separator_dark() = separator(darkTheme = true)

    @Test fun border_light() = border(darkTheme = false)
}
