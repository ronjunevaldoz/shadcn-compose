@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCarousel
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCarouselDots
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCarouselNext
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCarouselPrevious
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

val carouselDoc =
    ComponentDoc(
        id = "carousel",
        title = "Carousel",
        description = "A swipeable, snap-scrolling set of slides, built directly on Compose's own Pager.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCarousel
            import androidx.compose.foundation.pager.rememberPagerState

            val state = rememberPagerState { 5 }
            ShadcnCarousel(state = state) { page -> ShadcnText("Slide ${'$'}page") }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        val state = rememberPagerState { 5 }
                        // Fixed width: HorizontalPager needs a bounded main-axis size to know
                        // its own page width -- without one it silently expands to fill
                        // whatever ambient width it's given, which is rarely what you want.
                        Column(
                            modifier = Modifier.width(280.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            ShadcnCarousel(state = state, modifier = Modifier.fillMaxWidth().height(120.dp)) { page ->
                                Box(Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                                    ShadcnText("Slide ${'$'}{page + 1}", style = ShadcnTextStyle.TitleLarge)
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ShadcnCarouselPrevious(state = state)
                                ShadcnCarouselNext(state = state)
                            }
                            ShadcnCarouselDots(state = state)
                        }
                        """.trimIndent(),
                    preview = {
                        val state = rememberPagerState { 5 }
                        Column(
                            modifier = Modifier.width(280.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            ShadcnCarousel(
                                state = state,
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                            ) { page ->
                                Box(
                                    Modifier
                                        .padding(8.dp)
                                        .background(
                                            shadcnTheme.colors.muted,
                                            RoundedCornerShape(shadcnTheme.shapes.md),
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    ShadcnText("Slide ${page + 1}", style = ShadcnTextStyle.TitleLarge)
                                }
                            }
                            Row(
                                modifier = Modifier.padding(top = shadcnTheme.spacing.sm),
                                horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.sm),
                            ) {
                                ShadcnCarouselPrevious(state = state)
                                ShadcnCarouselNext(state = state)
                            }
                            ShadcnCarouselDots(state = state, modifier = Modifier.padding(top = shadcnTheme.spacing.sm))
                        }
                    },
                ),
            ),
    )
