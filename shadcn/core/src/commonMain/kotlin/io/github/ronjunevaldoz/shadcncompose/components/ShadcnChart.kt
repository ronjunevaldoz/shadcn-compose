package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import kotlin.math.roundToInt

/**
 * One labeled, colored data series in a chart. Mirrors real shadcn/ui's `chart.tsx`
 * `ChartConfig` (a `Record<dataKey, { label, color }>`) -- since there is no Compose
 * Multiplatform equivalent of Recharts (the library `chart.tsx` itself wraps), the
 * actual bars/lines here are drawn directly via [Canvas] rather than delegated to a
 * charting library, but the config-driven series-to-color/label mapping is the same
 * shape as the real API.
 */
data class ShadcnChartSeries(val label: String, val color: Color)

/** dataKey -> series config, matching real shadcn's `ChartConfig`. */
typealias ShadcnChartConfig = Map<String, ShadcnChartSeries>

/** One x-axis category with one value per series key present in the owning [ShadcnChartConfig]. */
data class ShadcnChartPoint(val label: String, val values: Map<String, Float>)

/**
 * Fixed aspect-ratio frame for chart content, matching real shadcn's `ChartContainer`
 * (`aspect-video` by default).
 */
@Composable
fun ShadcnChartContainer(
    modifier: Modifier = Modifier,
    aspectRatio: Float = 16f / 9f,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.fillMaxWidth().aspectRatio(aspectRatio)) {
        content()
    }
}

/** A color-swatch + label row for every series in [config], matching `ChartLegendContent`. */
@Composable
fun ShadcnChartLegend(
    config: ShadcnChartConfig,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.md)) {
            config.values.forEach { series ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.size(8.dp).background(series.color, CircleShape))
                    ShadcnText(series.label, style = ShadcnTextStyle.LabelSmall, muted = true)
                }
            }
        }
    }
}

/** A grouped vertical bar chart -- one bar per series, per x-axis category. */
@Composable
fun ShadcnBarChart(
    data: List<ShadcnChartPoint>,
    config: ShadcnChartConfig,
    modifier: Modifier = Modifier,
) {
    val seriesKeys = config.keys.toList()
    val maxValue = data.flatMap { it.values.values }.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    val gridColor = shadcnTheme.colors.border
    var hit by remember(data, seriesKeys) { mutableStateOf<ChartHit?>(null) }

    Box(modifier = modifier) {
        Column {
            Canvas(
                modifier =
                    Modifier.fillMaxWidth().weight(1f)
                        .chartHitTesting(data.isNotEmpty() && seriesKeys.isNotEmpty(), data.size, ::nearestBarIndex) {
                            hit = it
                        },
            ) {
                drawGridlines(gridColor)
                if (data.isEmpty() || seriesKeys.isEmpty()) return@Canvas

                val groupWidth = size.width / data.size
                val barGap = groupWidth * 0.15f
                val barWidth = (groupWidth - barGap * 2) / seriesKeys.size

                data.forEachIndexed { groupIndex, point ->
                    val groupStart = groupIndex * groupWidth + barGap
                    seriesKeys.forEachIndexed { seriesIndex, key ->
                        val value = point.values[key] ?: 0f
                        val barHeight = (value / maxValue) * size.height
                        val left = groupStart + seriesIndex * barWidth
                        drawRect(
                            color = config.getValue(key).color,
                            topLeft = Offset(left, size.height - barHeight),
                            size = Size(barWidth * 0.85f, barHeight),
                        )
                    }
                }
            }
            ChartXAxisLabels(data.map { it.label })
        }
        ChartTooltipOverlay(hit = hit, data = data, config = config)
    }
}

