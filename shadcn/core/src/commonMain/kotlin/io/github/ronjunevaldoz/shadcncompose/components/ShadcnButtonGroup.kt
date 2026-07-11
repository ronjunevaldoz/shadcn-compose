package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

enum class ButtonGroupOrientation { Horizontal, Vertical }

/** A single button in a [ShadcnButtonGroup]'s `items` overload. */
data class ButtonGroupItem(
    val label: String,
    val onClick: () -> Unit,
    val variant: ButtonVariant = ButtonVariant.Ghost,
    val enabled: Boolean = true,
)

/**
 * Internal state so children of a flexible [ShadcnButtonGroup] can adapt their
 * borders and corners.
 */
internal val LocalButtonGroupOrientation = compositionLocalOf { ButtonGroupOrientation.Horizontal }

/**
 * A container that visually joins a row (or column) of buttons into a single
 * segmented control.
 *
 * Two ways to use this:
 * 1. **`items` overload** (below) -- for a plain row of same-shaped buttons, this
 *    computes each item's real per-position corners. It also handles the "negative
 *    margin" trick (1dp offset) to prevent double borders between [ButtonVariant.Outline]
 *    items, matching real shadcn/ui's CSS behavior.
 * 2. **Flexible-content overload** (further below) -- for mixed compositions (an
 *    `Input` between buttons, a dropdown menu, [ShadcnButtonGroupSeparator]s).
 *
 * Usage:
 * ```
 * ShadcnButtonGroup(
 *     items = listOf(
 *         ButtonGroupItem("Copy", onClick = {}),
 *         ButtonGroupItem("Share", onClick = {}),
 *     ),
 * )
 * ```
 */
@OptIn(ExperimentalFoundationStyleApi::class)
@Composable
fun ShadcnButtonGroup(
    items: List<ButtonGroupItem>,
    modifier: Modifier = Modifier,
    orientation: ButtonGroupOrientation = ButtonGroupOrientation.Horizontal,
) {
    val rounded = shadcnTheme.shapes.lg
    val none = 0.dp

    fun cornersFor(index: Int): ShadcnGroupCorners =
        when {
            items.size <= 1 -> ShadcnGroupCorners(rounded, rounded, rounded, rounded)
            orientation == ButtonGroupOrientation.Horizontal ->
                when (index) {
                    0 -> ShadcnGroupCorners(topStart = rounded, topEnd = none, bottomEnd = none, bottomStart = rounded)
                    items.lastIndex ->
                        ShadcnGroupCorners(topStart = none, topEnd = rounded, bottomEnd = rounded, bottomStart = none)
                    else -> ShadcnGroupCorners(none, none, none, none)
                }
            else ->
                when (index) {
                    0 -> ShadcnGroupCorners(topStart = rounded, topEnd = rounded, bottomEnd = none, bottomStart = none)
                    items.lastIndex ->
                        ShadcnGroupCorners(topStart = none, topEnd = none, bottomEnd = rounded, bottomStart = rounded)
                    else -> ShadcnGroupCorners(none, none, none, none)
                }
        }

    val content: @Composable () -> Unit = {
        items.forEachIndexed { index, item ->
            val corners = cornersFor(index)
            val itemShape = RoundedCornerShape(corners.topStart, corners.topEnd, corners.bottomEnd, corners.bottomStart)

            // Prevent double borders for Outline variants by overlapping them by 1dp
            val overlapModifier =
                if (index > 0 && item.variant == ButtonVariant.Outline) {
                    if (orientation == ButtonGroupOrientation.Horizontal) {
                        Modifier.offset(x = (-1).dp)
                    } else {
                        Modifier.offset(y = (-1).dp)
                    }
                } else {
                    Modifier
                }

            ShadcnButton(
                onClick = item.onClick,
                enabled = item.enabled,
                variant = item.variant,
                style = Style { shape(itemShape) },
                modifier = overlapModifier,
            ) {
                ShadcnText(item.label)
            }
        }
    }

    when (orientation) {
        ButtonGroupOrientation.Horizontal -> Row(modifier = modifier) { content() }
        ButtonGroupOrientation.Vertical -> Column(modifier = modifier) { content() }
    }
}

/**
 * Flexible-content overload -- for mixed compositions (an `Input` between buttons, a
 * dropdown menu, [ShadcnButtonGroupSeparator]s) matching real shadcn's actual
 * `ButtonGroup > Button | Input | ButtonGroupSeparator | ...` composition pattern.
 *
 * NOTE: Unlike the `items` overload, this one cannot automatically calculate per-item
 * corners for arbitrary content. Callers should manually round the outer items or
 * use [ButtonVariant.Ghost] children.
 */
@Composable
fun ShadcnButtonGroup(
    modifier: Modifier = Modifier,
    orientation: ButtonGroupOrientation = ButtonGroupOrientation.Horizontal,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(shadcnTheme.shapes.lg)
    // We don't .clip(shape) here because focus rings drawn by children would be clipped.
    // Instead, the container just draws the border.
    val groupModifier =
        modifier.border(1.dp, shadcnTheme.colors.border, shape)

    CompositionLocalProvider(LocalButtonGroupOrientation provides orientation) {
        when (orientation) {
            // IntrinsicSize.Min on the cross axis: ShadcnButtonGroupSeparator uses
            // fillMaxHeight()/fillMaxWidth() to span its siblings, which needs the
            // Row/Column's own size to be bounded by its *other* children first --
            // without this, a Row in an unbounded-height container (e.g. a scrollable
            // Column) has nothing for fillMaxHeight() to resolve against and the whole
            // group stretches to fill all available space instead of wrapping content.
            ButtonGroupOrientation.Horizontal ->
                Row(modifier = groupModifier.height(IntrinsicSize.Min)) { content() }
            ButtonGroupOrientation.Vertical ->
                Column(modifier = groupModifier.width(IntrinsicSize.Min)) { content() }
        }
    }
}

/** A thin divider between items in a [ShadcnButtonGroup]; insert manually where wanted. */
@Composable
fun ShadcnButtonGroupSeparator(modifier: Modifier = Modifier) {
    val orientation = LocalButtonGroupOrientation.current
    val sizeModifier =
        when (orientation) {
            ButtonGroupOrientation.Horizontal -> Modifier.width(1.dp).fillMaxHeight()
            ButtonGroupOrientation.Vertical -> Modifier.fillMaxWidth().height(1.dp)
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
    topStart: Dp = 0.dp,
    topEnd: Dp = 0.dp,
    bottomEnd: Dp = 0.dp,
    bottomStart: Dp = 0.dp,
) {
    Box(
        modifier =
            modifier
                .background(
                    color = shadcnTheme.colors.muted,
                    shape = RoundedCornerShape(topStart, topEnd, bottomEnd, bottomStart),
                )
                .padding(horizontal = shadcnTheme.spacing.lg, vertical = shadcnTheme.spacing.sm),
    ) {
        ShadcnText(text, style = ShadcnTextStyle.LabelLarge, modifier = Modifier)
    }
}

@OptIn(ExperimentalFoundationStyleApi::class)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun ButtonGroupDocPreview() {
    val rounded = shadcnTheme.shapes.lg
    ShadcnButtonGroup {
        ShadcnButtonGroupText("https://", topStart = rounded, bottomStart = rounded)
        ShadcnButtonGroupSeparator()
        ShadcnButton(
            onClick = {},
            variant = ButtonVariant.Ghost,
            style =
                Style {
                    shape(RoundedCornerShape(0.dp, rounded, rounded, 0.dp))
                },
        ) { ShadcnText("example.com") }
    }
}
