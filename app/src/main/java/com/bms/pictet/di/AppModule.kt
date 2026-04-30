package com.bms.pictet.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * AppModule: Provides application-level dependencies using Hilt.
 *
 * @Module: Marks this class as a Hilt module
 * A module is responsible for providing dependencies that Hilt cannot instantiate automatically
 * (e.g., interfaces, third-party classes)
 *
 * @InstallIn(SingletonComponent::class): Specifies the component scope
 * SingletonComponent means these dependencies live as long as the application
 * (equivalent to @Singleton scope)
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Example: Providing a Retrofit instance
    // @Provides: Tells Hilt how to create instances of this type
    // @Singleton: Creates only one instance shared across the entire app
    /*
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    */

    // Example: Providing a Repository
    /*
    @Provides
    @Singleton
    fun provideRepository(
        apiService: ApiService  // Hilt will find this dependency automatically
    ): Repository {
        return RepositoryImpl(apiService)
    }
    */
}
