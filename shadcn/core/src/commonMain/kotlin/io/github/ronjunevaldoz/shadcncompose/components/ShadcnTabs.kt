@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.focused
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.ronjunevaldoz.shadcncompose.styles.focusRingShadow
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/** One tab in a [ShadcnTabsList]. */
data class ShadcnTabItem(val value: String, val label: String)

/**
 * A segmented tab switcher. Matches real shadcn/ui's `tabs.tsx` "default" variant: a
 * `bg-muted` track with the active tab as a raised `bg-background` pill.
 *
 * Usage:
 * ```
 * var tab by remember { mutableStateOf("account") }
 * ShadcnTabsList(
 *     items = listOf(ShadcnTabItem("account", "Account"), ShadcnTabItem("password", "Password")),
 *     selected = tab,
 *     onSelectedChange = { tab = it },
 * )
 * when (tab) {
 *     "account" -> ShadcnText("Account settings")
 *     "password" -> ShadcnText("Password settings")
 * }
 * ```
 */
@Composable
fun ShadcnTabsList(
    items: List<ShadcnTabItem>,
    selected: String,
    onSelectedChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .background(shadcnTheme.colors.muted, RoundedCornerShape(shadcnTheme.shapes.lg))
                .padding(shadcnTheme.spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xxs),
    ) {
        items.forEach { item ->
            val isSelected = item.value == selected
            val interactionSource = remember { MutableInteractionSource() }
            val theme = shadcnTheme
            val styleState = remember { MutableStyleState(interactionSource) }
            val tabStyle =
                remember(isSelected, theme) {
                    Style {
                        background(if (isSelected) theme.colors.background else theme.colors.muted)
                        shape(RoundedCornerShape(theme.shapes.md))
                        focused { dropShadow(theme.focusRingShadow()) }
                    }
                }
            Box(
                modifier =
                    Modifier
                        .styleable(styleState, tabStyle)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onSelectedChange(item.value) },
                        )
                        .padding(horizontal = shadcnTheme.spacing.md, vertical = shadcnTheme.spacing.xs),
                contentAlignment = Alignment.Center,
            ) {
                ShadcnText(
                    item.label,
                    style = ShadcnTextStyle.LabelLarge,
                    muted = !isSelected,
                )
            }
        }
    }
}
