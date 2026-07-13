@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.icons.Menu
import io.github.ronjunevaldoz.shadcncompose.icons.ShadcnGlyphIcon
import io.github.ronjunevaldoz.shadcncompose.styles.focusRing
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/** Whether a [ShadcnSidebar] is expanded (full width) or collapsed (width 0), plus how to toggle it. */
class ShadcnSidebarState(val expanded: Boolean, val toggle: () -> Unit)

private val LocalShadcnSidebarState =
    compositionLocalOf<ShadcnSidebarState> {
        error("ShadcnSidebar/ShadcnSidebarTrigger must be used within a ShadcnSidebarProvider")
    }

/**
 * Hoists open/collapsed state for a [ShadcnSidebar] + main content pair, matching real
 * shadcn/ui's `SidebarProvider`/`useSidebar()`. Real shadcn additionally has a
 * mobile-breakpoint Sheet variant, an "icon" collapse mode (narrow rail instead of fully
 * hidden), a cookie-persisted state, and a Cmd/Ctrl+B keyboard shortcut -- none of which
 * have a clean cross-platform equivalent here (no cookies, no reliable global keyboard
 * hook across Android/iOS/Desktop/Web), so this keeps the two states real shadcn always
 * supports everywhere: fully expanded or fully collapsed.
 *
 * Usage:
 * ```
 * var expanded by remember { mutableStateOf(true) }
 * ShadcnSidebarProvider(expanded = expanded, onExpandedChange = { expanded = it }) {
 *     ShadcnSidebar { ShadcnSidebarMenu(items = ..., activeId = ..., onItemClick = ...) }
 *     ShadcnSidebarInset { ShadcnSidebarTrigger(); ShadcnText("Main content") }
 * }
 * ```
 */
@Composable
fun ShadcnSidebarProvider(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val state = remember(expanded, onExpandedChange) { ShadcnSidebarState(expanded) { onExpandedChange(!expanded) } }
    CompositionLocalProvider(LocalShadcnSidebarState provides state) {
        Row(modifier = modifier.fillMaxSize()) { content() }
    }
}

/** The collapsible side rail itself -- must be a direct child of [ShadcnSidebarProvider]. */
@Composable
fun ShadcnSidebar(
    modifier: Modifier = Modifier,
    width: Dp = 240.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val state = LocalShadcnSidebarState.current
    val animatedWidth by animateDpAsState(if (state.expanded) width else 0.dp)
    val sidebarBorderColor = shadcnTheme.colors.sidebarBorder
    Box(
        modifier =
            modifier
                .width(animatedWidth)
                .fillMaxHeight()
                .background(shadcnTheme.colors.sidebar)
                .drawBehind {
                    drawLine(
                        color = sidebarBorderColor,
                        start = Offset(size.width, 0f),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx(),
                    )
                },
    ) {
        if (state.expanded) {
            Column(
                modifier = Modifier.fillMaxSize().width(width).padding(shadcnTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm),
                content = content,
            )
        }
    }
}

/** The main content area beside the sidebar, matching real shadcn's `SidebarInset`. */
@Composable
fun ShadcnSidebarInset(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxHeight().fillMaxWidth().background(shadcnTheme.colors.background),
        content = content,
    )
}

/** A hamburger-style button that toggles the nearest [ShadcnSidebarProvider]'s expanded state. */
@Composable
fun ShadcnSidebarTrigger(
    modifier: Modifier = Modifier,
    // A self-generated ImageVector, not a third-party icon-set dependency (this library
    // still takes none -- see README) -- text glyphs don't render on WasmJS (Skia has no
    // browser emoji-font fallback). Override with any icon set (e.g. this repo's own demo
    // app passes a real heroicons-outline Bars3 here -- see SidebarDoc.kt).
    icon: @Composable () -> Unit = { ShadcnGlyphIcon(Menu, tint = shadcnTheme.colors.onSurface) },
) {
    val state = LocalShadcnSidebarState.current
    val interactionSource = remember { MutableInteractionSource() }
    val theme = shadcnTheme
    val styleState = remember { MutableStyleState(interactionSource) }
    val triggerStyle =
        remember(theme) {
            Style {
                focusRing(RoundedCornerShape(theme.shapes.md))
            }
        }
    Box(
        modifier =
            modifier
                .styleable(styleState, triggerStyle)
                .clip(RoundedCornerShape(shadcnTheme.shapes.md))
                .clickable(interactionSource = interactionSource, indication = null, onClick = state.toggle)
                .padding(shadcnTheme.spacing.xs),
    ) {
        icon()
    }
}

@Composable
fun ShadcnSidebarHeader(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth(), content = content)
}

@Composable
fun ShadcnSidebarFooter(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth(), content = content)
}

/** A labeled section within the sidebar body, matching real shadcn's `SidebarGroup`/`SidebarGroupLabel`. */
@Composable
fun ShadcnSidebarGroup(
    modifier: Modifier = Modifier,
    label: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xxs),
    ) {
        if (label != null) {
            ShadcnText(
                label,
                style = ShadcnTextStyle.LabelSmall,
                muted = true,
                modifier = Modifier.padding(horizontal = shadcnTheme.spacing.xs, vertical = shadcnTheme.spacing.xs),
            )
        }
        content()
    }
}

/** One clickable destination inside a [ShadcnSidebarGroup]. [badge] renders as a trailing [ShadcnBadge], e.g. "New". */
data class ShadcnSidebarMenuItem(val id: String, val label: String, val badge: String? = null)

/** A vertical list of [ShadcnSidebarMenuItem]s with one active/highlighted entry. */
@Composable
fun ShadcnSidebarMenu(
    items: List<ShadcnSidebarMenuItem>,
    activeId: String?,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xxs)) {
        items.forEach { item ->
            val isActive = item.id == activeId
            val interactionSource = remember { MutableInteractionSource() }
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(shadcnTheme.shapes.md))
                        .let { if (isActive) it.background(shadcnTheme.colors.sidebarAccent) else it }
                        .clickable(interactionSource = interactionSource, indication = null) { onItemClick(item.id) }
                        .padding(horizontal = shadcnTheme.spacing.sm, vertical = shadcnTheme.spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                ShadcnText(
                    item.label,
                    style = ShadcnTextStyle.LabelLarge,
                    muted = !isActive,
                    color = if (isActive) shadcnTheme.colors.onSidebarAccent else Color.Unspecified,
                )
                item.badge?.let { ShadcnBadge { ShadcnText(it, style = ShadcnTextStyle.LabelSmall) } }
            }
        }
    }
}
