package com.bms.pictet.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bms.pictet.presentation.screens.PostDetailScreen
import com.bms.pictet.presentation.screens.PostsListScreen

/**
 * NavGraph: Defines the navigation structure of the app.
 *
 * Navigation Component Pattern:
 * - NavHost: Container for navigation destinations
 * - NavController: Manages navigation state and back stack
 * - composable: Defines a screen destination
 * - Arguments: Passed between screens via route
 *
 * Benefits:
 * - Type-safe navigation with arguments
 * - Automatic back stack management
 * - Deep linking support
 * - Animation between screens
 *
 * @param navController Controller for navigation actions
 * @param modifier Modifier for the NavHost
 * @param startDestination Initial screen to display
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = NavigationRoutes.POSTS_LIST
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        /**
         * Posts List Screen
         * Route: posts_list
         * No arguments required
         */
        composable(
            route = NavigationRoutes.POSTS_LIST
        ) {
            PostsListScreen(
                onPostClick = { postId ->
                    // Navigate to detail screen with post ID
                    navController.navigate(
                        NavigationRoutes.postDetailRoute(postId)
                    )
                }
            )
        }

        /**
         * Post Detail Screen
         * Route: post_detail/{postId}
         * Required argument: postId (Int)
         *
         * navArgument: Defines the argument type for type safety
         * NavType.IntType: Ensures argument is parsed as Integer
         */
        composable(
            route = NavigationRoutes.POST_DETAIL,
            arguments = listOf(
                navArgument(NavigationArguments.POST_ID) {
                    type = NavType.IntType
                    // Argument is required (no default value)
                }
            )
        ) { backStackEntry ->
            // Extract argument from route
            val postId = backStackEntry.arguments?.getInt(NavigationArguments.POST_ID)
                ?: throw IllegalArgumentException("postId is required")

            PostDetailScreen(
                postId = postId,
                onNavigateBack = {
                    // Go back to previous screen
                    navController.popBackStack()
                }
            )
        }
    }
}
