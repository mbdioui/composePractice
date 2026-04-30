package com.bms.pictet.di

import com.bms.pictet.data.repository.PostRepositoryImpl
import com.bms.pictet.domain.repository.PostRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * RepositoryModule: Module Hilt liant interfaces et implémentations.
 *
 * @Binds: Utilisé quand on a une interface et son implémentation.
 * Plus efficace que @Provides car généré à la compilation.
 *
 * Pattern:
 * - Interface dans domain layer
 * - Implémentation dans data layer
 * - Binding dans module Hilt
 *
 * Séparation des couches:
 * - Domain ne dépend pas de Data
 * - Data dépend de Domain (implémente interface)
 * - DI fournit l'implémentation au runtime
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Lie PostRepositoryImpl à PostRepository.
     *
     * Quand du code demande PostRepository (dans ViewModel, UseCase...),
     * Hilt fournit PostRepositoryImpl.
     *
     * @param impl Implémentation concrète (injectée automatiquement)
     * @return Interface que le code consommateur utilise
     */
    @Binds
    @Singleton
    abstract fun bindPostRepository(
        impl: PostRepositoryImpl
    ): PostRepository
}
