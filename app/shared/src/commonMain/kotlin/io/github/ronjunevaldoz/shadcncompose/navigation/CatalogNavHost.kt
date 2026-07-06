package io.github.ronjunevaldoz.shadcncompose.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.ronjunevaldoz.shadcncompose.catalog.CatalogHomeScreen
import io.github.ronjunevaldoz.shadcncompose.catalog.ComponentDetailScreen

@Composable
fun CatalogNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = CatalogHomeRoute,
    ) {
        composable<CatalogHomeRoute> {
            CatalogHomeScreen(
                onComponentClick = { componentId ->
                    navController.navigate(ComponentDetailRoute(componentId))
                },
            )
        }
        composable<ComponentDetailRoute> { backStackEntry ->
            val route: ComponentDetailRoute = backStackEntry.toRoute()
            ComponentDetailScreen(componentId = route.componentId)
        }
    }
}
