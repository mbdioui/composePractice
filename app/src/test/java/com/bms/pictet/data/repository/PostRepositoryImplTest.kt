package com.bms.pictet.data.repository

import app.cash.turbine.test
import com.bms.pictet.data.remote.api.JsonPlaceholderApi
import com.bms.pictet.data.remote.model.PostDto
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

/**
 * PostRepositoryImplTest: Unit tests for PostRepositoryImpl.
 *
 * Testing Strategy:
 * - Mock API responses
 * - Test caching behavior
 * - Test error handling
 * - Test data mapping (DTO -> Domain)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PostRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var api: JsonPlaceholderApi

    private lateinit var repository: PostRepositoryImpl

    private val samplePostDtos = listOf(
        PostDto(id = 1, userId = 1, title = "Title 1", body = "Body 1"),
        PostDto(id = 2, userId = 1, title = "Title 2", body = "Body 2")
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        repository = PostRepositoryImpl(api)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Test: getPosts should emit failure when cache is empty.
     */
    @Test
    fun `getPosts with empty cache should emit failure`() = runTest {
        // When
        repository.getPosts().test {
            // Then
            val result = awaitItem()
            assertTrue(result.isFailure)
            cancelAndConsumeRemainingEvents()
        }
    }

    /**
     * Test: refreshPosts should fetch from API and update cache.
     */
    @Test
    fun `refreshPosts should fetch from API and populate cache`() = runTest {
        // Given
        coEvery { api.getPosts() } returns Response.success(samplePostDtos)

        // When
        val result = repository.refreshPosts()

        // Then
        assertTrue(result.isSuccess)

        // Cache should now have data
        repository.getPosts().test {
            val postsResult = awaitItem()
            assertTrue(postsResult.isSuccess)
            assertEquals(2, postsResult.getOrNull()?.size)
            cancelAndConsumeRemainingEvents()
        }
    }

    /**
     * Test: refreshPosts should return failure when API returns error.
     */
    @Test
    fun `refreshPosts should return failure when API error`() = runTest {
        // Given
        coEvery { api.getPosts() } returns Response.error(500, okhttp3.ResponseBody.create(null, ""))

        // When
        val result = repository.refreshPosts()

        // Then
        assertTrue(result.isFailure)
    }

    /**
     * Test: getPostById should return post from cache if available.
     */
    @Test
    fun `getPostById should return cached post if available`() = runTest {
        // Given - populate cache first
        coEvery { api.getPosts() } returns Response.success(samplePostDtos)
        repository.refreshPosts()

        // When
        val result = repository.getPostById(1)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("Title 1", result.getOrNull()?.title)
    }

    /**
     * Test: DTO should map correctly to Domain model.
     */
    @Test
    fun `postDto should map to domain model correctly`() = runTest {
        // Given
        coEvery { api.getPosts() } returns Response.success(samplePostDtos)

        // When
        repository.refreshPosts()

        // Then
        repository.getPosts().test {
            val result = awaitItem()
            val posts = result.getOrNull()!!

            // Verify mapping
            assertEquals(1, posts[0].id)
            assertEquals("Title 1", posts[0].title)
            assertEquals("Body 1", posts[0].body)

            cancelAndConsumeRemainingEvents()
        }
    }
}
