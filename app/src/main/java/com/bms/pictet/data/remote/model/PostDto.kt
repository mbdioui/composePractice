package com.bms.pictet.data.remote.model

/**
 * PostDto: Data Transfer Object représentant un post JSONPlaceholder.
 *
 * DTO Pattern:
 * - DTO (Data Transfer Object) = objet pour transfert de données (API <-> App)
 * - Correspond exactement à la structure JSON de l'API
 * - Séparé du modèle domain (PortfolioItem) pour isolation des couches
 * - Permet de gérer les différences entre API et domaine métier
 *
 * jsonplaceholder.typicode.com/posts retourne:
 * {
 *   "userId": 1,
 *   "id": 1,
 *   "title": "sunt aut facere...",
 *   "body": "quia et suscipit..."
 * }
 *
 * @property userId ID de l'utilisateur ayant créé le post
 * @property id ID unique du post
 * @property title Titre du post
 * @property body Contenu du post
 */
data class PostDto(
    val userId: Int,    // Identifiant de l'auteur
    val id: Int,        // Identifiant unique du post
    val title: String,  // Titre
    val body: String    // Contenu textuel
)

/**
 * UserDto: DTO représentant un utilisateur JSONPlaceholder.
 *
 * Structure API:
 * {
 *   "id": 1,
 *   "name": "Leanne Graham",
 *   "username": "Bret",
 *   "email": "Sincere@april.biz",
 *   "address": { ... },
 *   "phone": "1-770-736-8031",
 *   "website": "hildegard.org",
 *   "company": { ... }
 * }
 *
 * Pour simplifier, on garde uniquement les champs utiles.
 */
data class UserDto(
    val id: Int,
    val name: String,
    val username: String,
    val email: String
)
