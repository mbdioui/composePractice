package com.bms.pictet.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bms.pictet.presentation.screens.LaunchDetailScreen
import com.bms.pictet.presentation.screens.LaunchScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.LAUNCH_LIST
    ) {
        composable(route = NavRoutes.LAUNCH_LIST) {
            LaunchScreen(
                onLaunchClick = { launchId ->
                    navController.navigate(NavRoutes.launchDetailRoute(launchId))
                }
            )
        }

        composable(
            route = NavRoutes.LAUNCH_DETAIL_ROUTE,
            arguments = listOf(
                navArgument(NavRoutes.LAUNCH_ID_ARG) { type = NavType.StringType }
            )
        ) {
            LaunchDetailScreen()
        }
    }
}
