package com.bms.pictet.data.repository

import com.bms.pictet.data.datasource.PortfolioRemoteDataSource
import com.bms.pictet.domain.model.PortfolioItem
import com.bms.pictet.domain.model.PortfolioSummary
import com.bms.pictet.domain.repository.PortfolioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PortfolioRepositoryImpl: Implementation of PortfolioRepository.
 *
 * Responsibilities:
 * 1. Coordinates between data sources (remote, local cache)
 * 2. Implements caching strategy (refresh-first shown in this example)
 * 3. Maps data source results to domain models
 * 4. Provides reactive updates via StateFlow
 * 5. Thread-safe operations using Mutex
 *
 * @Singleton: Repository is expensive to create and should be shared
 * @Inject: Hilt provides dependencies via constructor injection
 *
 * @param remoteDataSource Fetches data from network/API
 */
@Singleton
class PortfolioRepositoryImpl @Inject constructor(
    private val remoteDataSource: PortfolioRemoteDataSource
) : PortfolioRepository {

    // Mutex for thread-safe cache updates
    // Prevents race conditions when multiple coroutines access cache
    private val cacheMutex = Mutex()

    // In-memory cache of portfolio items
    // StateFlow allows reactive observation of cache changes
    private val _cachedItems = MutableStateFlow<List<PortfolioItem>>(emptyList())

    /**
     * Get portfolio items as reactive Flow.
     *
     * Strategy:
     * 1. Return cached data immediately (if available) - fast UI
     * 2. Trigger refresh in background - fresh data
     * 3. Emit updated data when refresh completes
     *
     * This is "cache-first with background refresh" pattern.
     */
    override fun getPortfolioItems(): Flow<Result<List<PortfolioItem>>> {
        // Map the cached items StateFlow to Result-wrapped values
        return _cachedItems.map { items ->
            if (items.isNotEmpty()) {
                Result.success(items)
            } else {
                // No cache available - return loading/error state
                Result.failure(IllegalStateException("No data available"))
            }
        }
    }

    /**
     * Get portfolio summary with computed aggregates.
     *
     * Transforms raw items into summary statistics:
     * - Total value across all positions
     * - Count of positions
     * - Top performers by value
     */
    override fun getPortfolioSummary(): Flow<Result<PortfolioSummary>> {
        return _cachedItems.map { items ->
            if (items.isEmpty()) {
                Result.failure(IllegalStateException("No portfolio data"))
            } else {
                // Calculate summary statistics
                val totalValue = items.sumOf { it.totalValue }
                val totalPositions = items.size
                // Sort by total value descending, take top 3
                val topPerformers = items.sortedByDescending { it.totalValue }.take(3)

                Result.success(
                    PortfolioSummary(
                        totalValue = totalValue,
                        totalPositions = totalPositions,
                        topPerformers = topPerformers
                    )
                )
            }
        }
    }

    /**
     * Refresh data from remote source.
     *
     * This is a suspend function - can be called from coroutines.
     * Uses Mutex for thread-safe cache updates.
     *
     * @return Result indicating success or failure
     */
    override suspend fun refreshPortfolio(): Result<Unit> {
        return try {
            // Fetch fresh data from network
            val freshItems = remoteDataSource.fetchPortfolioItems()

            // Thread-safe cache update
            cacheMutex.withLock {
                _cachedItems.value = freshItems
            }

            Result.success(Unit)
        } catch (e: Exception) {
            // Log error in production: Log.e(TAG, "Failed to refresh", e)
            Result.failure(e)
        }
    }

    /**
     * Get single item by ID.
     *
     * Strategy: Check cache first, fallback to remote.
     */
    override suspend fun getPortfolioItem(id: String): Result<PortfolioItem> {
        return try {
            // Check cache first
            val cachedItem = cacheMutex.withLock {
                _cachedItems.value.find { it.id == id }
            }

            if (cachedItem != null) {
                Result.success(cachedItem)
            } else {
                // Fetch from remote if not in cache
                remoteDataSource.fetchItemById(id)?.let {
                    Result.success(it)
                } ?: Result.failure(NoSuchElementException("Item not found: $id"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
