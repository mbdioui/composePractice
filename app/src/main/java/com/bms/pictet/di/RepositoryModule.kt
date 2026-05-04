package com.bms.pictet.di

import com.bms.pictet.data.repository.LaunchRepositoryImpl
import com.bms.pictet.domain.repository.LaunchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindLaunchRepository(
        impl: LaunchRepositoryImpl
    ): LaunchRepository
}