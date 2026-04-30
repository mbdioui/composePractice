package com.bms.pictet.domain.repository

import com.bms.pictet.domain.model.PortfolioItem
import com.bms.pictet.domain.model.PortfolioSummary
import kotlinx.coroutines.flow.Flow

/**
 * PortfolioRepository: Interface defining the contract for portfolio data access.
 *
 * Repository Pattern Purpose:
 * 1. Abstracts data source details from the rest of the app
 * 2. Provides a clean API for data access
 * 3. Handles data operations (fetch, cache, sync)
 * 4. Acts as Single Source of Truth for domain data
 *
 * Interface vs Implementation:
 * - Interface is in DOMAIN layer: no implementation details
 * - Implementation is in DATA layer: knows about APIs, databases
 * - This separation enables:
 *   - Testing (can mock the interface)
 *   - Swapping implementations (local vs remote)
 *   - Clean architecture (domain doesn't depend on data layer)
 *
 * Why Flow instead of suspend functions?
 * - Flow allows reactive updates (data changes automatically refresh UI)
 * - Repository can emit loading states, cached data, then fresh data
 * - Multiple collectors can observe same data source
 */
interface PortfolioRepository {

    /**
     * Get portfolio items as a Flow.
     *
     * Returns Flow<Result<List<PortfolioItem>>>:
     * - Result: Success/Failure wrapper for error handling
     * - Flow: Reactive stream that emits updates
     *
     * Usage:
     * ```
     * repository.getPortfolioItems().collect { result ->
     *     result.fold(
     *         onSuccess = { items -> updateUI(items) },
     *         onFailure = { error -> showError(error) }
     *     )
     * }
     * ```
     */
    fun getPortfolioItems(): Flow<Result<List<PortfolioItem>>>

    /**
     * Get portfolio summary with computed aggregates.
     *
     * @return Flow emitting summary data or error
     */
    fun getPortfolioSummary(): Flow<Result<PortfolioSummary>>

    /**
     * Refresh portfolio data from remote source.
     * Triggers a network request and updates local cache.
     *
     * @return Result indicating success/failure of refresh operation
     */
    suspend fun refreshPortfolio(): Result<Unit>

    /**
     * Get a single portfolio item by ID.
     *
     * @param id Portfolio item identifier
     * @return Result with item or error if not found
     */
    suspend fun getPortfolioItem(id: String): Result<PortfolioItem>
}
