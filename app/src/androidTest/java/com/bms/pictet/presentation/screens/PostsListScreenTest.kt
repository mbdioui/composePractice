package com.bms.pictet.presentation.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.bms.pictet.domain.model.Post
import com.bms.pictet.presentation.theme.PictetTheme
import org.junit.Rule
import org.junit.Test

/**
 * PostsListScreenTest: UI tests for PostsListScreen.
 *
 * UI Testing Strategy:
 * - Test composable rendering
 * - Test user interactions
 * - Test state changes
 * - Use createComposeRule for Compose tests
 */
class PostsListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Test: Screen should display posts when loaded.
     */
    @Test
    fun screen_withPosts_shouldDisplayPostTitles() {
        // Given
        val posts = listOf(
            Post(id = 1, userId = 1, title = "Test Post", body = "Test body")
        )

        // When
        composeTestRule.setContent {
            PictetTheme {
                PostsListContent(
                    uiState = PostsUiState(posts = posts),
                    onPostClick = {},
                    onRefresh = {},
                    onRetry = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Test Post").assertIsDisplayed()
    }

    /**
     * Test: Screen should display loading indicator.
     */
    @Test
    fun screen_whenLoading_shouldShowLoading() {
        // When
        composeTestRule.setContent {
            PictetTheme {
                PostsListContent(
                    uiState = PostsUiState(isLoading = true),
                    onPostClick = {},
                    onRefresh = {},
                    onRetry = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Loading posts...").assertIsDisplayed()
    }

    /**
     * Test: Screen should display error message.
     */
    @Test
    fun screen_withError_shouldShowError() {
        // Given
        val errorMessage = "Network error"

        // When
        composeTestRule.setContent {
            PictetTheme {
                PostsListContent(
                    uiState = PostsUiState(error = errorMessage),
                    onPostClick = {},
                    onRefresh = {},
                    onRetry = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Failed to load posts").assertIsDisplayed()
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    /**
     * Test: Clicking on post should trigger navigation callback.
     */
    @Test
    fun screen_whenPostClicked_shouldTriggerCallback() {
        // Given
        var clickedPostId: Int? = null
        val posts = listOf(
            Post(id = 1, userId = 1, title = "Clickable Post", body = "Body")
        )

        composeTestRule.setContent {
            PictetTheme {
                PostsListContent(
                    uiState = PostsUiState(posts = posts),
                    onPostClick = { clickedPostId = it },
                    onRefresh = {},
                    onRetry = {}
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Clickable Post").performClick()

        // Then
        assert(clickedPostId == 1)
    }

    /**
     * Test: Error banner should be displayed when error exists but has posts.
     */
    @Test
    fun screen_withErrorAndPosts_shouldShowErrorBanner() {
        // Given
        val posts = listOf(
            Post(id = 1, userId = 1, title = "Cached Post", body = "Body")
        )

        // When
        composeTestRule.setContent {
            PictetTheme {
                PostsListContent(
                    uiState = PostsUiState(
                        posts = posts,
                        error = "Sync failed"
                    ),
                    onPostClick = {},
                    onRefresh = {},
                    onRetry = {}
                )
            }
        }

        // Then - Should show both posts and error banner
        composeTestRule.onNodeWithText("Cached Post").assertIsDisplayed()
        composeTestRule.onNodeWithText("⚠️ Sync failed").assertIsDisplayed()
    }
}
