package com.bms.pictet.domain.model

/**
 * Post: Modèle domain représentant un article/post.
 *
 * Domain Model vs DTO:
 * - Domain: Orienté métier, indépendant de l'API
 * - DTO: Correspond exactement à la structure API
 * - Séparation permet de gérer les différences
 *   (ex: API change mais domain reste stable)
 *
 * Ce modèle est utilisé dans:
 * - Repository (après mapping depuis DTO)
 * - Use Cases (logique métier)
 * - ViewModel (exposé à l'UI)
 * - UI (affichage)
 *
 * @property id Identifiant unique
 * @property userId ID de l'auteur
 * @property title Titre
 * @property body Contenu
 */
data class Post(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String
) {
    /**
     * Formatted title for display.
     * Capitalise la première lettre.
     */
    val displayTitle: String
        get() = title.replaceFirstChar { it.uppercase() }

    /**
     * Excerpt: premiers 100 caractères du body.
     * Utilisé pour les listes (aperçu).
     */
    val excerpt: String
        get() = if (body.length > 100) {
            body.take(100) + "..."
        } else {
            body
        }

    /**
     * Word count approximation.
     * Simple split par espaces.
     */
    val wordCount: Int
        get() = body.split(Regex("\\s+")).size
}
