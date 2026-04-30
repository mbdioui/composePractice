package com.bms.pictet.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bms.pictet.domain.model.Post
import com.bms.pictet.presentation.viewmodels.PostsUiState
import com.bms.pictet.presentation.viewmodels.PostsViewModel

/**
 * PostsScreen: Écran principal affichant les posts depuis l'API JSONPlaceholder.
 *
 * Architecture:
 * - Screen: Point d'entrée avec ViewModel (gestion état)
 * - Content: Composable stateless réutilisable
 * - Components: Composables privés pour chaque état
 *
 * Flux de données:
 * 1. ViewModel charge posts via API
 * 2. UI observe StateFlow
 * 3. Affichage selon état (loading/error/success)
 *
 * @param viewModel Injected by Hilt via hiltViewModel()
 */
@Composable
fun PostsScreen(
    viewModel: PostsViewModel = hiltViewModel()
) {
    // Collecte état UI depuis ViewModel
    // collectAsStateWithLifecycle: optimised pour lifecycle Android
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            PostsTopBar(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.onRefresh() }
            )
        }
    ) { paddingValues ->
        // Pass paddingValues à content pour éviter overlap avec system bars
        PostsContent(
            uiState = uiState,
            onRefresh = { viewModel.onRefresh() },
            onRetry = { viewModel.onRetry() },
            onPostClick = { post -> viewModel.onPostSelected(post) },
            modifier = Modifier.padding(paddingValues)
        )
    }
}

/**
 * Header avec titre et bouton refresh.
 * Implémentation custom sans TopAppBar (expérimental).
 *
 * @param isRefreshing Affiche indicateur si refresh en cours
 * @param onRefresh Callback bouton refresh
 */
@Composable
private fun PostsTopBar(
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Posts",
            style = MaterialTheme.typography.headlineMedium
        )

        IconButton(
            onClick = onRefresh,
            enabled = !isRefreshing
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh posts"
                )
            }
        }
    }
}

/**
 * Content principal avec gestion états.
 *
 * Affiche:
 * - Loading: Premier chargement
 * - Error: Si erreur et pas de données
 * - List: Liste des posts
 * - Error banner: Si erreur mais données existantes
 *
 * @param uiState État UI à afficher
 * @param onRefresh Callback refresh
 * @param onRetry Callback retry après erreur
 * @param onPostClick Callback clic post
 */
@Composable
private fun PostsContent(
    uiState: PostsUiState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onPostClick: (Post) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            // Loading initial: pas de données, pas d'erreur
            uiState.isLoading && !uiState.hasPosts -> {
                LoadingContent()
            }

            // Error sans données: afficher full error
            uiState.error != null && !uiState.hasPosts -> {
                ErrorContent(
                    error = uiState.error,
                    onRetry = onRetry
                )
            }

            // Liste des posts (avec error banner si erreur)
            else -> {
                PostsList(
                    posts = uiState.posts,
                    error = uiState.error,
                    onPostClick = onPostClick
                )
            }
        }
    }
}

/**
 * Indicateur de chargement plein écran.
 */
@Composable
private fun LoadingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading posts...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Content d'erreur avec retry.
 * Affiché quand aucune donnée n'est disponible.
 */
@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icône erreur
        Text(
            text = "⚠️",
            style = MaterialTheme.typography.displayMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Titre erreur
        Text(
            text = "Failed to load posts",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Message détaillé
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Bouton retry
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

/**
 * Liste des posts.
 * Affiche aussi un banner d'erreur si erreur + données existantes.
 *
 * @param posts Liste des posts
 * @param error Message d'erreur optionnel (affiché si présent)
 * @param onPostClick Callback clic
 */
@Composable
private fun PostsList(
    posts: List<Post>,
    error: String?,
    onPostClick: (Post) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Banner d'erreur si erreur mais données existantes
        if (error != null) {
            item {
                ErrorBanner(error = error)
            }
        }

        // Liste des posts
        items(
            items = posts,
            key = { it.id }  // Stable key pour performance
        ) { post ->
            PostCard(
                post = post,
                onClick = { onPostClick(post) }
            )
        }
    }
}

/**
 * Banner d'erreur inline.
 * Affiché quand erreur mais données existantes.
 */
@Composable
private fun ErrorBanner(error: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Text(
            text = "⚠️ $error",
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

/**
 * Carte individuelle pour un post.
 *
 * @param post Post à afficher
 * @param onClick Callback clic
 */
@Composable
private fun PostCard(
    post: Post,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Titre du post
            Text(
                text = post.displayTitle,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Excerpt (aperçu)
            Text(
                text = post.excerpt,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: ID + stats
            Text(
                text = "Post #${post.id} • ${post.wordCount} words • User ${post.userId}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
