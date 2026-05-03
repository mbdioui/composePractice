package com.bms.pictet.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bms.pictet.data.local.dao.LaunchDao
import com.bms.pictet.data.local.entity.LaunchEntity

@Database(entities = [LaunchEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun launchDao(): LaunchDao
}