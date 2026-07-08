package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.overlay.ShadcnAnchoredPopup
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * One item in a [ShadcnNavigationMenu]: either a plain link ([onClick] set, [panel]
 * null) or a trigger that reveals a [panel] of related links when clicked.
 */
data class ShadcnNavigationMenuItem(
    val label: String,
    val onClick: (() -> Unit)? = null,
    val panel: (@Composable () -> Unit)? = null,
)

/**
 * A horizontal top-level site navigation row where some items open a panel of related
 * links. Matches real shadcn/ui's `navigation-menu.tsx` intent (a simplified take --
 * real shadcn's version animates a shared viewport that morphs between panels; this
 * opens each item's own independently-positioned [ShadcnAnchoredPopup] instead, a
 * deliberate approximation, not full parity).
 *
 * Usage:
 * ```
 * ShadcnNavigationMenu(
 *     items = listOf(
 *         ShadcnNavigationMenuItem("Home", onClick = {}),
 *         ShadcnNavigationMenuItem("Getting started", panel = { ShadcnText("Docs links here") }),
 *     ),
 * )
 * ```
 */
@Composable
fun ShadcnNavigationMenu(
    items: List<ShadcnNavigationMenuItem>,
    modifier: Modifier = Modifier,
) {
    var openIndex by remember { mutableStateOf(-1) }

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xs)) {
        items.forEachIndexed { index, item ->
            val interactionSource = remember { MutableInteractionSource() }
            val isOpen = openIndex == index
            Box(
                modifier =
                    Modifier
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = {
                                if (item.panel != null) {
                                    openIndex = if (isOpen) -1 else index
                                } else {
                                    item.onClick?.invoke()
                                }
                            },
                        )
                        .background(
                            if (isOpen) shadcnTheme.colors.secondary else shadcnTheme.colors.background,
                            RoundedCornerShape(shadcnTheme.shapes.sm),
                        )
                        .padding(horizontal = shadcnTheme.spacing.md, vertical = shadcnTheme.spacing.xs),
            ) {
                ShadcnText(item.label, style = ShadcnTextStyle.BodySmall)
                val panel = item.panel
                if (panel != null) {
                    ShadcnAnchoredPopup(expanded = isOpen, onDismissRequest = { openIndex = -1 }) {
                        Box(
                            modifier =
                                Modifier
                                    .width(320.dp)
                                    .background(shadcnTheme.colors.surface, RoundedCornerShape(shadcnTheme.shapes.md))
                                    .border(1.dp, shadcnTheme.colors.border, RoundedCornerShape(shadcnTheme.shapes.md))
                                    .padding(shadcnTheme.spacing.lg),
                        ) {
                            panel()
                        }
                    }
                }
            }
        }
    }
}
