package com.bms.pictet.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bms.pictet.domain.model.Post
import com.bms.pictet.domain.usecase.GetPostsUseCase
import com.bms.pictet.domain.usecase.RefreshPostsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * PostsViewModel: Gère l'état UI pour l'écran des posts.
 *
 * Responsabilités:
 * 1. Exposer l'état UI (PostsUiState)
 * 2. Gérer les événements utilisateur (refresh, retry)
 * 3. Coordonner les appels aux Use Cases
 * 4. Gérer les états de chargement/erreur
 *
 * StateFlow Pattern:
 * - MutableStateFlow privé (_uiState): modifiable par ViewModel uniquement
 * - StateFlow public (uiState): UI observe, ne modifie pas directement
 * - Mise à jour atomique avec update { it.copy(...) }
 *
 * @param getPostsUseCase UseCase pour observer les posts (Flow)
 * @param refreshPostsUseCase UseCase pour forcer le refresh (suspend)
 */
@HiltViewModel
class PostsViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase,
    private val refreshPostsUseCase: RefreshPostsUseCase
) : ViewModel() {

    // État UI privé (seul ViewModel modifie)
    private val _uiState = MutableStateFlow(PostsUiState())

    // État UI public (UI observe)
    val uiState: StateFlow<PostsUiState> = _uiState.asStateFlow()

    /**
     * Init: charge les données au démarrage.
     *
     * Stratégie:
     * 1. Démarrer la collecte du Flow (pour recevoir les mises à jour)
     * 2. Déclencher immédiatement le refresh depuis le réseau
     *
     * Sans le refresh initial, le cache est vide et on affiche une erreur!
     */
    init {
        loadPosts()          // Démarre l'observation du cache
        refreshInitialData() // Charge depuis le réseau
    }

    /**
     * Charge les posts avec gestion états.
     *
     * Pattern:
     * 1. État Loading
     * 2. Collecte du Flow depuis UseCase
     * 3. Mise à jour état selon Result (Success/Error)
     * 4. Gestion erreurs avec try/catch
     */
    private fun loadPosts() {
        // État loading
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                // Collecte le Flow depuis UseCase
                getPostsUseCase().collect { result ->
                    result.fold(
                        onSuccess = { posts ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    posts = posts,
                                    error = null
                                )
                            }
                        },
                        onFailure = { error ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = error.message ?: "Unknown error"
                                )
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                // Erreur inattendue (cancellation, etc.)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unexpected error"
                    )
                }
            }
        }
    }

    /**
     * Rafraîchissement initial au démarrage.
     *
     * CRITIQUE: Sans cet appel, le cache reste vide et l'affiche une erreur.
     * On utilise launch avec le résultat du refresh.
     */
    private fun refreshInitialData() {
        viewModelScope.launch {
            refreshPostsUseCase()
            // Le résultat arrive via le Flow (getPostsUseCase)
            // car refreshPosts() met à jour _cachedPosts
        }
    }

    /**
     * Rafraîchit les données depuis le réseau.
     *
     * Déclenché par:
     * - Pull-to-refresh utilisateur
     * - Bouton refresh
     * - Reconnexion internet
     *
     * Affiche indicateur refresh sans masquer les données existantes.
     */
    fun onRefresh() {
        _uiState.update { it.copy(isRefreshing = true, error = null) }

        viewModelScope.launch {
            val result = refreshPostsUseCase()

            result.fold(
                onSuccess = {
                    // Les données arrivent via le Flow (getPostsUseCase)
                    // On désactive juste l'indicateur refresh
                    _uiState.update { it.copy(isRefreshing = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            error = error.message ?: "Refresh failed"
                        )
                    }
                }
            )
        }
    }

    /**
     * Réessaie après une erreur.
     *
     * Réinitialise l'état error et relance le chargement.
     */
    fun onRetry() {
        _uiState.update { it.copy(error = null) }
        loadPosts()
    }

    /**
     * Gère la sélection d'un post.
     *
     * Pourrait naviguer vers detail ou afficher dialog.
     * Pour l'instant, juste log.
     */
    fun onPostSelected(post: Post) {
        // Navigation vers detail screen
        // ou afficher bottom sheet avec détails
        _uiState.update { it.copy(selectedPost = post) }
    }

    /**
     * Désélectionne le post.
     */
    fun onPostDeselected() {
        _uiState.update { it.copy(selectedPost = null) }
    }
}

/**
 * PostsUiState: État complet de l'écran Posts.
 *
 * Single Source of Truth:
 * - Toute l'UI dérive de cet état
 * - Pas d'autres StateFlow/LiveData
 * - Immutable (copie avec copy())
 *
 * Propriétés:
 * - isLoading: Premier chargement (plein écran)
 * - isRefreshing: Pull-to-refresh (indicateur inline)
 * - posts: Liste des posts affichés
 * - error: Message d'erreur ou null
 * - selectedPost: Post sélectionné ou null
 */
data class PostsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val posts: List<Post> = emptyList(),
    val error: String? = null,
    val selectedPost: Post? = null
) {
    /**
     * True si des posts sont disponibles.
     * Utilisé pour afficher liste ou empty state.
     */
    val hasPosts: Boolean
        get() = posts.isNotEmpty()

    /**
     * True si afficher indicateur chargement initial.
     * (pas refresh, pas d'erreur, pas de données)
     */
    val showLoading: Boolean
        get() = isLoading && !hasPosts && error == null

    /**
     * True si pas de posts et pas en chargement.
     */
    val isEmpty: Boolean
        get() = !isLoading && posts.isEmpty()

    /**
     * Nombre de posts pour affichage.
     */
    val postCount: Int
        get() = posts.size
}