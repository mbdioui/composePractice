package com.bms.pictet.domain.model

/**
 * PortfolioItem: Domain model representing a financial portfolio item.
 *
 * Domain models are pure Kotlin classes that represent business entities.
 * They contain ONLY business logic, no Android framework dependencies.
 *
 * This model is used across all layers:
 * - Data layer maps API responses TO this model
 * - Domain layer operates on this model
 * - Presentation layer displays this model (or maps to UI-specific models)
 *
 * @param id Unique identifier for the portfolio item
 * @param name Human-readable name (e.g., "Apple Inc.")
 * @param symbol Trading symbol (e.g., "AAPL")
 * @param quantity Number of shares held
 * @param currentPrice Current market price per share
 * @param currency Currency code (e.g., "USD", "EUR")
 */
data class PortfolioItem(
    val id: String,           // Unique identifier (UUID or API id)
    val name: String,         // Company/asset name
    val symbol: String,       // Trading symbol/ticker
    val quantity: Double,     // Number of shares/units
    val currentPrice: Double, // Current price per unit
    val currency: String      // Currency for the price
) {
    /**
     * Computed property: Total value of this position.
     * Calculated as quantity * currentPrice.
     * This is business logic that belongs in the domain model.
     */
    val totalValue: Double
        get() = quantity * currentPrice

    /**
     * Formatted representation for display.
     * Example: "AAPL - 10 shares @ $150.00 = $1,500.00"
     */
    fun formatForDisplay(): String {
        return "$symbol - ${quantity.toInt()} shares @ ${formatPrice()} = ${formatTotalValue()}"
    }

    /**
     * Helper to format price with currency symbol.
     * In production, this would use NumberFormat with Locale.
     */
    private fun formatPrice(): String {
        val symbol = when (currency) {
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            else -> currency
        }
        return "$symbol%.2f".format(currentPrice)
    }

    /**
     * Helper to format total value.
     */
    private fun formatTotalValue(): String {
        val symbol = when (currency) {
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            else -> currency
        }
        return "$symbol%,.2f".format(totalValue)
    }
}

/**
 * PortfolioSummary: Aggregate data for portfolio overview.
 *
 * @param totalValue Total value of all positions
 * @param totalPositions Number of different assets held
 * @param topPerformers List of best performing assets (by total value)
 */
data class PortfolioSummary(
    val totalValue: Double,
    val totalPositions: Int,
    val topPerformers: List<PortfolioItem>
)
