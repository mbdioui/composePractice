package com.bms.pictet.presentation.viewmodels

import app.cash.turbine.test
import com.bms.pictet.domain.model.Post
import com.bms.pictet.domain.usecase.GetPostsUseCase
import com.bms.pictet.domain.usecase.RefreshPostsUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * PostsViewModelTest: Unit tests for PostsViewModel.
 *
 * Testing Strategy:
 * - Test each public method independently
 * - Mock dependencies (UseCases) to isolate ViewModel
 * - Test state transitions (loading → success/error)
 * - Test Flow emissions using Turbine
 *
 * Patterns:
 * - @MockK for dependency mocking
 * - runTest for coroutine testing
 * - Turbine for Flow testing
 * - StandardTestDispatcher for controlled execution
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PostsViewModelTest {

    // Test dispatcher for controlling coroutine execution
    private val testDispatcher = StandardTestDispatcher()

    // Mocked dependencies
    @MockK
    private lateinit var getPostsUseCase: GetPostsUseCase

    @MockK
    private lateinit var refreshPostsUseCase: RefreshPostsUseCase

    // System under test
    private lateinit var viewModel: PostsViewModel

    // Sample data for tests
    private val samplePosts = listOf(
        Post(id = 1, userId = 1, title = "Post 1", body = "Body 1"),
        Post(id = 2, userId = 1, title = "Post 2", body = "Body 2")
    )

    /**
     * Setup before each test.
     * - Initialize MockK annotations
     * - Set main dispatcher for coroutine testing
     * - Create ViewModel with mocked dependencies
     */
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
    }

    /**
     * Cleanup after each test.
     * - Reset main dispatcher
     */
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Test: When ViewModel is created, it should load posts successfully.
     *
     * Scenario:
     * 1. getPostsUseCase returns flow with success result
     * 2. refreshPostsUseCase returns success
     * 3. ViewModel state should update with posts
     */
    @Test
    fun `when initialized, should load posts successfully`() = runTest {
        // Given
        every { getPostsUseCase() } returns flowOf(Result.success(samplePosts))
        coEvery { refreshPostsUseCase() } returns Result.success(Unit)

        // When
        viewModel = PostsViewModel(getPostsUseCase, refreshPostsUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(samplePosts, state.posts)
        assertNull(state.error)
        assertTrue(state.hasPosts)
    }

    /**
     * Test: When refresh fails, error should be displayed.
     *
     * Scenario:
     * 1. getPostsUseCase returns empty cache (failure)
     * 2. User triggers refresh that fails
     * 3. ViewModel should show error state
     */
    @Test
    fun `when refresh fails, should show error`() = runTest {
        // Given
        val errorMessage = "Network error"
        every { getPostsUseCase() } returns flowOf(
            Result.failure(IllegalStateException("No cache"))
        )
        coEvery { refreshPostsUseCase() } returns Result.success(Unit)

        viewModel = PostsViewModel(getPostsUseCase, refreshPostsUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // When - user triggers refresh that fails
        coEvery { refreshPostsUseCase() } returns Result.failure(
            IllegalStateException(errorMessage)
        )
        viewModel.onRefresh()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals(errorMessage, state.error)
    }

    /**
     * Test: onRefresh should trigger refresh use case.
     *
     * Scenario:
     * 1. User triggers refresh
     * 2. refreshPostsUseCase should be called
     * 3. State should show isRefreshing
     */
    @Test
    fun `when onRefresh called, should call refresh use case`() = runTest {
        // Given
        every { getPostsUseCase() } returns flowOf(Result.success(emptyList()))
        coEvery { refreshPostsUseCase() } returns Result.success(Unit)
        viewModel = PostsViewModel(getPostsUseCase, refreshPostsUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onRefresh()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { refreshPostsUseCase() }
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    /**
     * Test: onRetry should clear error and reload posts.
     *
     * Scenario:
     * 1. Initial load fails with error
     * 2. User clicks retry
     * 3. Error cleared and load retried
     */
    @Test
    fun `when onRetry called, should clear error and reload`() = runTest {
        // Given - initial error state
        every { getPostsUseCase() } returns flowOf(
            Result.failure(IllegalStateException("Error"))
        )
        coEvery { refreshPostsUseCase() } returns Result.failure(
            IllegalStateException("Error")
        )
        viewModel = PostsViewModel(getPostsUseCase, refreshPostsUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify error state
        assertTrue(viewModel.uiState.value.error != null)

        // When - retry with success
        every { getPostsUseCase() } returns flowOf(Result.success(samplePosts))
        coEvery { refreshPostsUseCase() } returns Result.success(Unit)
        viewModel.onRetry()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNull(viewModel.uiState.value.error)
        assertEquals(samplePosts, viewModel.uiState.value.posts)
    }

    /**
     * Test: onPostSelected should update selected post.
     *
     * Scenario:
     * 1. User selects a post
     * 2. selectedPost in state should be updated
     */
    @Test
    fun `when onPostSelected called, should update selected post`() = runTest {
        // Given
        every { getPostsUseCase() } returns flowOf(Result.success(samplePosts))
        coEvery { refreshPostsUseCase() } returns Result.success(Unit)
        viewModel = PostsViewModel(getPostsUseCase, refreshPostsUseCase)
        testDispatcher.scheduler.advanceUntilIdle()

        val postToSelect = samplePosts[0]

        // When
        viewModel.onPostSelected(postToSelect)

        // Then
        assertEquals(postToSelect, viewModel.uiState.value.selectedPost)
    }

    /**
     * Test: onPostDeselected should clear selected post.
     */
    @Test
    fun `when onPostDeselected called, should clear selected post`() = runTest {
        // Given
        every { getPostsUseCase() } returns flowOf(Result.success(samplePosts))
        coEvery { refreshPostsUseCase() } returns Result.success(Unit)
        viewModel = PostsViewModel(getPostsUseCase, refreshPostsUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onPostSelected(samplePosts[0])
        assertEquals(samplePosts[0], viewModel.uiState.value.selectedPost)

        // When
        viewModel.onPostDeselected()

        // Then
        assertNull(viewModel.uiState.value.selectedPost)
    }

    /**
     * Test: UiState computed properties should work correctly.
     */
    @Test
    fun `uiState computed properties should return correct values`() = runTest {
        // Given - loading with no data
        val loadingEmptyState = PostsUiState(isLoading = true)

        // Then
        assertFalse(loadingEmptyState.hasPosts)
        assertFalse(loadingEmptyState.isEmpty) // isEmpty = !isLoading && posts.isEmpty()
        assertTrue(loadingEmptyState.showLoading) // showLoading = isLoading && !hasPosts && error == null

        // Given - empty state not loading
        val emptyState = PostsUiState(isLoading = false)

        // Then
        assertFalse(emptyState.hasPosts)
        assertTrue(emptyState.isEmpty)
        assertFalse(emptyState.showLoading)

        // Given - with posts
        val stateWithPosts = PostsUiState(posts = samplePosts)

        // Then
        assertTrue(stateWithPosts.hasPosts)
        assertFalse(stateWithPosts.isEmpty)
        assertEquals(2, stateWithPosts.postCount)
    }

    /**
     * Test: Flow emissions should be collected using Turbine.
     *
     * This demonstrates the Turbine library for Flow testing.
     */
    @Test
    fun `uiState flow should emit state updates`() = runTest {
        // Given
        every { getPostsUseCase() } returns flowOf(Result.success(samplePosts))
        coEvery { refreshPostsUseCase() } returns Result.success(Unit)

        // When & Then - Use Turbine to test Flow emissions
        viewModel = PostsViewModel(getPostsUseCase, refreshPostsUseCase)

        viewModel.uiState.test {
            // First emission: initial state
            val initialState = awaitItem()
            assertTrue(initialState.isLoading || initialState.posts.isEmpty())

            // Advance coroutines
            testDispatcher.scheduler.advanceUntilIdle()

            // Subsequent emissions: updated state
            val finalState = awaitItem()
            assertEquals(samplePosts, finalState.posts)
            assertFalse(finalState.isLoading)

            cancelAndConsumeRemainingEvents()
        }
    }
}