/** A multi-series line chart, one polyline per series key. */
@Composable
fun ShadcnLineChart(
    data: List<ShadcnChartPoint>,
    config: ShadcnChartConfig,
    modifier: Modifier = Modifier,
) {
    val seriesKeys = config.keys.toList()
    val maxValue = data.flatMap { it.values.values }.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    val gridColor = shadcnTheme.colors.border
    var hit by remember(data, seriesKeys) { mutableStateOf<ChartHit?>(null) }

    Box(modifier = modifier) {
        Column {
            Canvas(
                modifier =
                    Modifier.fillMaxWidth().weight(1f)
                        .chartHitTesting(data.size >= 2 && seriesKeys.isNotEmpty(), data.size, ::nearestLineIndex) {
                            hit = it
                        },
            ) {
                drawGridlines(gridColor)
                if (data.size < 2 || seriesKeys.isEmpty()) return@Canvas

                val stepX = size.width / (data.size - 1)
                seriesKeys.forEach { key ->
                    val points =
                        data.mapIndexed { index, point ->
                            val value = point.values[key] ?: 0f
                            Offset(index * stepX, size.height - (value / maxValue) * size.height)
                        }
                    for (i in 0 until points.lastIndex) {
                        drawLine(
                            color = config.getValue(key).color,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                    points.forEach { drawCircle(color = config.getValue(key).color, radius = 3.dp.toPx(), center = it) }
                }
            }
            ChartXAxisLabels(data.map { it.label })
        }
        ChartTooltipOverlay(hit = hit, data = data, config = config)
    }
}

private fun DrawScope.drawGridlines(color: Color) {
    val lineCount = 4
    for (i in 0..lineCount) {
        val y = size.height * i / lineCount
        drawLine(
            color = color.copy(alpha = 0.5f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Butt,
        )
    }
}

/** Nearest data-point index + last known pointer position, matching real `ChartTooltipContent`'s active point. */
private data class ChartHit(val index: Int, val position: Offset)

/** Bar layout puts point `i` in `[i * groupWidth, (i + 1) * groupWidth)` -- same math as the draw loop above. */
private fun nearestBarIndex(
    x: Float,
    width: Float,
    count: Int,
): Int {
    if (count <= 0) return -1
    val groupWidth = width / count
    return (x / groupWidth).toInt().coerceIn(0, count - 1)
}

/** Line layout places point `i` at `i * stepX` -- same math as the draw loop above. */
private fun nearestLineIndex(
    x: Float,
    width: Float,
    count: Int,
): Int {
    if (count <= 0) return -1
    if (count == 1) return 0
    val stepX = width / (count - 1)
    return (x / stepX).roundToInt().coerceIn(0, count - 1)
}

/**
 * Tracks pointer move (desktop hover) and press (touch tap/drag) over the chart's [Canvas],
 * hit-testing the pointer's x-position against [indexForX] to find the nearest data point.
 * Clears on pointer exit/release, matching Recharts' hover-in/hover-out tooltip behavior.
 */
private fun Modifier.chartHitTesting(
    enabled: Boolean,
    pointCount: Int,
    indexForX: (x: Float, width: Float, count: Int) -> Int,
    onHit: (ChartHit?) -> Unit,
): Modifier =
    if (!enabled) {
        this
    } else {
        this.pointerInput(pointCount) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    when (event.type) {
                        PointerEventType.Move, PointerEventType.Press, PointerEventType.Enter -> {
                            val position = event.changes.first().position
                            val index = indexForX(position.x, size.width.toFloat(), pointCount)
                            onHit(if (index >= 0) ChartHit(index, position) else null)
                        }
                        PointerEventType.Exit, PointerEventType.Release -> onHit(null)
                    }
                }
            }
        }
    }

/** A floating card near the pointer, one row per series at the hit point -- matches real `ChartTooltipContent`. */
@Composable
private fun ChartTooltipOverlay(
    hit: ChartHit?,
    data: List<ShadcnChartPoint>,
    config: ShadcnChartConfig,
) {
    val point = hit?.let { data.getOrNull(it.index) } ?: return
    Box(
        modifier =
            Modifier
                .offset { IntOffset(hit.position.x.roundToInt() + 12, hit.position.y.roundToInt() - 12) }
                .background(shadcnTheme.colors.popover, RoundedCornerShape(shadcnTheme.shapes.md))
                .border(1.dp, shadcnTheme.colors.border, RoundedCornerShape(shadcnTheme.shapes.md))
                .padding(shadcnTheme.spacing.sm),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xs)) {
            ShadcnText(point.label, style = ShadcnTextStyle.LabelSmall, color = shadcnTheme.colors.onPopover)
            config.forEach { (key, series) ->
                val value = point.values[key] ?: return@forEach
                Row(
                    horizontalArrangement = Arrangement.spacedBy(shadcnTheme.spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.size(8.dp).background(series.color, CircleShape))
                    ShadcnText(series.label, style = ShadcnTextStyle.LabelSmall, color = shadcnTheme.colors.onPopover)
                    ShadcnText(
                        formatChartValue(value),
                        style = ShadcnTextStyle.LabelSmall,
                        color = shadcnTheme.colors.onPopover,
                    )
                }
            }
        }
    }
}

private fun formatChartValue(value: Float): String =
    if (value == value.toInt().toFloat()) value.toInt().toString() else value.toString()

@Composable
private fun ChartXAxisLabels(labels: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = shadcnTheme.spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        labels.forEach { label ->
            ShadcnText(label, style = ShadcnTextStyle.LabelSmall, muted = true)
        }
    }
}
