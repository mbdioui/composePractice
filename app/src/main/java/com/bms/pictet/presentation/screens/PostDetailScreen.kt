package com.bms.pictet.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bms.pictet.domain.model.Post
import com.bms.pictet.presentation.viewmodels.PostDetailViewModel

/**
 * PostDetailScreen: Displays detailed view of a single post.
 *
 * Navigation:
 * - Receives postId from navigation arguments
 * - Fetches post details from repository
 * - Handles back navigation
 *
 * @param postId ID of the post to display
 * @param onNavigateBack Callback when user presses back
 */
@Composable
fun PostDetailScreen(
    postId: Int,
    onNavigateBack: () -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    // Local state for this screen
    var uiState by remember { mutableStateOf(PostDetailUiState()) }

    // Load post when screen is displayed
    LaunchedEffect(postId) {
        uiState = PostDetailUiState(isLoading = true)
        val result = viewModel.getPostById(postId)
        uiState = result.fold(
            onSuccess = { post ->
                PostDetailUiState(post = post)
            },
            onFailure = { error ->
                PostDetailUiState(error = error.message ?: "Failed to load post")
            }
        )
    }

    PostDetailContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRetry = {
            // Retry loading
            uiState = PostDetailUiState(isLoading = true)
        }
    )
}

/**
 * UI state for Post Detail screen.
 */
data class PostDetailUiState(
    val isLoading: Boolean = false,
    val post: Post? = null,
    val error: String? = null
)

/**
 * Content composable for Post Detail.
 */
@Composable
private fun PostDetailContent(
    uiState: PostDetailUiState,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar with back button
        DetailTopBar(
            title = "Post Details",
            onNavigateBack = onNavigateBack
        )

        // Content based on state
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> DetailLoadingIndicator()
                uiState.error != null -> DetailErrorContent(
                    error = uiState.error,
                    onRetry = onRetry
                )
                uiState.post != null -> PostDetailCard(post = uiState.post)
            }
        }
    }
}

/**
 * Top bar with back button for detail screen.
 */
@Composable
private fun DetailTopBar(
    title: String,
    onNavigateBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Go back"
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

/**
 * Loading indicator for detail screen.
 */
@Composable
private fun DetailLoadingIndicator() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading post...",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Error content with retry for detail screen.
 */
@Composable
private fun DetailErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⚠️",
            style = MaterialTheme.typography.displayMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Failed to load post",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = error)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

/**
 * Card displaying post details.
 */
@Composable
private fun PostDetailCard(post: Post) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Post ID badge
            Text(
                text = "Post #${post.id}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = post.displayTitle,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Metadata row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "User ID: ${post.userId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${post.wordCount} words",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Full body text
            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
