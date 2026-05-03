package com.bms.pictet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bms.pictet.data.local.entity.LaunchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LaunchDao {
    @Query("SELECT * FROM launches ORDER BY flightNumber DESC")
    fun getAllLaunches(): Flow<List<LaunchEntity>>

    @Query("SELECT * FROM launches WHERE id = :id")
    suspend fun getLaunchById(id: String): LaunchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLaunches(launches: List<LaunchEntity>)

    @Query("DELETE FROM launches")
    suspend fun clearAll()
}