package com.bms.pictet.domain.repository

import com.bms.pictet.domain.model.Launch
import com.bms.pictet.domain.model.Result
import com.bms.pictet.domain.model.Rocket
import kotlinx.coroutines.flow.Flow

interface LaunchRepository {
    /**
     * Offline-First: Returns cached data immediately (if available),
     * then silently refreshes from remote.
     * Flow emits:
     *   - Loading (on first launch when cache empty)
     *   - Success (cached data immediately)
     *   - Success (fresh data after background refresh)
     *   - Error (if network fails AND cache is empty)
     */
    fun getLaunches(): Flow<Result<List<Launch>>>

    /**
     * Force refresh from remote (for pull-to-refresh)
     */
    suspend fun refreshLaunches(): Result<Unit>

    /**
     * Get single launch (cache first, then remote)
     */
    suspend fun getLaunch(id: String): Result<Launch>

    suspend fun getRocket(id: String): Result<Rocket>
}
