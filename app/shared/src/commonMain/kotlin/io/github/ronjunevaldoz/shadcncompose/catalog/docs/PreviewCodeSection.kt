@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnChip
import io.github.ronjunevaldoz.shadcncompose.styles.ChipVariant
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * A "Preview | Code" toggle used for every live example in the catalog, mirroring
 * the shadcn/ui docs pattern. There's no Tabs component in the design system yet
 * (that's a later Overlays & Navigation milestone), so this uses a small chip
 * pair locally rather than blocking on that component.
 */
@Composable
fun PreviewCodeSection(
    code: String,
    modifier: Modifier = Modifier,
    preview: @Composable () -> Unit,
) {
    var showCode by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShadcnChip(
                label = "Preview",
                selected = !showCode,
                onClick = { showCode = false },
                variant = if (!showCode) ChipVariant.Selected else ChipVariant.Outline,
            )
            ShadcnChip(
                label = "Code",
                selected = showCode,
                onClick = { showCode = true },
                variant = if (showCode) ChipVariant.Selected else ChipVariant.Outline,
            )
        }
        Spacer(Modifier.height(shadcnTheme.spacing.sm))
        if (showCode) {
            CodeBlock(code = code, modifier = Modifier.fillMaxWidth())
        } else {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .border(1.dp, shadcnTheme.colors.border, RoundedCornerShape(shadcnTheme.shapes.lg))
                        .background(shadcnTheme.colors.background, RoundedCornerShape(shadcnTheme.shapes.lg))
                        .padding(shadcnTheme.spacing.xxl),
                // Not Alignment.Center: centering horizontally re-centers the whole preview
                // block whenever its intrinsic width changes (e.g. a Collapsible expanding to
                // reveal wider text, or switching Tabs to a longer content string) -- since
                // that width change is driven by content BELOW/AROUND a Start-anchored trigger,
                // the trigger visibly shifts sideways as the block recenters even though its
                // own position within its own layout never changed. TopStart keeps every demo
                // anchored to a fixed point regardless of how its content resizes.
                contentAlignment = Alignment.TopStart,
            ) {
                preview()
            }
        }
    }
}
