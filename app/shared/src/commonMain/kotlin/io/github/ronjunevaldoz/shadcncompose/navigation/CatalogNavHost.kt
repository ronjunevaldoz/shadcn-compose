package io.github.ronjunevaldoz.shadcncompose.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.ronjunevaldoz.shadcncompose.catalog.CatalogSidebar
import io.github.ronjunevaldoz.shadcncompose.catalog.CatalogTopBar
import io.github.ronjunevaldoz.shadcncompose.catalog.ComponentDetailScreen
import io.github.ronjunevaldoz.shadcncompose.catalog.CreatePage
import io.github.ronjunevaldoz.shadcncompose.theme.ShadcnTheme
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnAccent
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnBaseColor
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnRadius
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnStylePreset

/**
 * Below this width, there isn't enough room for a persistent 240dp sidebar plus a
 * usable content pane -- single-word labels (chip/button text) start wrapping
 * letter-by-letter instead of the layout adapting. Matches a typical phone-portrait
 * width plus a little headroom.
 */
private val COMPACT_BREAKPOINT = 720.dp

// Matches App.kt's own outer ShadcnTheme's baseRadius -- the nested content-pane
// ShadcnTheme below needs the same value so the content pane's shapes stay visually
// consistent with the chrome around it.
private val CATALOG_BASE_RADIUS = ShadcnRadius(4.dp)

/**
 * Sidebar + content-pane shell -- only the content pane swaps via [NavHost]. Matches
 * the shadcn/ui docs-site layout rather than a phone-app push/pop list. Above
 * [COMPACT_BREAKPOINT] the sidebar stays persistently on screen; below it, the
 * sidebar becomes a dismissible drawer opened from a menu button in the top bar,
 * so narrow/phone-width screens get a full-width content pane instead of a
 * squeezed-down sidebar-plus-content split.
 *
 * Two theming axes, two different scopes -- see [CatalogContentTheme], nested around
 * everything below the top bar:
 * - [isDarkMode]/[onToggleDarkMode] apply everywhere, top bar included -- both this
 *   nested theme and the *outer* ShadcnTheme in
 *   [io.github.ronjunevaldoz.shadcncompose.App] read the same shared `isDarkMode` value,
 *   since dark/light is a real accessibility preference, not a preview-only setting.
 * - [stylePreset]/[baseColor]/[accent] (the picker values) apply to the sidebar and
 *   content pane only -- the outer theme wrapping the top bar uses fixed constants
 *   instead, so the top bar keeps a stable brand identity regardless of what a reader
 *   is previewing.
 */
