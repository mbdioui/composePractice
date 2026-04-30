package com.bms.pictet.di

import com.bms.pictet.data.repository.PortfolioRepositoryImpl
import com.bms.pictet.domain.repository.PortfolioRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * RepositoryModule: Hilt module for binding Repository implementations.
 *
 * @Binds vs @Provides:
 * - @Binds: Use when you have an interface and implementation (cleaner, compile-time checked)
 * - @Provides: Use when creating instances (e.g., third-party classes)
 *
 * This module binds PortfolioRepositoryImpl to PortfolioRepository interface.
 * When code requests PortfolioRepository, Hilt provides PortfolioRepositoryImpl.
 *
 * @Module: Marks this as a Hilt module
 * @InstallIn(SingletonComponent::class): Bindings available app-wide
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds PortfolioRepositoryImpl to PortfolioRepository interface.
     *
     * @Binds: Tells Hilt "when someone needs PortfolioRepository, use this implementation"
     * @Singleton: Creates only one instance (same impl used everywhere)
     *
     * Abstract method: Hilt generates the implementation
     * Parameter: The concrete implementation to bind
     * Return type: The interface being bound to
     */
    @Binds
    @Singleton
    abstract fun bindPortfolioRepository(
        impl: PortfolioRepositoryImpl
    ): PortfolioRepository
}
