@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.ShadcnScreenshotTest
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonSize
import io.github.ronjunevaldoz.shadcncompose.styles.CardVariant
import kotlin.test.Test

class CardScreenshotTest : ShadcnScreenshotTest() {
    private fun allVariants(darkTheme: Boolean) {
        snapshot("card_variants", darkTheme = darkTheme) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf(CardVariant.Default, CardVariant.Elevated, CardVariant.Filled).forEach { variant ->
                    ShadcnCard(
                        modifier = Modifier.width(180.dp),
                        variant = variant,
                        header = {
                            ShadcnCardHeader(
                                title = variant::class.simpleName ?: "?",
                                description = "Card description",
                            )
                        },
                        footer = { ShadcnButton(onClick = {}, size = ButtonSize.Sm) { ShadcnText("Action") } },
                    ) {
                        ShadcnText("Card body content goes here.")
                    }
                }
            }
        }
    }

    @Test fun variants_light() = allVariants(darkTheme = false)

    @Test fun variants_dark() = allVariants(darkTheme = true)
}
