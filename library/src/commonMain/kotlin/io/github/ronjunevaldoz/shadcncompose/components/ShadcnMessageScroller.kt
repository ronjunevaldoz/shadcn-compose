package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import kotlinx.coroutines.launch

/**
 * How close to the bottom (in px) the user has to already be, right before new content
 * arrives, for [ShadcnMessageScroller] to auto-scroll them along with it -- and how close
 * they have to currently be for [ShadcnMessageScrollerButton] to hide itself.
 */
internal const val DEFAULT_AUTO_SCROLL_THRESHOLD_PX = 64

/** Which edge of the scroller a [ShadcnMessageScrollerButton] jumps to, matching real shadcn's `direction` prop. */
enum class ShadcnMessageScrollerDirection { Start, End }

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
 * Whether new content arriving should pull the scroll position along with it, given the
 * reader is currently [following] the live edge. Real shadcn's `message-scroller.tsx`
 * auto-scrolls to follow new messages *only* while the reader hasn't released the view --
 * if they've scrolled up into history, new messages must not yank them back down, even if
 * [scrollValue] happens to already be near [previousMaxValue] again.
 */
internal fun shouldAutoScrollToBottom(
    following: Boolean,
    previousMaxValue: Int,
    newMaxValue: Int,
): Boolean {
    if (!following) return false
    return newMaxValue > previousMaxValue
}

/**
 * Whether a user-driven scroll (not one this component just commanded itself via
 * `animateScrollTo`) should release the "follow new messages" state. Matches real
 * shadcn's documented semantics: "Scrolling away from the live edge releases the view,
 * whether by wheel, touch, keyboard scroll keys, or dragging the scrollbar" -- and,
 * critically, release is **sticky**: it does not silently re-engage just because the
 * reader happens to scroll back within [thresholdPx] of the bottom. Re-engaging requires
 * an explicit action ([ShadcnMessageScrollerButton] or `scrollToEnd()`), which is why this
 * function only ever returns `true` (a one-way transition) and callers should not use its
 * negation to resume following.
 */
internal fun shouldReleaseFollowing(
    value: Int,
    maxValue: Int,
    thresholdPx: Int,
): Boolean = !isMessageScrollerNearBottom(value, maxValue, thresholdPx)

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
 * [isMessageScrollerNearBottom], [shouldReleaseFollowing]) are all plain functions with
 * no Compose dependency, unit tested directly (`ShadcnMessageScrollerTest`) -- this
 * component has no drag gesture to avoid simulating live, but keeps the same "extract the
 * decision logic" discipline as the components that do.
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
    // Sticky: only ever set back to true by an explicit jump-to-bottom, never by the
    // reader passively scrolling back within range -- see shouldReleaseFollowing's doc.
    var isFollowing by remember { mutableStateOf(true) }
    // Distinguishes our own animateScrollTo from a real user gesture, so the release
    // check below doesn't mistake the programmatic scroll it just triggered for the
    // reader manually scrolling away.
    var isProgrammaticScroll by remember { mutableStateOf(false) }

    fun jumpToEnd() {
        coroutineScope.launch {
            isProgrammaticScroll = true
            isFollowing = true
            scrollState.animateScrollTo(scrollState.maxValue)
            isProgrammaticScroll = false
        }
    }

    LaunchedEffect(scrollState.maxValue) {
        val newMaxValue = scrollState.maxValue
        if (shouldAutoScrollToBottom(isFollowing, previousMaxValue, newMaxValue)) {
            isProgrammaticScroll = true
            scrollState.animateScrollTo(newMaxValue)
            isProgrammaticScroll = false
        }
        previousMaxValue = newMaxValue
    }

    LaunchedEffect(scrollState.value) {
        val released =
            !isProgrammaticScroll &&
                shouldReleaseFollowing(scrollState.value, scrollState.maxValue, autoScrollThresholdPx)
        if (released) {
            isFollowing = false
        }
    }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
            content = content,
        )
        val nearBottom = isMessageScrollerNearBottom(scrollState.value, scrollState.maxValue, autoScrollThresholdPx)
        ShadcnMessageScrollerButton(
            visible = scrollState.maxValue > 0 && !nearBottom,
            onClick = ::jumpToEnd,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = shadcnTheme.spacing.md),
        )
    }
}

/**
 * A floating "scroll to bottom"/"scroll to top" affordance. Normally rendered internally
 * by [ShadcnMessageScroller] for [ShadcnMessageScrollerDirection.End]; exposed separately
 * in case a caller wants a custom trigger, position, or a second [Start]-direction button
 * of their own (real shadcn's own `MessageScrollerButton` is likewise caller-composed,
 * not auto-rendered by `MessageScroller`, for exactly this reason).
 *
 * Animation matches real shadcn's `message-scroller.tsx` exactly, transcribed from its
 * source CSS (`data-[active=true]:...`/`data-[active=false]:...` on the real component):
 * showing slides+scales+fades in over 200ms with an ease-out curve
 * (`cubic-bezier(0.23,1,0.32,1)`); hiding is twice as slow (400ms) with an accelerating
 * ease-in curve (`cubic-bezier(0.7,0,0.84,0)`) -- not a symmetric fade.
 */
@Composable
fun ShadcnMessageScrollerButton(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    direction: ShadcnMessageScrollerDirection = ShadcnMessageScrollerDirection.End,
) {
    val showEasing = remember { CubicBezierEasing(0.23f, 1f, 0.32f, 1f) }
    val hideEasing = remember { CubicBezierEasing(0.7f, 0f, 0.84f, 0f) }
    // End slides up from below (offscreen below the viewport); Start slides down from above.
    val offscreenSign = if (direction == ShadcnMessageScrollerDirection.End) 1 else -1

    AnimatedVisibility(
        visible = visible,
        enter =
            slideInVertically(tween(200, easing = showEasing)) { fullHeight -> offscreenSign * fullHeight } +
                scaleIn(tween(200, easing = showEasing), initialScale = 0.95f) +
                fadeIn(tween(200, easing = showEasing)),
        exit =
            slideOutVertically(tween(400, easing = hideEasing)) { fullHeight -> offscreenSign * fullHeight } +
                scaleOut(tween(400, easing = hideEasing), targetScale = 0.95f) +
                fadeOut(tween(400, easing = hideEasing)),
        modifier = modifier,
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()
        Box(
            modifier =
                Modifier
                    .size(36.dp)
                    .background(
                        if (isHovered) shadcnTheme.colors.muted else shadcnTheme.colors.background,
                        RoundedCornerShape(shadcnTheme.shapes.lg),
                    )
                    .border(1.dp, shadcnTheme.colors.border, RoundedCornerShape(shadcnTheme.shapes.lg))
                    .hoverable(interactionSource)
                    .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            val rotation = if (direction == ShadcnMessageScrollerDirection.Start) 180f else 0f
            ShadcnMessageScrollerArrowIcon(rotationDegrees = rotation)
        }
    }
}

/**
 * A plain text glyph, not `tailwind-icons-outline`'s real `ArrowDown` `ImageVector` --
 * deliberate: this library takes no icon-set dependency (matching every other
 * icon-needing spot here, e.g. `ShadcnSidebarTrigger`'s "☰"), so `tailwind-icons-outline`
 * is used in the catalog app's *examples* only, never as a `:library` dependency.
 */
@Composable
private fun ShadcnMessageScrollerArrowIcon(rotationDegrees: Float) {
    ShadcnText("↓", style = ShadcnTextStyle.TitleMedium, modifier = Modifier.graphicsLayer(rotationZ = rotationDegrees))
}
