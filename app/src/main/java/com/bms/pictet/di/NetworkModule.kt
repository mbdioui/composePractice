package com.bms.pictet.di

import com.bms.pictet.data.remote.api.JsonPlaceholderApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * NetworkModule: Module Hilt fournissant les dépendances réseau.
 *
 * Responsabilités:
 * 1. Configurer OkHttpClient (timeouts, interceptors)
 * 2. Configurer Retrofit (base URL, converter)
 * 3. Fournir les instances d'API service
 *
 * @Module: Déclare ce module à Hilt
 * @InstallIn(SingletonComponent::class): Instances créées une seule fois pour toute l'app
 * (équivalent au scope Application)
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * BASE_URL: URL de base de l'API JSONPlaceholder.
     *
     * Tous les endpoints Retrofit seront relatifs à cette URL.
     * Doit se terminer par "/" pour que les chemins relatifs fonctionnent.
     */
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

    /**
     * Fournit OkHttpClient configuré.
     *
     * OkHttp: Client HTTP sous-jacent utilisé par Retrofit.
     * Configuration:
     * - LoggingInterceptor: Log les requêtes/réponses en debug
     *
     * @Singleton: Une seule instance partagée (performance)
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().apply {
            // Interceptor pour logger les requêtes (debug uniquement)
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            addInterceptor(logging)
        }.build()
    }

    /**
     * Fournit Retrofit configuré.
     *
     * Retrofit: Librairie HTTP type-safe pour Android.
     * Configuration:
     * - baseUrl: URL de base de l'API
     * - client: OkHttpClient personnalisé (avec logging)
     * - addConverterFactory: Gson pour parsing JSON automatique
     *
     * @param okHttpClient Client HTTP à utiliser
     * @return Instance Retrofit configurée
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Fournit JsonPlaceholderApi.
     *
     * Retrofit.create(): Génère l'implémentation de l'interface API.
     * Cette implémentation gère:
     * - Sérialisation/désérialisation JSON
     - Exécution des requêtes HTTP
     * - Gestion des threads (coroutines)
     *
     * @param retrofit Instance Retrofit configurée
     * @return Service API prêt à utiliser
     */
    @Provides
    @Singleton
    fun provideJsonPlaceholderApi(retrofit: Retrofit): JsonPlaceholderApi {
        return retrofit.create(JsonPlaceholderApi::class.java)
    }
}
