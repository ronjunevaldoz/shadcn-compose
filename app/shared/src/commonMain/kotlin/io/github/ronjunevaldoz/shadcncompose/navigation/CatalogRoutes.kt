package io.github.ronjunevaldoz.shadcncompose.navigation

import kotlinx.serialization.Serializable

@Serializable
object CatalogHomeRoute

@Serializable
data class ComponentDetailRoute(val componentId: String)
