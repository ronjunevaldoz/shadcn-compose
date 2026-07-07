package io.github.ronjunevaldoz.shadcncompose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.ronjunevaldoz.shadcncompose.theme.LocalShadcnDataSlots
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme

@Composable
fun ShadcnSlotIcon(imageVector: ImageVector, contentDescription: String?) {
    // If inside a data-slot component (like a Chip), pull slot constraints. Otherwise, default.
    val slot = LocalShadcnDataSlots.current
    val fallbackSize = ShadcnTheme.current.icons.standardSize

    Image(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = Modifier.size(slot?.iconSize ?: fallbackSize)
    )
}