package com.bms.pictet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "launches")
data class LaunchEntity(
    @PrimaryKey
    val id: String,
    val flightNumber: Int,
    val name: String,
    val dateUtc: String,
    val upcoming: Boolean,
    val success: Boolean?,
    val details: String?,
    val patchSmall: String?,
    val patchLarge: String?,
    val webcast: String?,
    val article: String?,
    val wikipedia: String?,
    val rocketId: String,
    val launchpadId: String,
    val cachedAt: Long = System.currentTimeMillis()
)
