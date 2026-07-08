package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.styles.shadcnScrollFade

val scrollFadeDoc =
    ComponentDoc(
        id = "scroll-fade",
        title = "Scroll Fade",
        description =
            "A scroll-position-aware fade at the edges of a scrollable container, hinting overflow " +
                "without a hard cut -- a modifier, not a standalone component.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.styles.shadcnScrollFade

            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier.height(160.dp).verticalScroll(scrollState).shadcnScrollFade(scrollState),
            ) {
                repeat(20) { ShadcnText("Row ${'$'}it") }
            }
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Default",
                    code =
                        """
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier.width(240.dp).height(160.dp)
                                .verticalScroll(scrollState).shadcnScrollFade(scrollState),
                        ) {
                            repeat(20) { index -> ShadcnText("Row ${'$'}index", modifier = Modifier.padding(vertical = 4.dp)) }
                        }
                        """.trimIndent(),
                    preview = {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier =
                                Modifier
                                    .width(240.dp)
                                    .height(160.dp)
                                    .verticalScroll(scrollState)
                                    .shadcnScrollFade(scrollState),
                        ) {
                            repeat(20) { index ->
                                ShadcnText("Row $index", modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    },
                ),
            ),
    )
