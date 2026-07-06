package io.github.ronjunevaldoz.shadcncompose.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

@Composable
fun ComponentDetailScreen(componentId: String) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(shadcnTheme.spacing.lg),
    ) {
        when (componentId) {
            "button" -> ButtonDemo()
            "card" -> CardDemo()
            "badge" -> BadgeDemo()
            "chip" -> ChipDemo()
            "text-field" -> TextFieldDemo()
            "text" -> TextDemo()
        }
    }
}
