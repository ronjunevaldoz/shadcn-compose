@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnCard
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
                        // Matches real shadcn's own default demo: a Card per slide, roughly
                        // square (real shadcn: `aspect-square`), a big bold slide number, and
                        // circular Previous/Next buttons overlaid on the carousel's edges
                        // (real shadcn: `absolute -left-12`/`-right-12`) rather than a row
                        // below it.
                        val state = rememberPagerState { 5 }
                        Box(
                            modifier = Modifier.width(200.dp).height(200.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            ShadcnCarousel(state = state, modifier = Modifier.fillMaxSize()) { page ->
                                ShadcnCard(modifier = Modifier.fillMaxSize()) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        ShadcnText("${'$'}{page + 1}", style = ShadcnTextStyle.DisplayMedium)
                                    }
                                }
                            }
                            ShadcnCarouselPrevious(
                                state = state,
                                modifier = Modifier.align(Alignment.CenterStart).offset(x = (-40).dp),
                            )
                            ShadcnCarouselNext(
                                state = state,
                                modifier = Modifier.align(Alignment.CenterEnd).offset(x = 40.dp),
                            )
                        }
                        """.trimIndent(),
                    preview = {
                        val state = rememberPagerState { 5 }
                        Box(
                            modifier = Modifier.width(200.dp).height(200.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            ShadcnCarousel(state = state, modifier = Modifier.fillMaxSize()) { page ->
                                ShadcnCard(modifier = Modifier.fillMaxSize()) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        ShadcnText("${page + 1}", style = ShadcnTextStyle.DisplayMedium)
                                    }
                                }
                            }
                            ShadcnCarouselPrevious(
                                state = state,
                                modifier = Modifier.align(Alignment.CenterStart).offset(x = (-40).dp),
                            )
                            ShadcnCarouselNext(
                                state = state,
                                modifier = Modifier.align(Alignment.CenterEnd).offset(x = 40.dp),
                            )
                        }
                    },
                ),
                ComponentExample(
                    title = "With dot indicators",
                    code =
                        """
                        // ShadcnCarouselDots has no real shadcn/ui equivalent -- real
                        // carousel.tsx ships only Previous/Next, no pagination dots. This is
                        // a deliberate addition, not a parity gap: a common carousel pattern
                        // worth having, shown separately so the "Default" example above stays
                        // a faithful match to the real demo.
                        val state = rememberPagerState { 5 }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ShadcnCarousel(state = state, modifier = Modifier.width(280.dp).height(120.dp)) { page ->
                                Box(Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                                    ShadcnText("Slide ${'$'}{page + 1}", style = ShadcnTextStyle.TitleLarge)
                                }
                            }
                            ShadcnCarouselDots(state = state, modifier = Modifier.padding(top = 8.dp))
                        }
                        """.trimIndent(),
                    preview = {
                        val state = rememberPagerState { 5 }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ShadcnCarousel(
                                state = state,
                                modifier = Modifier.width(280.dp).height(120.dp),
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
                            ShadcnCarouselDots(
                                state = state,
                                modifier = Modifier.padding(top = shadcnTheme.spacing.sm),
                            )
                        }
                    },
                ),
            ),
    )
