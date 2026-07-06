package io.github.ronjunevaldoz.shadcncompose.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnText
import io.github.ronjunevaldoz.shadcncompose.components.ShadcnTextStyle

/** Shown in the content pane before a sidebar entry has been selected. */
@Composable
fun CatalogWelcomeScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ShadcnText(
            text = "Select a component from the sidebar",
            style = ShadcnTextStyle.BodyLarge,
            muted = true,
        )
    }
}
