@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.styles.AlertVariant
import io.github.ronjunevaldoz.shadcncompose.styles.rememberStyle
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * A short, prominent callout. Matches real shadcn/ui's `alert.tsx` grid layout
 * (icon column + title/description column).
 *
 * Usage:
 * ```
 * ShadcnAlert(title = "Heads up!", description = "You can add components to your app.")
 * ShadcnAlert(variant = AlertVariant.Destructive, title = "Error", description = "Something went wrong.")
 * ```
 */
@Composable
fun ShadcnAlert(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    variant: AlertVariant = AlertVariant.Default,
    icon: (@Composable () -> Unit)? = null,
    style: Style = Style,
) {
    val styleState = remember { MutableStyleState(MutableInteractionSource()) }
    Row(
        modifier =
            modifier
                .padding(horizontal = shadcnTheme.spacing.lg, vertical = shadcnTheme.spacing.md)
                .styleable(styleState, variant.rememberStyle(), style),
        horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm),
    ) {
        if (icon != null) {
            Column(modifier = Modifier.padding(top = 2.dp)) { icon() }
        }
        Column(verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xxs)) {
            ShadcnText(title, style = ShadcnTextStyle.LabelLarge)
            if (description != null) {
                ShadcnText(description, style = ShadcnTextStyle.BodySmall, muted = true)
            }
        }
    }
}
