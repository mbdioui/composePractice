package com.bms.pictet.domain.repository

import com.bms.pictet.domain.model.Post
import kotlinx.coroutines.flow.Flow

/**
 * PostRepository: Contrat pour l'accès aux données des posts.
 *
 * Repository Pattern:
 * - Abstraction au-dessus des data sources (API, DB, cache)
 * - Domain ne connaît pas l'implémentation (Retrofit, Room...)
 * - Interface définit les opérations disponibles
 * - Implémentation dans la couche data
 *
 * Pourquoi Flow?
 * - Réactivité: UI se met à jour quand données changent
 * - Cache-first: peut émettre cache puis réseau
 * - Composition: combine, map, filter sur les streams
 *
 * Pourquoi Result<T>?
 * - Type-safe error handling
 * - Force traitement des deux cas (succès/échec)
 * - Plus propre que exceptions pour erreurs métier
 */
interface PostRepository {

    /**
     * Récupère tous les posts en tant que Flow.
     *
     * Le Flow:
     * - Émet immédiatement le cache si disponible
     * - Émet données fraîches après appel réseau
     * - Continue à émettre si données changent
     *
     * @return Flow émettant Result<List<Post>>
     */
    fun getPosts(): Flow<Result<List<Post>>>

    /**
     * Force le rafraîchissement depuis le réseau.
     *
     * À appeler quand:
     * - Pull-to-refresh utilisateur
     * - Reconnexion internet
     * - Données obsolètes
     *
     * @return Result indiquant si le refresh a réussi
     */
    suspend fun refreshPosts(): Result<Unit>

    /**
     * Récupère un post spécifique par ID.
     *
     * Utilise le cache si disponible, sinon appel réseau.
     *
     * @param id Identifiant du post
     * @return Result<Post> avec le post ou erreur
     */
    suspend fun getPostById(id: Int): Result<Post>
}
