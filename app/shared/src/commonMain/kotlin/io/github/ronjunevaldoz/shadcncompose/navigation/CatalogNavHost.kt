package io.github.ronjunevaldoz.shadcncompose.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.ronjunevaldoz.shadcncompose.catalog.CatalogSidebar
import io.github.ronjunevaldoz.shadcncompose.catalog.ComponentDetailScreen
import io.github.ronjunevaldoz.shadcncompose.theme.shadcnTheme

/**
 * Persistent sidebar + content-pane shell -- the sidebar never leaves the screen,
 * only the content pane swaps via [NavHost]. Matches the shadcn/ui docs-site layout
 * rather than a phone-app push/pop list.
 */
@Composable
fun CatalogNavHost(navController: NavHostController = rememberNavController()) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val selectedId =
        currentBackStackEntry
            ?.let { runCatching { it.toRoute<ComponentDetailRoute>() }.getOrNull() }
            ?.componentId

    Row(modifier = Modifier.fillMaxSize()) {
        CatalogSidebar(
            selectedId = selectedId ?: "introduction",
            onEntryClick = { componentId ->
                navController.navigate(ComponentDetailRoute(componentId)) {
                    popUpTo<ComponentDetailRoute> { inclusive = true }
                    launchSingleTop = true
                }
            },
        )
        Box(
            modifier =
                Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(shadcnTheme.colors.border),
        )
        Box(modifier = Modifier.weight(1f)) {
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
