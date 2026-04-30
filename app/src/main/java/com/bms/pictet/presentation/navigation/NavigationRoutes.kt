package com.bms.pictet.presentation.navigation

/**
 * NavigationRoutes: Definition of all navigation routes in the app.
 *
 * Navigation Pattern:
 * - Centralized route definitions as constants
 * - Type-safe navigation using sealed classes or const val
 * - Routes are used in NavHost and navigation calls
 *
 * Routes:
 * - posts_list: List of all posts
 * - post_detail: Detail view for a specific post (requires postId argument)
 */
object NavigationRoutes {
    /**
     * Route for posts list screen.
     * No arguments required.
     */
    const val POSTS_LIST = "posts_list"

    /**
     * Route pattern for post detail screen.
     * Requires postId as argument.
     * Example: post_detail/1
     */
    const val POST_DETAIL = "post_detail/{postId}"

    /**
     * Creates the actual route with argument.
     *
     * @param postId The post ID to navigate to
     * @return Complete route string
     */
    fun postDetailRoute(postId: Int): String {
        return "post_detail/$postId"
    }
}

/**
 * NavigationArguments: Constants for argument names.
 * Ensures consistency between route definition and argument extraction.
 */
object NavigationArguments {
    const val POST_ID = "postId"
}
