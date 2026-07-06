package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.runtime.Composable

/** A static "Getting Started" documentation page, as opposed to a component demo. */
data class GuidePage(
    val id: String,
    val title: String,
    val content: @Composable () -> Unit,
)
