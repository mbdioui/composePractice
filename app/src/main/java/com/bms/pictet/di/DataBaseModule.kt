package com.bms.pictet.di

import android.content.Context
import androidx.room.Room
import com.bms.pictet.data.local.dao.LaunchDao
import com.bms.pictet.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataBaseModule {

    @Provides
    @Singleton
    fun providesDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "spaceXplorer.db"
        ).build()
    }

    @Provides
    @Singleton
    fun providesDao(database: AppDatabase): LaunchDao {
        return database.launchDao()
    }
}