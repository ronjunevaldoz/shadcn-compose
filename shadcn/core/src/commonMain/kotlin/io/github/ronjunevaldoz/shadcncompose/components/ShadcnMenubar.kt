package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/** One top-level menu in a [ShadcnMenubar] (e.g. "File", "Edit"), with its own dropdown content. */
data class ShadcnMenubarMenu(
    val label: String,
    val content: @Composable ShadcnDropdownMenuScope.() -> Unit,
)

/**
 * A horizontal row of menu triggers, each opening its own [ShadcnDropdownMenu] --
 * matches real shadcn/ui's `menubar.tsx` (a desktop-app-style "File Edit View" menu
 * bar, `flex h-9 rounded-md border bg-background p-1`). Only one menu is open at a
 * time.
 *
 * Usage:
 * ```
 * ShadcnMenubar(
 *     menus = listOf(
 *         ShadcnMenubarMenu("File") {
 *             ShadcnDropdownMenuItem("New Tab", onClick = {})
 *             ShadcnDropdownMenuSeparator()
 *             ShadcnDropdownMenuItem("Close Window", onClick = {}, destructive = true)
 *         },
 *         ShadcnMenubarMenu("Edit") { ShadcnDropdownMenuItem("Undo", onClick = {}) },
 *     ),
 * )
 * ```
 */
@Composable
fun ShadcnMenubar(
    menus: List<ShadcnMenubarMenu>,
    modifier: Modifier = Modifier,
) {
    var openIndex by remember { mutableStateOf(-1) }

    Row(
        modifier =
            modifier
                .background(shadcnTheme.colors.background, RoundedCornerShape(shadcnTheme.shapes.md))
                .border(1.dp, shadcnTheme.colors.border, RoundedCornerShape(shadcnTheme.shapes.md))
                .padding(shadcnTheme.spacing.xxs),
    ) {
        menus.forEachIndexed { index, menu ->
            val interactionSource = remember { MutableInteractionSource() }
            val isOpen = openIndex == index
            Box(
                modifier =
                    Modifier
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { openIndex = if (isOpen) -1 else index },
                        )
                        .background(
                            if (isOpen) shadcnTheme.colors.secondary else shadcnTheme.colors.background,
                            RoundedCornerShape(shadcnTheme.shapes.sm),
                        )
                        .padding(horizontal = shadcnTheme.spacing.sm, vertical = shadcnTheme.spacing.xs),
            ) {
                ShadcnText(menu.label, style = ShadcnTextStyle.BodySmall)
                ShadcnDropdownMenu(
                    expanded = isOpen,
                    onDismissRequest = { openIndex = -1 },
                    content = menu.content,
                )
            }
        }
    }
}
