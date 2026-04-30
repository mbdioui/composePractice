package com.bms.pictet.data.repository

import com.bms.pictet.data.remote.api.JsonPlaceholderApi
import com.bms.pictet.data.remote.model.PostDto
import com.bms.pictet.domain.model.Post
import com.bms.pictet.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PostRepositoryImpl: Implementation avec appels réseau réels.
 *
 * Architecture:
 * - Utilise JsonPlaceholderApi pour les vrais appels HTTP
 * - Gère le cache en mémoire pour optimiser les performances
 * - Transforme DTOs (PostDto) en modèles domain (Post)
 * - Gère les erreurs réseau (timeouts, 404, parsing...)
 *
 * Flow Pattern:
 * - Expose des Flow pour réactivité (UI se met à jour automatiquement)
 * - Cache-first: retourne données locales puis rafraîchit
 * - Thread-safe avec Mutex pour accès concurrents au cache
 *
 * @param api Service Retrofit pour appels réseau
 */
@Singleton
class PostRepositoryImpl @Inject constructor(
    private val api: JsonPlaceholderApi
) : PostRepository {

    // Mutex pour thread-safety sur le cache
    private val cacheMutex = Mutex()

    // Cache en mémoire des posts
    private val _cachedPosts = MutableStateFlow<List<Post>>(emptyList())

    /**
     * Récupère tous les posts via Flow.
     *
     * Pattern:
     * 1. Émet le cache s'il existe (UI répond instantanément)
     * 2. Appel réseau en parallèle pour données fraîches
     * 3. Met à jour cache et émet nouvelles données
     *
     * @return Flow émettant Result<List<Post>> avec cache puis données réseau
     */
    override fun getPosts(): Flow<Result<List<Post>>> {
        return _cachedPosts.map { cached ->
            if (cached.isNotEmpty()) {
                Result.success(cached)
            } else {
                Result.failure(IllegalStateException("No cached data"))
            }
        }
    }

    /**
     * Rafraîchit les données depuis le réseau.
     *
     * Implémentation réelle:
     * 1. Appel API suspend (getPosts())
     * 2. Vérification réponse HTTP (isSuccessful)
     * 3. Mapping DTO -> Domain model
     * 4. Mise à jour cache thread-safe
     * 5. Gestion erreurs (IOException, HttpException...)
     *
     * @return Result<Unit> indiquant succès ou échec
     */
    override suspend fun refreshPosts(): Result<Unit> {
        return try {
            // Appel réseau réel via Retrofit
            val response = api.getPosts()

            if (response.isSuccessful) {
                // Succès HTTP 2xx
                val postsDto = response.body()
                    ?: return Result.failure(IllegalStateException("Empty response"))

                // Mapping DTO -> Domain
                val posts = postsDto.map { it.toDomainModel() }

                // Mise à jour cache thread-safe
                cacheMutex.withLock {
                    _cachedPosts.value = posts
                }

                Result.success(Unit)
            } else {
                // Erreur HTTP (4xx, 5xx)
                val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                Result.failure(IllegalStateException(errorMsg))
            }
        } catch (e: Exception) {
            // Exceptions réseau (IOException, UnknownHostException, etc.)
            Result.failure(e)
        }
    }

    /**
     * Récupère un post par ID.
     *
     * Stratégie:
     * 1. Cherche dans le cache d'abord (rapide)
     * 2. Si absent, appel réseau
     *
     * @param id ID du post
     * @return Result<Post> avec le post ou erreur
     */
    override suspend fun getPostById(id: Int): Result<Post> {
        return try {
            // Vérifie cache d'abord
            val cached = cacheMutex.withLock {
                _cachedPosts.value.find { it.id == id }
            }

            if (cached != null) {
                return Result.success(cached)
            }

            // Appel réseau si pas en cache
            val response = api.getPostById(id)

            if (response.isSuccessful) {
                val postDto = response.body()
                    ?: return Result.failure(IllegalStateException("Empty response"))

                Result.success(postDto.toDomainModel())
            } else {
                Result.failure(IllegalStateException("HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extension function: Mapping DTO -> Domain.
     *
     * Pourquoi une extension function?
     * - Clean: logique de mapping co-localisée
     * - Réutilisable: peut être utilisée ailleurs si besoin
     * - Testable: facile à tester unitairement
     *
     * Dans un vrai projet, utiliser un Mapper dédié ou MapStruct.
     */
    private fun PostDto.toDomainModel(): Post {
        return Post(
            id = this.id,
            userId = this.userId,
            title = this.title,
            body = this.body
        )
    }
}
