@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnButton
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnSwitch
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

private const val GITHUB_URL = "https://github.com/ronjunevaldoz/shadcn-compose"

/**
 * App-wide top bar: brand mark on the left, GitHub link + dark mode toggle on the
 * right. Sits above [io.github.ronjunevaldoz.shadcncompose.navigation.CatalogNavHost]'s
 * sidebar/content row so it stays visible across every screen.
 */
@Composable
fun CatalogTopBar(
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(shadcnTheme.colors.background)
                .padding(horizontal = shadcnTheme.spacing.lg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShadcnText("shadcn-compose", style = ShadcnTextStyle.TitleSmall)

        Row(
            horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShadcnButton(
                onClick = { uriHandler.openUri(GITHUB_URL) },
                variant = ButtonVariant.Ghost,
            ) {
                ShadcnText("GitHub")
            }
            ShadcnSwitch(checked = isDarkMode, onCheckedChange = onToggleDarkMode)
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(shadcnTheme.colors.border),
    )
}
