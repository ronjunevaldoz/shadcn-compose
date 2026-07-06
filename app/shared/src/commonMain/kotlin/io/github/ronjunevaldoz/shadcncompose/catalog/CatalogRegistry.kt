package io.github.ronjunevaldoz.shadcncompose.catalog

/**
 * Static registry of every entry the catalog sidebar can show -- both docs pages
 * (Getting Started) and component demos. Add an entry here whenever a new page or
 * component ships -- this is what drives the sidebar's grouping and the detail
 * route dispatch, so new categories (Forms & Inputs, Overlays & Navigation, Data
 * Display & Feedback, ...) slot in without touching navigation plumbing.
 */
enum class CatalogCategory(val title: String) {
    GETTING_STARTED("Getting Started"),
    CORE_PRIMITIVES("Core Primitives"),
    FORMS_AND_INPUTS("Forms & Inputs"),
}

data class CatalogEntry(
    val id: String,
    val title: String,
    val category: CatalogCategory,
)

val catalogEntries: List<CatalogEntry> =
    listOf(
        // Getting Started -- listed first so groupBy keeps this section on top.
        CatalogEntry(id = "introduction", title = "Introduction", category = CatalogCategory.GETTING_STARTED),
        CatalogEntry(id = "installation", title = "Installation", category = CatalogCategory.GETTING_STARTED),
        CatalogEntry(id = "theming", title = "Theming", category = CatalogCategory.GETTING_STARTED),
        CatalogEntry(id = "dark-mode", title = "Dark Mode", category = CatalogCategory.GETTING_STARTED),
        CatalogEntry(id = "typography", title = "Typography", category = CatalogCategory.GETTING_STARTED),
        // Core Primitives
        CatalogEntry(id = "button", title = "Button", category = CatalogCategory.CORE_PRIMITIVES),
        CatalogEntry(id = "card", title = "Card", category = CatalogCategory.CORE_PRIMITIVES),
        CatalogEntry(id = "badge", title = "Badge", category = CatalogCategory.CORE_PRIMITIVES),
        CatalogEntry(id = "chip", title = "Chip", category = CatalogCategory.CORE_PRIMITIVES),
        CatalogEntry(id = "text-field", title = "Text Field", category = CatalogCategory.CORE_PRIMITIVES),
        CatalogEntry(id = "text", title = "Text", category = CatalogCategory.CORE_PRIMITIVES),
        // Forms & Inputs
        CatalogEntry(id = "label", title = "Label", category = CatalogCategory.FORMS_AND_INPUTS),
        CatalogEntry(id = "checkbox", title = "Checkbox", category = CatalogCategory.FORMS_AND_INPUTS),
        CatalogEntry(id = "radio-group", title = "Radio Group", category = CatalogCategory.FORMS_AND_INPUTS),
        CatalogEntry(id = "switch", title = "Switch", category = CatalogCategory.FORMS_AND_INPUTS),
        CatalogEntry(id = "toggle", title = "Toggle", category = CatalogCategory.FORMS_AND_INPUTS),
        CatalogEntry(id = "slider", title = "Slider", category = CatalogCategory.FORMS_AND_INPUTS),
        CatalogEntry(id = "toggle-group", title = "Toggle Group", category = CatalogCategory.FORMS_AND_INPUTS),
        CatalogEntry(id = "input-group", title = "Input Group", category = CatalogCategory.FORMS_AND_INPUTS),
        CatalogEntry(id = "button-group", title = "Button Group", category = CatalogCategory.FORMS_AND_INPUTS),
        CatalogEntry(id = "textarea", title = "Textarea", category = CatalogCategory.FORMS_AND_INPUTS),
    )

val catalogEntriesByCategory: Map<CatalogCategory, List<CatalogEntry>> =
    catalogEntries.groupBy { it.category }
