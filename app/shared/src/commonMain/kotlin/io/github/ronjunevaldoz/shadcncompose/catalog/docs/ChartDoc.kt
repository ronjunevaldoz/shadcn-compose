package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnBarChart
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnChartContainer
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnChartPoint
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnChartSeries
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnChartLegend
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

private val chartConfig =
    mapOf(
        "desktop" to ShadcnChartSeries("Desktop", Color(0xFF2563EB)),
        "mobile" to ShadcnChartSeries("Mobile", Color(0xFF60A5FA)),
    )

private val chartData =
    listOf(
        ShadcnChartPoint("Jan", mapOf("desktop" to 186f, "mobile" to 80f)),
        ShadcnChartPoint("Feb", mapOf("desktop" to 305f, "mobile" to 200f)),
        ShadcnChartPoint("Mar", mapOf("desktop" to 237f, "mobile" to 120f)),
        ShadcnChartPoint("Apr", mapOf("desktop" to 73f, "mobile" to 190f)),
        ShadcnChartPoint("May", mapOf("desktop" to 209f, "mobile" to 130f)),
    )

val chartDoc =
    ComponentDoc(
        id = "chart",
        title = "Chart",
        description =
            "A config-driven bar/line chart. Real shadcn/ui's chart.tsx wraps Recharts; " +
                "since there is no Compose Multiplatform equivalent, this draws directly via Canvas.",
        usageCode =
            """
            import io.github.ronjunevaldoz.shadcncompose.components.*

            val config = mapOf("desktop" to ShadcnChartSeries("Desktop", Color(0xFF2563EB)))
            val data = listOf(ShadcnChartPoint("Jan", mapOf("desktop" to 186f)))
            ShadcnChartContainer {
                ShadcnBarChart(data = data, config = config)
            }
            ShadcnChartLegend(config = config)
            """.trimIndent(),
        examples =
            listOf(
                ComponentExample(
                    title = "Bar chart",
                    code =
                        """
                        ShadcnChartContainer {
                            ShadcnBarChart(data = chartData, config = chartConfig)
                        }
                        ShadcnChartLegend(config = chartConfig, modifier = Modifier.padding(top = 8.dp))
                        """.trimIndent(),
                    preview = {
                        Column {
                            ShadcnChartContainer {
                                ShadcnBarChart(data = chartData, config = chartConfig)
                            }
                            ShadcnChartLegend(
                                config = chartConfig,
                                modifier = Modifier.padding(top = shadcnTheme.spacing.sm),
                            )
                        }
                    },
                ),
            ),
    )
