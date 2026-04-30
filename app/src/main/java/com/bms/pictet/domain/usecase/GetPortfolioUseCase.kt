package com.bms.pictet.domain.usecase

import com.bms.pictet.domain.model.PortfolioSummary
import com.bms.pictet.domain.repository.PortfolioRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * GetPortfolioUseCase: Encapsulates the business logic for retrieving portfolio data.
 *
 * UseCase Pattern Purpose:
 * 1. Encapsulates a single business operation
 * 2. Contains business logic that doesn't fit in Repository or ViewModel
 * 3. Makes business rules testable independently
 * 4. Provides clear naming for operations (verb + noun)
 *
 * Why UseCase instead of Repository directly?
 * - Repository handles data access
 * - UseCase handles business rules and orchestration
 * - Example business logic in UseCase:
 *   - Filtering/sorting data
 *   - Combining multiple data sources
 *   - Data transformation for domain requirements
 *   - Validation before operations
 *
 * @Inject constructor: Hilt automatically provides the Repository dependency
 * This is Constructor Injection - dependencies are provided when created
 *
 * @param repository PortfolioRepository for data access
 */
class GetPortfolioUseCase @Inject constructor(
    private val repository: PortfolioRepository
) {
    /**
     * operator fun invoke(): Allows calling use case like a function.
     *
     * Usage:
     * ```
     * val useCase: GetPortfolioUseCase = ...
     * val flow: Flow<Result<PortfolioSummary>> = useCase()  // Called like function
     * ```
     *
     * Why operator invoke?
     * - More concise syntax
     * - Use case behaves like a callable action
     * - Standard pattern in Clean Architecture
     *
     * @return Flow emitting portfolio summary or error
     */
    operator fun invoke(): Flow<Result<PortfolioSummary>> {
        // Currently just delegates to repository.
        // In real app, could add:
        // - Filtering by account type
        // - Sorting by performance
        // - Combining with user preferences
        // - Applying business rules (e.g., hide restricted assets)
        return repository.getPortfolioSummary()
    }
}
