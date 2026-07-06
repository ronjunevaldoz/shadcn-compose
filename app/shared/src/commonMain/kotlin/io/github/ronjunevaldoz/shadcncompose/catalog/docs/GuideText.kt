package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.runtime.Composable
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle

@Composable
fun GuideHeading(text: String) {
    ShadcnText(text, style = ShadcnTextStyle.TitleMedium)
}

@Composable
fun GuideParagraph(text: String) {
    ShadcnText(text, style = ShadcnTextStyle.BodyLarge)
}
