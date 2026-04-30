package com.bms.pictet.data.remote.api

import com.bms.pictet.data.remote.model.PostDto
import com.bms.pictet.data.remote.model.UserDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * JsonPlaceholderApi: Interface Retrofit pour l'API JSONPlaceholder.
 *
 * Retrofit Pattern:
 * - Interface annotée avec des méthodes HTTP (@GET, @POST, etc.)
 * - Retrofit génère automatiquement l'implémentation
 * - Coroutines support: méthodes suspend pour appels asynchrones
 * - Response<T> permet d'accéder au code HTTP, headers, body
 *
 * API Base URL: https://jsonplaceholder.typicode.com/
 * Endpoints disponibles:
 * - /posts (100 posts)
 * - /users (10 users)
 * - /comments (500 comments)
 * - /albums, /photos, /todos
 *
 * Toutes les ressources sont en lecture seule (GET).
 * JSONPlaceholder simule aussi POST/PUT/DELETE mais ne persiste pas.
 */
interface JsonPlaceholderApi {

    /**
     * Récupère tous les posts.
     *
     * GET https://jsonplaceholder.typicode.com/posts
     * Retourne: Liste de 100 posts
     *
     * Response wrapper permet de vérifier:
     * - response.isSuccessful: true si HTTP 200-299
     * - response.code(): code HTTP (200, 404, 500...)
     * - response.body(): données désérialisées
     * - response.errorBody(): erreur si non-success
     */
    @GET("posts")
    suspend fun getPosts(): Response<List<PostDto>>

    /**
     * Récupère un post par son ID.
     *
     * GET https://jsonplaceholder.typicode.com/posts/{id}
     * Exemple: /posts/1
     *
     * @param id Identifiant du post
     */
    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") id: Int): Response<PostDto>

    /**
     * Récupère tous les utilisateurs.
     *
     * GET https://jsonplaceholder.typicode.com/users
     * Retourne: Liste de 10 utilisateurs
     */
    @GET("users")
    suspend fun getUsers(): Response<List<UserDto>>

    /**
     * Récupère un utilisateur par son ID.
     *
     * GET https://jsonplaceholder.typicode.com/users/{id}
     *
     * @param id Identifiant de l'utilisateur
     */
    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Int): Response<UserDto>
}
