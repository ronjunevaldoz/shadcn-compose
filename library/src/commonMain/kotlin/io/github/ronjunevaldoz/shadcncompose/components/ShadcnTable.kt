package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * A simple data table shell, matching real shadcn/ui's `table.tsx` structure --
 * header row separated by a bottom border, body rows separated by hairlines, cells
 * left-aligned with consistent padding. Rows are plain [Row]s (`cells` receives a
 * [RowScope]) so callers can freely use `Modifier.weight` per column.
 *
 * Usage:
 * ```
 * ShadcnTable {
 *     ShadcnTableHeaderRow { ShadcnTableHeadCell("Name", Modifier.weight(1f)); ShadcnTableHeadCell("Status") }
 *     ShadcnTableRow { ShadcnTableCell("Invoice #1", Modifier.weight(1f)); ShadcnTableCell("Paid") }
 * }
 * ```
 */
@Composable
fun ShadcnTable(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth(), content = content)
}

@Composable
fun ColumnScope.ShadcnTableHeaderRow(
    modifier: Modifier = Modifier,
    cells: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(shadcnTheme.colors.background)
                .padding(vertical = shadcnTheme.spacing.sm),
        content = cells,
    )
    ShadcnSeparator()
}

@Composable
fun ColumnScope.ShadcnTableRow(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    isLast: Boolean = false,
    cells: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(if (selected) shadcnTheme.colors.muted else shadcnTheme.colors.background)
                .padding(vertical = shadcnTheme.spacing.sm),
        content = cells,
    )
    if (!isLast) ShadcnSeparator()
}

@Composable
fun RowScope.ShadcnTableHeadCell(
    text: String,
    modifier: Modifier = Modifier,
) {
    ShadcnText(
        text,
        style = ShadcnTextStyle.LabelLarge,
        modifier = modifier.padding(horizontal = shadcnTheme.spacing.sm).padding(end = 4.dp),
    )
}

@Composable
fun RowScope.ShadcnTableCell(
    text: String,
    modifier: Modifier = Modifier,
    muted: Boolean = false,
) {
    ShadcnText(
        text,
        style = ShadcnTextStyle.BodyMedium,
        muted = muted,
        modifier = modifier.padding(horizontal = shadcnTheme.spacing.sm).padding(end = 4.dp),
    )
}
