package com.bms.pictet.data.datasource

import com.bms.pictet.domain.model.PortfolioItem
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PortfolioRemoteDataSource: Simulates fetching portfolio data from a remote API.
 *
 * DataSource Pattern:
 * - Responsible for fetching data from a specific source (network, database, etc.)
 * - Repository coordinates between multiple DataSources (cache-first strategy)
 * - Each DataSource has single responsibility
 *
 * In production, this would:
 * - Use Retrofit/OkHttp for HTTP requests
 * - Handle API authentication
 * - Parse JSON responses
 * - Handle network errors (timeouts, no connection)
 *
 * @Singleton: Single instance shared across the app (expensive to create)
 */
@Singleton
class PortfolioRemoteDataSource @Inject constructor() {

    /**
     * Simulates fetching portfolio items from remote API.
     *
     * delay(1000): Simulates network latency for realistic testing.
     * In production, this would be an actual HTTP request.
     *
     * @return List of PortfolioItem from "server"
     * @throws Exception if network request fails
     */
    suspend fun fetchPortfolioItems(): List<PortfolioItem> {
        // Simulate network delay (1 second)
        delay(1000)

        // Return mock data simulating API response
        // In production, this would parse JSON from API
        return listOf(
            PortfolioItem(
                id = "1",
                name = "Apple Inc.",
                symbol = "AAPL",
                quantity = 50.0,
                currentPrice = 175.50,
                currency = "USD"
            ),
            PortfolioItem(
                id = "2",
                name = "Microsoft Corp.",
                symbol = "MSFT",
                quantity = 25.0,
                currentPrice = 380.25,
                currency = "USD"
            ),
            PortfolioItem(
                id = "3",
                name = "Tesla Inc.",
                symbol = "TSLA",
                quantity = 30.0,
                currentPrice = 245.80,
                currency = "USD"
            ),
            PortfolioItem(
                id = "4",
                name = "Nestlé SA",
                symbol = "NESN",
                quantity = 40.0,
                currentPrice = 98.45,
                currency = "CHF"
            ),
            PortfolioItem(
                id = "5",
                name = "Roche Holding",
                symbol = "ROG",
                quantity = 15.0,
                currentPrice = 245.30,
                currency = "CHF"
            )
        )
    }

    /**
     * Simulates fetching a single item by ID.
     *
     * @param id Item identifier
     * @return PortfolioItem or null if not found
     */
    suspend fun fetchItemById(id: String): PortfolioItem? {
        delay(500)
        return fetchPortfolioItems().find { it.id == id }
    }
}
