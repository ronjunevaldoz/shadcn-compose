package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

enum class ButtonGroupOrientation { Horizontal, Vertical }

/**
 * A container that visually joins a row (or column) of buttons into a single
 * segmented control. Real shadcn/ui gives every button its own border and drops
 * the shared edge per-side via CSS; the Style API has no per-side border control,
 * so the group draws one shared border and buttons inside should use
 * [io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant.Ghost] (borderless)
 * so only the group's outline shows.
 *
 * Usage:
 * ```
 * ShadcnButtonGroup {
 *     ShadcnButton(onClick = {}, variant = ButtonVariant.Ghost) { ShadcnText("Copy") }
 *     ShadcnButtonGroupSeparator()
 *     ShadcnButton(onClick = {}, variant = ButtonVariant.Ghost) { ShadcnText("Share") }
 * }
 * ```
 */
@Composable
fun ShadcnButtonGroup(
    modifier: Modifier = Modifier,
    orientation: ButtonGroupOrientation = ButtonGroupOrientation.Horizontal,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(shadcnTheme.shapes.md)
    val groupModifier =
        modifier
            .clip(shape)
            .border(1.dp, shadcnTheme.colors.border, shape)

    when (orientation) {
        ButtonGroupOrientation.Horizontal -> Row(modifier = groupModifier) { content() }
        ButtonGroupOrientation.Vertical -> Column(modifier = groupModifier) { content() }
    }
}

/** A thin divider between items in a [ShadcnButtonGroup]; insert manually where wanted. */
@Composable
fun ShadcnButtonGroupSeparator(
    modifier: Modifier = Modifier,
    orientation: ButtonGroupOrientation = ButtonGroupOrientation.Vertical,
) {
    val sizeModifier =
        when (orientation) {
            ButtonGroupOrientation.Vertical -> Modifier.width(1.dp).fillMaxHeight()
            ButtonGroupOrientation.Horizontal -> Modifier.fillMaxWidth().width(1.dp)
        }
    Box(
        modifier =
            modifier
                .then(sizeModifier)
                .background(shadcnTheme.colors.border),
    )
}

/** A static, non-interactive label chip for prefixing/suffixing a [ShadcnButtonGroup]. */
@Composable
fun ShadcnButtonGroupText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .background(shadcnTheme.colors.muted)
                .padding(horizontal = shadcnTheme.spacing.lg, vertical = shadcnTheme.spacing.sm),
    ) {
        ShadcnText(text, style = ShadcnTextStyle.LabelLarge)
    }
}
