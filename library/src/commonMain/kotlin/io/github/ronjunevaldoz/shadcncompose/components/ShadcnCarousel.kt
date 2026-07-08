@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonSize
import io.github.ronjunevaldoz.shadcncompose.styles.ButtonVariant
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import kotlinx.coroutines.launch

/**
 * A snap-scrolling, swipeable item carousel. Real shadcn/ui's `carousel.tsx` wraps
 * `embla-carousel-react`; Compose Multiplatform's own [HorizontalPager]/[VerticalPager]
 * already provide the same snap-per-item scrolling behavior natively, so this wraps
 * those directly rather than reimplementing snap physics.
 *
 * [modifier] must constrain the main-axis size (`width` for [Orientation.Horizontal],
 * `height` for [Orientation.Vertical]) -- this composable fills whatever it's given
 * ([HorizontalPager]/[VerticalPager] need a bounded main-axis size to know each page's
 * own size), so an unconstrained caller silently gets a carousel that expands to the
 * full ambient width/height instead of a sensible card-like size.
 *
 * Usage:
 * ```
 * val state = rememberPagerState { items.size }
 * ShadcnCarousel(state = state, modifier = Modifier.width(280.dp).height(160.dp)) {
 *     page -> ShadcnText(items[page])
 * }
 * ```
 */
@Composable
fun ShadcnCarousel(
    state: PagerState,
    modifier: Modifier = Modifier,
    orientation: Orientation = Orientation.Horizontal,
    pageContent: @Composable (page: Int) -> Unit,
) {
    if (orientation == Orientation.Horizontal) {
        HorizontalPager(state = state, modifier = modifier.fillMaxSize()) { page -> pageContent(page) }
    } else {
        VerticalPager(state = state, modifier = modifier.fillMaxSize()) { page -> pageContent(page) }
    }
}

/** A circular Previous button that scrolls a [ShadcnCarousel]'s [state] back one page. */
@Composable
fun ShadcnCarouselPrevious(
    state: PagerState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    ShadcnButton(
        onClick = { scope.launch { state.animateScrollToPage((state.currentPage - 1).coerceAtLeast(0)) } },
        modifier = modifier.size(32.dp),
        enabled = state.currentPage > 0,
        variant = ButtonVariant.Outline,
        size = ButtonSize.Icon,
    ) {
        ShadcnText("‹", style = ShadcnTextStyle.TitleMedium)
    }
}

/** A circular Next button that scrolls a [ShadcnCarousel]'s [state] forward one page. */
@Composable
fun ShadcnCarouselNext(
    state: PagerState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val canScrollNext = state.currentPage < state.pageCount - 1
    ShadcnButton(
        onClick = {
            scope.launch { state.animateScrollToPage((state.currentPage + 1).coerceAtMost(state.pageCount - 1)) }
        },
        modifier = modifier.size(32.dp),
        enabled = canScrollNext,
        variant = ButtonVariant.Outline,
        size = ButtonSize.Icon,
    ) {
        ShadcnText("›", style = ShadcnTextStyle.TitleMedium)
    }
}

/** A row of small dots indicating the current page in a [ShadcnCarousel]. */
@Composable
fun ShadcnCarouselDots(
    state: PagerState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xs),
    ) {
        repeat(state.pageCount) { index ->
            val interactionSource = remember { MutableInteractionSource() }
            val isActive = index == state.currentPage
            Box(
                modifier =
                    Modifier
                        .size(if (isActive) 8.dp else 6.dp)
                        .background(
                            if (isActive) shadcnTheme.colors.primary else shadcnTheme.colors.border,
                            CircleShape,
                        )
                        .clickable(interactionSource = interactionSource, indication = null) {
                            scope.launch { state.animateScrollToPage(index) }
                        },
            )
        }
    }
}
