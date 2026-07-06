package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

val typographyPage =
    GuidePage(
        id = "typography",
        title = "Typography",
        content = { TypographyContent() },
    )

@Composable
private fun TypographyContent() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(shadcnTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.lg),
    ) {
        ShadcnText("Typography", style = ShadcnTextStyle.TitleLarge)
        GuideParagraph(
            "ShadcnText renders the ShadcnTextStyle type scale. Every style below maps to a " +
                "ShadcnTypography token, so changing the token updates every screen at once.",
        )
        Column(verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm)) {
            ShadcnText("Display Large", style = ShadcnTextStyle.DisplayLarge)
            ShadcnText("Display Medium", style = ShadcnTextStyle.DisplayMedium)
            ShadcnText("Title Large", style = ShadcnTextStyle.TitleLarge)
            ShadcnText("Title Medium", style = ShadcnTextStyle.TitleMedium)
            ShadcnText("Title Small", style = ShadcnTextStyle.TitleSmall)
            ShadcnText("Body Large", style = ShadcnTextStyle.BodyLarge)
            ShadcnText("Body Medium", style = ShadcnTextStyle.BodyMedium)
            ShadcnText("Body Small", style = ShadcnTextStyle.BodySmall)
            ShadcnText("Label Large", style = ShadcnTextStyle.LabelLarge)
            ShadcnText("Label Small", style = ShadcnTextStyle.LabelSmall)
        }
        GuideHeading("Muted text")
        GuideParagraph(
            "Pass muted = true for secondary/supporting text -- it reads onSurfaceVariant instead of onSurface:",
        )
        ShadcnText("This is muted body text", style = ShadcnTextStyle.BodyMedium, muted = true)
    }
}
