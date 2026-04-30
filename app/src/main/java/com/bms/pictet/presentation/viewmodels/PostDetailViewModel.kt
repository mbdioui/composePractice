package com.bms.pictet.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.bms.pictet.domain.model.Post
import com.bms.pictet.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * PostDetailViewModel: Manages UI state for Post Detail screen.
 *
 * This ViewModel is simpler than PostsViewModel because:
 * - It handles a single operation (fetch one post)
 * - No reactive Flow needed (one-shot operation)
 * - Returns Result directly to the UI
 *
 * @param repository Repository for fetching post data
 */
@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    /**
     * Fetches a single post by ID.
     *
     * Unlike the list screen, this is a suspend function that returns
     * a Result directly. The UI calls this and handles the result.
     *
     * Pattern differences:
     * - List screen: Uses Flow for reactive updates
     * - Detail screen: One-shot suspend function (simpler)
     *
     * @param postId ID of the post to fetch
     * @return Result<Post> with the post or error
     */
    suspend fun getPostById(postId: Int): Result<Post> {
        return repository.getPostById(postId)
    }
}
