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
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnAccent
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnBaseColor
import io.github.ronjunevaldoz.shadcncompose.tokens.ShadcnStylePreset

/**
 * Below this width, there isn't enough room for a persistent 240dp sidebar plus a
 * usable content pane -- single-word labels (chip/button text) start wrapping
 * letter-by-letter instead of the layout adapting. Matches a typical phone-portrait
 * width plus a little headroom.
 */
private val COMPACT_BREAKPOINT = 720.dp

/**
 * Sidebar + content-pane shell -- only the content pane swaps via [NavHost]. Matches
 * the shadcn/ui docs-site layout rather than a phone-app push/pop list. Above
 * [COMPACT_BREAKPOINT] the sidebar stays persistently on screen; below it, the
 * sidebar becomes a dismissible drawer opened from a menu button in the top bar,
 * so narrow/phone-width screens get a full-width content pane instead of a
 * squeezed-down sidebar-plus-content split.
 */
@Composable
fun CatalogNavHost(
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    stylePreset: ShadcnStylePreset,
    onStylePresetChange: (ShadcnStylePreset) -> Unit,
    baseColor: ShadcnBaseColor,
    onBaseColorChange: (ShadcnBaseColor) -> Unit,
    accent: ShadcnAccent,
    onAccentChange: (ShadcnAccent) -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedId =
        currentBackStackEntry
            ?.let { runCatching { it.toRoute<ComponentDetailRoute>() }.getOrNull() }
            ?.componentId
    var drawerOpen by remember { mutableStateOf(false) }

    fun navigateTo(componentId: String) {
        navController.navigate(ComponentDetailRoute(componentId)) {
            popUpTo<ComponentDetailRoute> { inclusive = true }
            launchSingleTop = true
        }
        drawerOpen = false
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isCompact = maxWidth < COMPACT_BREAKPOINT

        Column(modifier = Modifier.fillMaxSize()) {
            CatalogTopBar(
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode,
                stylePreset = stylePreset,
                onStylePresetChange = onStylePresetChange,
                baseColor = baseColor,
                onBaseColorChange = onBaseColorChange,
                accent = accent,
                onAccentChange = onAccentChange,
                onMenuClick = if (isCompact) ({ drawerOpen = true }) else null,
            )

            Row(modifier = Modifier.weight(1f)) {
                if (!isCompact) {
                    CatalogSidebar(
                        selectedId = selectedId ?: "introduction",
                        onEntryClick = ::navigateTo,
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
                    }
                }
            }
        }

        if (isCompact && drawerOpen) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { drawerOpen = false },
            )
            CatalogSidebar(
                selectedId = selectedId ?: "introduction",
                onEntryClick = ::navigateTo,
                modifier = Modifier.fillMaxHeight(),
            )
        }
    }
}
