package com.bms.pictet.domain.usecase

import com.bms.pictet.domain.model.Post
import com.bms.pictet.domain.repository.PostRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * GetPostsUseCaseTest: Unit tests for GetPostsUseCase.
 *
 * UseCase tests verify:
 * - Delegation to repository
 * - No business logic errors
 * - Proper result propagation
 */
class GetPostsUseCaseTest {

    @MockK
    private lateinit var repository: PostRepository

    private lateinit var getPostsUseCase: GetPostsUseCase
    private lateinit var refreshPostsUseCase: RefreshPostsUseCase

    private val samplePosts = listOf(
        Post(id = 1, userId = 1, title = "Post 1", body = "Body 1"),
        Post(id = 2, userId = 1, title = "Post 2", body = "Body 2")
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        getPostsUseCase = GetPostsUseCase(repository)
        refreshPostsUseCase = RefreshPostsUseCase(repository)
    }

    /**
     * Test: GetPostsUseCase should delegate to repository.
     */
    @Test
    fun `getPostsUseCase should return flow from repository`() = runBlocking {
        // Given
        every { repository.getPosts() } returns flowOf(Result.success(samplePosts))

        // When
        val result: Flow<Result<List<Post>>> = getPostsUseCase()

        // Then
        val firstResult = result.first()
        assertTrue(firstResult.isSuccess)
        assertEquals(samplePosts, firstResult.getOrNull())
        verify { repository.getPosts() }
    }

    /**
     * Test: RefreshPostsUseCase should call repository refresh.
     */
    @Test
    fun `refreshPostsUseCase should call repository refresh`() = runBlocking {
        // Given
        coEvery { repository.refreshPosts() } returns Result.success(Unit)

        // When
        val result = refreshPostsUseCase()

        // Then
        assertTrue(result.isSuccess)
        coVerify { repository.refreshPosts() }
    }

    /**
     * Test: GetPostsUseCase should propagate errors from repository.
     */
    @Test
    fun `getPostsUseCase should propagate errors`() = runBlocking {
        // Given
        val errorMessage = "Network error"
        every { repository.getPosts() } returns flowOf(
            Result.failure(IllegalStateException(errorMessage))
        )

        // When
        val result = getPostsUseCase().first()

        // Then
        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)
    }
}
