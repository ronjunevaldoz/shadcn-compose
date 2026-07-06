package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.runtime.Composable

data class ComponentExample(
    val title: String,
    val code: String,
    val preview: @Composable () -> Unit,
)

data class ComponentDoc(
    val id: String,
    val title: String,
    val description: String,
    val usageCode: String,
    val examples: List<ComponentExample>,
)
