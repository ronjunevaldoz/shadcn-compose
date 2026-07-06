package io.github.ronjunevaldoz.shadcncompose.catalog

/**
 * Static registry of every component the catalog app can show. Add an entry here
 * whenever a new component ships in `:library` -- this is what drives the home
 * screen's grouping and the detail route dispatch, so new categories (Forms & Inputs,
 * Overlays & Navigation, Data Display & Feedback, ...) slot in without touching
 * navigation plumbing.
 */
enum class CatalogCategory(val title: String) {
    CORE_PRIMITIVES("Core Primitives"),
}

data class CatalogEntry(
    val id: String,
    val title: String,
    val category: CatalogCategory,
)

val catalogEntries: List<CatalogEntry> =
    listOf(
        CatalogEntry(id = "button", title = "Button", category = CatalogCategory.CORE_PRIMITIVES),
        CatalogEntry(id = "card", title = "Card", category = CatalogCategory.CORE_PRIMITIVES),
        CatalogEntry(id = "badge", title = "Badge", category = CatalogCategory.CORE_PRIMITIVES),
        CatalogEntry(id = "chip", title = "Chip", category = CatalogCategory.CORE_PRIMITIVES),
        CatalogEntry(id = "text-field", title = "Text Field", category = CatalogCategory.CORE_PRIMITIVES),
        CatalogEntry(id = "text", title = "Text", category = CatalogCategory.CORE_PRIMITIVES),
    )

val catalogEntriesByCategory: Map<CatalogCategory, List<CatalogEntry>> =
    catalogEntries.groupBy { it.category }
