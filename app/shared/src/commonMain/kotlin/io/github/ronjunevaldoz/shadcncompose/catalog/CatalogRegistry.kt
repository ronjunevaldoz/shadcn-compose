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
    DATA_DISPLAY("Data Display"),
    FEEDBACK("Feedback"),
    DISCLOSURE("Disclosure & Navigation"),
    OVERLAYS("Overlays & Navigation"),
    DATA_AND_LAYOUT("Data & Layout"),
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
        // Data Display
        CatalogEntry(id = "avatar", title = "Avatar", category = CatalogCategory.DATA_DISPLAY),
        CatalogEntry(id = "aspect-ratio", title = "Aspect Ratio", category = CatalogCategory.DATA_DISPLAY),
        CatalogEntry(id = "separator", title = "Separator", category = CatalogCategory.DATA_DISPLAY),
        CatalogEntry(id = "kbd", title = "Kbd", category = CatalogCategory.DATA_DISPLAY),
        // Feedback
        CatalogEntry(id = "alert", title = "Alert", category = CatalogCategory.FEEDBACK),
        CatalogEntry(id = "progress", title = "Progress", category = CatalogCategory.FEEDBACK),
        CatalogEntry(id = "skeleton", title = "Skeleton", category = CatalogCategory.FEEDBACK),
        CatalogEntry(id = "spinner", title = "Spinner", category = CatalogCategory.FEEDBACK),
        // Disclosure & Navigation
        CatalogEntry(id = "collapsible", title = "Collapsible", category = CatalogCategory.DISCLOSURE),
        CatalogEntry(id = "accordion", title = "Accordion", category = CatalogCategory.DISCLOSURE),
        CatalogEntry(id = "tabs", title = "Tabs", category = CatalogCategory.DISCLOSURE),
        // Overlays & Navigation
        CatalogEntry(id = "tooltip", title = "Tooltip", category = CatalogCategory.OVERLAYS),
        CatalogEntry(id = "popover", title = "Popover", category = CatalogCategory.OVERLAYS),
        CatalogEntry(id = "hover-card", title = "Hover Card", category = CatalogCategory.OVERLAYS),
        CatalogEntry(id = "dropdown-menu", title = "Dropdown Menu", category = CatalogCategory.OVERLAYS),
        CatalogEntry(id = "context-menu", title = "Context Menu", category = CatalogCategory.OVERLAYS),
        CatalogEntry(id = "dialog", title = "Dialog", category = CatalogCategory.OVERLAYS),
        CatalogEntry(id = "alert-dialog", title = "Alert Dialog", category = CatalogCategory.OVERLAYS),
        CatalogEntry(id = "sheet", title = "Sheet", category = CatalogCategory.OVERLAYS),
        CatalogEntry(id = "combobox", title = "Combobox", category = CatalogCategory.OVERLAYS),
        CatalogEntry(id = "command", title = "Command", category = CatalogCategory.OVERLAYS),
        CatalogEntry(id = "menubar", title = "Menubar", category = CatalogCategory.OVERLAYS),
        CatalogEntry(id = "navigation-menu", title = "Navigation Menu", category = CatalogCategory.OVERLAYS),
        // Data & Layout
        CatalogEntry(id = "table", title = "Table", category = CatalogCategory.DATA_AND_LAYOUT),
        CatalogEntry(id = "pagination", title = "Pagination", category = CatalogCategory.DATA_AND_LAYOUT),
        CatalogEntry(id = "scroll-area", title = "Scroll Area", category = CatalogCategory.DATA_AND_LAYOUT),
        CatalogEntry(id = "chart", title = "Chart", category = CatalogCategory.DATA_AND_LAYOUT),
        CatalogEntry(id = "calendar", title = "Calendar", category = CatalogCategory.DATA_AND_LAYOUT),
        CatalogEntry(id = "carousel", title = "Carousel", category = CatalogCategory.DATA_AND_LAYOUT),
        CatalogEntry(id = "resizable", title = "Resizable", category = CatalogCategory.DATA_AND_LAYOUT),
        CatalogEntry(id = "sidebar", title = "Sidebar", category = CatalogCategory.DATA_AND_LAYOUT),
    )

val catalogEntriesByCategory: Map<CatalogCategory, List<CatalogEntry>> =
    catalogEntries.groupBy { it.category }
