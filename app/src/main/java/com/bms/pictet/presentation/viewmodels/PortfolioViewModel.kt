package com.bms.pictet.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bms.pictet.domain.model.PortfolioSummary
import com.bms.pictet.domain.usecase.GetPortfolioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * PortfolioViewModel: Manages UI state for Portfolio screen.
 *
 * MVVM Responsibilities:
 * - Exposes UI state as immutable StateFlow
 * - Handles user interactions (Intents/Events)
 * - Manages business logic coordination (calls UseCases)
 * - Survives configuration changes (rotation)
 *
 * @HiltViewModel: Enables constructor injection for ViewModel
 * Hilt provides the GetPortfolioUseCase automatically
 *
 * @param getPortfolioUseCase Use case for fetching portfolio data
 */
@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val getPortfolioUseCase: GetPortfolioUseCase
) : ViewModel() {

    // Private mutable state - only ViewModel can modify
    // MutableStateFlow: Allows thread-safe updates via .value = ...
    private val _uiState = MutableStateFlow(PortfolioUiState())

    // Public immutable state - UI observes this
    // asStateFlow(): Returns read-only view of the state
    val uiState: StateFlow<PortfolioUiState> = _uiState.asStateFlow()

    /**
     * Initialize ViewModel: Load data immediately.
     *
     * init block runs when ViewModel is created.
     * viewModelScope is bound to ViewModel lifecycle.
     */
    init {
        loadPortfolio()
    }

    /**
     * Load portfolio data from repository.
     *
     * Pattern:
     * 1. Set loading state immediately
     * 2. Collect from Flow (handles async operations)
     * 3. Update state based on Result
     *
     * Why Flow instead of suspend function?
     * - Repository can emit multiple times (cached then fresh)
     * - UI reacts to each emission automatically
     */
    private fun loadPortfolio() {
        // Update state to loading
        _uiState.update { it.copy(isLoading = true, error = null) }

        // Collect from use case Flow
        getPortfolioUseCase()
            .onEach { result ->
                // Called each time repository emits new data
                result.fold(
                    onSuccess = { summary ->
                        // Success: update UI with data
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                summary = summary,
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        // Failure: show error, keep existing data if any
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Unknown error"
                            )
                        }
                    }
                )
            }
            .catch { error ->
                // Handle unexpected exceptions
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Unexpected error"
                    )
                }
            }
            .launchIn(viewModelScope)  // Collect in ViewModel's scope
    }

    /**
     * Handle user refresh action.
     *
     * Called when user pulls to refresh or taps refresh button.
     * Triggers repository refresh and updates UI accordingly.
     *
     * Why separate function from loadPortfolio?
     * - User-initiated vs automatic loading
     * - Could add refresh-specific logic (show toast, track analytics)
     * - Clear separation of concerns
     */
    fun onRefresh() {
        // For this demo, reload triggers fresh data fetch
        // In real app, would call repository.refreshPortfolio()
        loadPortfolio()
    }

    /**
     * Handle user retry after error.
     *
     * Called when user taps "Retry" button in error state.
     * Clears error and attempts to reload.
     */
    fun onRetry() {
        _uiState.update { it.copy(error = null) }
        loadPortfolio()
    }
}

/**
 * PortfolioUiState: Immutable data class representing entire UI state.
 *
 * Single Source of Truth for the UI:
 * - UI has ONE observable state, not multiple LiveData/StateFlows
 * - All UI decisions based on this state
 * - Prevents partial state bugs
 *
 * State properties:
 * - isLoading: Show loading indicator
 * - summary: Display portfolio data (null until loaded)
 * - error: Error message to display (null if no error)
 */
data class PortfolioUiState(
    val isLoading: Boolean = false,
    val summary: PortfolioSummary? = null,
    val error: String? = null
) {
    /**
     * Computed properties for UI convenience.
     * These help simplify UI logic.
     */
    val hasData: Boolean
        get() = summary != null

    val isEmpty: Boolean
        get() = !isLoading && summary?.totalPositions == 0

    val totalValueFormatted: String
        get() = summary?.let { "%,.2f".format(it.totalValue) } ?: "0.00"
}
