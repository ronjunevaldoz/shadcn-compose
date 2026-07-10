package io.github.ronjunevaldoz.shadcncompose.navigation

import kotlinx.serialization.Serializable

@Serializable
data class ComponentDetailRoute(val componentId: String)

/** The theme-configurator page -- see [io.github.ronjunevaldoz.shadcncompose.catalog.CreatePage]. */
@Serializable
data object CreateRoute