@Composable
fun CatalogNavHost(
    stylePreset: ShadcnStylePreset,
    onStylePresetChange: (ShadcnStylePreset) -> Unit,
    baseColor: ShadcnBaseColor,
    onBaseColorChange: (ShadcnBaseColor) -> Unit,
    accent: ShadcnAccent,
    onAccentChange: (ShadcnAccent) -> Unit,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedId =
        currentBackStackEntry
            ?.let { runCatching { it.toRoute<ComponentDetailRoute>() }.getOrNull() }
            ?.componentId
    var drawerOpen by remember { mutableStateOf(false) }
    // Hoisted here, not a local `remember` inside CatalogSidebar, since this composable
    // instantiates two separate CatalogSidebar instances below (persistent desktop
    // sidebar + mobile drawer overlay) that need to share one query rather than each
    // starting fresh -- a resize crossing COMPACT_BREAKPOINT would otherwise silently
    // drop an in-progress search.
    var searchQuery by remember { mutableStateOf("") }
    // Explicit UI state, not derived via toRoute<CreateRoute>() off the back stack --
    // CreateRoute is a zero-property `data object`, so a mismatched entry's argument
    // bundle (e.g. ComponentDetailRoute's componentId) has nothing for that decode to
    // fail on, and toRoute<CreateRoute>() spuriously succeeds for *any* current
    // destination. Tracked the same way as drawerOpen/searchQuery instead.
    var isOnCreateRoute by remember { mutableStateOf(false) }

    fun navigateTo(componentId: String) {
        navController.navigate(ComponentDetailRoute(componentId)) {
            popUpTo<ComponentDetailRoute> { inclusive = true }
            launchSingleTop = true
        }
        drawerOpen = false
        searchQuery = ""
        isOnCreateRoute = false
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxWidth < COMPACT_BREAKPOINT

        Column(modifier = Modifier.fillMaxSize()) {
            CatalogTopBar(
                stylePreset = stylePreset,
                onStylePresetChange = onStylePresetChange,
                baseColor = baseColor,
                onBaseColorChange = onBaseColorChange,
                accent = accent,
                onAccentChange = onAccentChange,
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode,
                onMenuClick = if (isCompact && !isOnCreateRoute) ({ drawerOpen = true }) else null,
                isOnCreateRoute = isOnCreateRoute,
                onNavigateToComponents = { navigateTo(selectedId ?: "introduction") },
                onNavigateToCreate = {
                    navController.navigate(CreateRoute)
                    isOnCreateRoute = true
                },
            )

            // Nested ShadcnTheme, scoped to everything below the top bar -- isDarkMode
            // (the manual toggle) applies to the sidebar and content pane, but not the
            // top bar above (which stays on the outer, system-only theme in App.kt).
            // Same preset/baseColor/accent/baseRadius as the outer theme so shapes and
            // color axes stay consistent; only isDark differs.
            CatalogContentTheme(stylePreset, baseColor, accent, isDarkMode) {
                Row(modifier = Modifier.weight(1f)) {
                    if (!isCompact && !isOnCreateRoute) {
                        CatalogSidebar(
                            selectedId = selectedId ?: "introduction",
                            onEntryClick = ::navigateTo,
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                        )
                        Box(
                            modifier =
                                Modifier
                                    .width(1.dp)
                                    .fillMaxHeight()
                                    .background(shadcnTheme.colors.border),
                        )
                    }
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .background(shadcnTheme.colors.background),
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = ComponentDetailRoute("introduction"),
                        ) {
                            composable<ComponentDetailRoute> { backStackEntry ->
                                val route: ComponentDetailRoute = backStackEntry.toRoute()
                                ComponentDetailScreen(componentId = route.componentId)
                            }
                            composable<CreateRoute> {
                                CreatePage(
                                    stylePreset = stylePreset,
                                    onStylePresetChange = onStylePresetChange,
                                    baseColor = baseColor,
                                    onBaseColorChange = onBaseColorChange,
                                    accent = accent,
                                    onAccentChange = onAccentChange,
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isCompact && drawerOpen && !isOnCreateRoute) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable {
                            drawerOpen = false
                            searchQuery = ""
                        },
            )
            CatalogContentTheme(stylePreset, baseColor, accent, isDarkMode) {
                CatalogSidebar(
                    selectedId = selectedId ?: "introduction",
                    onEntryClick = ::navigateTo,
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.fillMaxHeight(),
                )
            }
        }
    }
}

/**
 * The dark-mode-toggle-aware theme scope shared by the sidebar (both the persistent
 * desktop instance and the mobile drawer instance) and the content pane -- factored out
 * so both [CatalogNavHost] call sites stay in sync rather than risking the two nested
 * `ShadcnTheme` calls drifting out of sync with each other over time.
 */
@Composable
private fun CatalogContentTheme(
    stylePreset: ShadcnStylePreset,
    baseColor: ShadcnBaseColor,
    accent: ShadcnAccent,
    isDarkMode: Boolean,
    content: @Composable () -> Unit,
) {
    ShadcnTheme(
        preset = stylePreset,
        baseColor = baseColor,
        accent = accent,
        baseRadius = CATALOG_BASE_RADIUS,
        isDark = isDarkMode,
        content = content,
    )
}
