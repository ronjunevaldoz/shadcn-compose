package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import kotlinx.coroutines.launch

/**
 * How close to the bottom (in px) the user has to already be, right before new content
 * arrives, for [ShadcnMessageScroller] to auto-scroll them along with it -- and how close
 * they have to currently be for [ShadcnMessageScrollerButton] to hide itself.
 */
internal const val DEFAULT_AUTO_SCROLL_THRESHOLD_PX = 64

/**
 * Whether the scroll position is within [thresholdPx] of the bottom of the content, as a
 * plain function with no Compose dependency -- drives [ShadcnMessageScrollerButton]'s
 * visibility (shown only once scrolled meaningfully away from the bottom).
 */
internal fun isMessageScrollerNearBottom(
    value: Int,
    maxValue: Int,
    thresholdPx: Int,
): Boolean = (maxValue - value) <= thresholdPx

/**
 * Whether new content arriving should pull the scroll position along with it. Real
 * shadcn's `message-scroller.tsx` (via `@shadcn/react/message-scroller`) auto-scrolls to
 * follow new messages *only* if the user was already reading near the bottom -- if
 * they've scrolled up into history, new messages must not yank them back down. Compares
 * against [previousMaxValue] (the scrollable extent *before* this growth), not
 * [newMaxValue], since [scrollValue] itself hasn't moved yet by the time this is checked.
 */
internal fun shouldAutoScrollToBottom(
    scrollValue: Int,
    previousMaxValue: Int,
    newMaxValue: Int,
    thresholdPx: Int,
): Boolean {
    if (newMaxValue <= previousMaxValue) return false
    return (previousMaxValue - scrollValue) <= thresholdPx
}

/**
 * A chat-transcript scroll container that follows new messages to the bottom -- but only
 * while the reader hasn't scrolled away to read history -- with a floating "jump to
 * bottom" button when they have. Part of shadcn's "AI Elements" family, matching real
 * shadcn/ui's `message-scroller.tsx` (a wrapper over the separate `@shadcn/react/
 * message-scroller` npm package there; reimplemented directly here on Compose's own
 * [ScrollState][androidx.compose.foundation.ScrollState], since no equivalent package
 * exists for Compose Multiplatform).
 *
 * The auto-scroll/button-visibility *decisions* ([shouldAutoScrollToBottom],
 * [isMessageScrollerNearBottom]) are both plain functions with no Compose dependency,
 * unit tested directly (`ShadcnMessageScrollerTest`) -- this component has no drag
 * gesture to avoid simulating live, but keeps the same "extract the decision logic"
 * discipline as the components that do.
 *
 * Usage:
 * ```
 * ShadcnMessageScroller(modifier = Modifier.fillMaxSize()) {
 *     messages.forEach { ShadcnMessage(...) { ... } }
 * }
 * ```
 */
@Composable
fun ShadcnMessageScroller(
    modifier: Modifier = Modifier,
    autoScrollThresholdPx: Int = DEFAULT_AUTO_SCROLL_THRESHOLD_PX,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var previousMaxValue by remember { mutableIntStateOf(0) }

    LaunchedEffect(scrollState.maxValue) {
        val newMaxValue = scrollState.maxValue
        if (shouldAutoScrollToBottom(scrollState.value, previousMaxValue, newMaxValue, autoScrollThresholdPx)) {
            scrollState.animateScrollTo(newMaxValue)
        }
        previousMaxValue = newMaxValue
    }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
            content = content,
        )
        val nearBottom = isMessageScrollerNearBottom(scrollState.value, scrollState.maxValue, autoScrollThresholdPx)
        ShadcnMessageScrollerButton(
            visible = scrollState.maxValue > 0 && !nearBottom,
            onClick = { coroutineScope.launch { scrollState.animateScrollTo(scrollState.maxValue) } },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = shadcnTheme.spacing.md),
        )
    }
}

/**
 * A floating "scroll to bottom" affordance, fading in/out as [visible] changes. Normally
 * rendered internally by [ShadcnMessageScroller]; exposed separately in case a caller
 * wants a custom trigger/position.
 */
@Composable
fun ShadcnMessageScrollerButton(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut(), modifier = modifier) {
        val interactionSource = remember { MutableInteractionSource() }
        Box(
            modifier =
                Modifier
                    .size(36.dp)
                    .background(shadcnTheme.colors.background, CircleShape)
                    .border(1.dp, shadcnTheme.colors.border, CircleShape)
                    .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            ShadcnText("↓", style = ShadcnTextStyle.TitleMedium)
        }
    }
}
