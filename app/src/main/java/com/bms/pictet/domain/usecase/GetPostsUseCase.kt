package com.bms.pictet.domain.usecase

import com.bms.pictet.domain.model.Post
import com.bms.pictet.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * GetPostsUseCase: Cas d'utilisation pour récupérer les posts.
 *
 * UseCase Pattern:
 * - Encapsule une opération métier spécifique
 * - Nom explicite: verbe + nom (GetPosts)
 * - Réutilisable entre plusieurs ViewModels
 * - Testable indépendamment du Repository
 *
 * operator fun invoke():
 * - Permet d'appeler le use case comme une fonction
 * - Syntaxe: useCase() au lieu de useCase.execute()
 *
 * Responsabilités:
 * - Orchestrer appels au Repository
 * - Appliquer règles métier (filtrage, tri...)
 * - Transformer données si nécessaire
 *
 * @param repository Repository injecté par Hilt
 */
class GetPostsUseCase @Inject constructor(
    private val repository: PostRepository
) {

    /**
     * Exécute le use case.
     *
     * Retourne un Flow de Result pour:
     * - Réactivité: UI mise à jour automatiquement
     * - Error handling: succès/échec explicitement typés
     * - Loading states: peut émettre loading, puis données
     *
     * @return Flow émettant Result<List<Post>>
     */
    operator fun invoke(): Flow<Result<List<Post>>> {
        return repository.getPosts()
    }
}

/**
 * RefreshPostsUseCase: Cas d'utilisation pour rafraîchir les posts.
 *
 * Séparé de GetPostsUseCase car:
 * - Opération différente (force refresh vs observe)
 * - Ne retourne pas de Flow (one-shot operation)
 * - Peut avoir logique métier spécifique (ex: invalidation cache)
 *
 * @param repository Repository pour accès données
 */
class RefreshPostsUseCase @Inject constructor(
    private val repository: PostRepository
) {

    /**
     * Exécute le refresh.
     *
     * Suspend function: doit être appelée depuis une coroutine.
     * Retourne Result<Unit>: succès ou échec, pas de données.
     *
     * @return Result indiquant succès/échec du refresh
     */
    suspend operator fun invoke(): Result<Unit> {
        return repository.refreshPosts()
    }
}
