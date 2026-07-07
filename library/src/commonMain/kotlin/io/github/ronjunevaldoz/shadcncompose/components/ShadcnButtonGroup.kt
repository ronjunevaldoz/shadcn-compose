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
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * A container that visually joins a row (or column) of buttons into a single
 * segmented control.
 *
 * Two ways to use this:
 * 1. **`items` overload** (below) -- for a plain row of same-shaped buttons, this
 *    computes each item's real per-position corners the same way [ShadcnToggleGroup]
 *    already does for [ShadcnToggle]: real shadcn/ui strips each button's own corners
 *    and left-border per position (`:not(:first-child):rounded-l-none`, etc.) rather
 *    than drawing one shared border around an unmodified row -- get that for free here.
 * 2. **Flexible-content overload** (further below) -- for mixed compositions (an
 *    `Input` between buttons, a dropdown menu, [ShadcnButtonGroupSeparator]s), matching
 *    real shadcn's actual `ButtonGroup > Button | Input | ButtonGroupSeparator | ...`
 *    composition pattern. This one draws a single shared outer border/clip instead,
 *    since arbitrary child content can't be corner-stripped generically without
 *    knowing its type -- callers should use [ButtonVariant.Ghost] (borderless,
 *    backgroundless at rest) for its children so only the group's own outline shows.
 *
 * Note: the `items` overload strips each item's own *corner radius* per position but
 * not its per-side *border* the way real shadcn's CSS does (`border-l-0` on every item
 * but the first) -- [ButtonVariant.Ghost] has no border to begin with so this doesn't
 * matter for the default/recommended variant, but grouping [ButtonVariant.Outline]
 * items will show each item's own full border, not a single shared seam line. This
 * matches the same simplification [ShadcnToggleGroup] already makes for its own
 * `Outline` variant.
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
 *
 *  TODO bugfix: a lot visual bug found: please see original reference https://ui.shadcn.com/docs/components/base/button-group
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

    fun cornersFor(index: Int): ButtonGroupCorners =
        when {
            items.lastIndex == 0 -> ButtonGroupCorners(rounded, rounded, rounded, rounded)
            orientation == ButtonGroupOrientation.Horizontal ->
                when (index) {
                    0 -> ButtonGroupCorners(topStart = rounded, topEnd = none, bottomEnd = none, bottomStart = rounded)
                    items.lastIndex ->
                        ButtonGroupCorners(topStart = none, topEnd = rounded, bottomEnd = rounded, bottomStart = none)
                    else -> ButtonGroupCorners(none, none, none, none)
                }
            else ->
                when (index) {
                    0 -> ButtonGroupCorners(topStart = rounded, topEnd = rounded, bottomEnd = none, bottomStart = none)
                    items.lastIndex ->
                        ButtonGroupCorners(topStart = none, topEnd = none, bottomEnd = rounded, bottomStart = rounded)
                    else -> ButtonGroupCorners(none, none, none, none)
                }
        }

    val content: @Composable () -> Unit = {
        items.forEachIndexed { index, item ->
            val (topStart, topEnd, bottomEnd, bottomStart) = cornersFor(index)
            val itemShape = RoundedCornerShape(topStart, topEnd, bottomEnd, bottomStart)
            ShadcnButton(
                onClick = item.onClick,
                enabled = item.enabled,
                variant = item.variant,
                ringTopStartCorner = topStart,
                ringTopEndCorner = topEnd,
                ringBottomEndCorner = bottomEnd,
                ringBottomStartCorner = bottomStart,
                style = Style { shape(itemShape) },
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
 * See the [items]-overload doc above for why this one draws a single shared border
 * instead of stripping each child's own corners.
 */
@Composable
fun ShadcnButtonGroup(
    modifier: Modifier = Modifier,
    orientation: ButtonGroupOrientation = ButtonGroupOrientation.Horizontal,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(shadcnTheme.shapes.lg)
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
        ShadcnText(text, style = ShadcnTextStyle.LabelLarge, modifier = Modifier)
    }
}

private data class ButtonGroupCorners(
    val topStart: Dp,
    val topEnd: Dp,
    val bottomEnd: Dp,
    val bottomStart: Dp,
)



@OptIn(ExperimentalFoundationStyleApi::class)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun ButtonGroupDocPreview() {
    // TODO should not use this if we want to have a button proper division, rather use items
    ShadcnButtonGroup {
        ShadcnButtonGroupText("https://")
        ShadcnButtonGroupSeparator()
        ShadcnButton(
            onClick = {},
            variant = ButtonVariant.Ghost,
            style = Style {
                // TODO workaround so that last item has no separator
                val itemShape = RoundedCornerShape(0.dp, shadcnTheme.shapes.lg, 0.dp, shadcnTheme.shapes.lg)
                shape(itemShape)
            }
        ) { ShadcnText("example.com") }
    }
}