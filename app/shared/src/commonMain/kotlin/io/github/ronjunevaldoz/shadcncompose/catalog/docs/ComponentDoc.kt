package io.github.ronjunevaldoz.shadcncompose.catalog.docs

import androidx.compose.runtime.Composable

data class ComponentExample(
    val title: String,
    val code: String,
    val preview: @Composable () -> Unit,
)

/**
 * @param referenceUrl The real shadcn/ui docs page this component is modeled on
 * (`https://ui.shadcn.com/docs/components/base/<slug>`), verified live -- not guessed.
 * `null` only for components with no real shadcn/ui equivalent (this library's own
 * additions, e.g. [io.github.ronjunevaldoz.shadcncompose.components.ShadcnChip]/
 * [io.github.ronjunevaldoz.shadcncompose.components.ShadcnStepper], or utility
 * modifiers with no dedicated real component page).
 */
data class ComponentDoc(
    val id: String,
    val referenceUrl: String? = null,
    val title: String,
    val description: String,
    val usageCode: String,
    val examples: List<ComponentExample>,
)
