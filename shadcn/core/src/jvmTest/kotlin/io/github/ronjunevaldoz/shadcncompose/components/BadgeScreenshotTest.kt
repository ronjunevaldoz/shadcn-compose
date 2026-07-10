@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.styles.BadgeVariant
import kotlin.test.Test

class BadgeScreenshotTest : ShadcnScreenshotTest() {
    private fun allVariants(darkTheme: Boolean) {
        snapshot("badge_variants", darkTheme = darkTheme) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    BadgeVariant.Default,
                    BadgeVariant.Secondary,
                    BadgeVariant.Destructive,
                    BadgeVariant.Outline,
                    BadgeVariant.Ghost,
                ).forEach { variant ->
                    ShadcnBadge(variant = variant) { ShadcnText(variant::class.simpleName ?: "?") }
                }
            }
        }
    }

    @Test fun variants_light() = allVariants(darkTheme = false)

    @Test fun variants_dark() = allVariants(darkTheme = true)
}
