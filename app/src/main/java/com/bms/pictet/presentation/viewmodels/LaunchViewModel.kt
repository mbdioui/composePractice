package com.bms.pictet.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bms.pictet.domain.model.Launch
import com.bms.pictet.domain.model.Result
import com.bms.pictet.domain.usecase.FilterLaunchesUseCase
import com.bms.pictet.domain.usecase.GetLaunchesUseCase
import com.bms.pictet.domain.usecase.LaunchFilterStatus
import com.bms.pictet.domain.usecase.RefreshLaunchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LaunchViewModel @Inject constructor(
    private val getLaunchesUseCase: GetLaunchesUseCase,
    private val refreshLaunchUseCase: RefreshLaunchUseCase,
    private val filterLaunchesUseCase: FilterLaunchesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LaunchesUiState>(LaunchesUiState())
    val uiState: StateFlow<LaunchesUiState> = _uiState

    private val _searchQuery = MutableStateFlow("")
    private val _filterStatus = MutableStateFlow(LaunchFilterStatus.ALL)

    init {
        loadLaunches()
        observeFilters()
    }

    private fun observeFilters() {
        viewModelScope.launch {
            combine(_searchQuery, _filterStatus, _uiState) { query, status, state ->
                Triple(query, status, state.launches)
            }.collect { (query, status, launches) ->
                val filtered = filterLaunchesUseCase(launches, query, status)
                _uiState.update { it.copy(filteredLaunches = filtered) }
            }
        }
    }

    fun onFilterStatusChange(status: LaunchFilterStatus) {
        _filterStatus.value = status
    }

    fun onRefresh() {
        _uiState.update { it.copy(isRefreshing = true) }

        viewModelScope.launch {
            when (val result = refreshLaunchUseCase()) {
                is Result.Success -> {
                    _uiState.update { it.copy(isRefreshing = false) }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            error = result.message
                        )
                    }
                }
                else -> { /* Loading not applicable for refresh */ }
            }
        }
    }

    fun onRetry() {
        _uiState.update { it.copy(error = null) }
        loadLaunches()
    }

    fun onLaunchClick(launchId: String) {
        _uiState.update { it.copy(selectedLaunchId = launchId) }
    }


    private fun loadLaunches() {
        viewModelScope.launch {
            getLaunchesUseCase().collect { result ->
                when (result) {
                    is Result.Loading -> {
                        // Only shows loading on first launch (empty cache)
                        _uiState.update {
                            it.copy(
                                isLoading = true,
                                launches = result.data ?: it.launches
                            )
                        }
                    }

                    is Result.Success -> {
                        // Called twice:
                        // 1. With cached data (fast!)
                        // 2. With fresh data after background refresh
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                launches = result.data,
                                error = null
                            )
                        }
                    }

                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }

                }
            }
        }
        // Note: No manual refresh here! Repository handles it via onStart {}
    }


}

data class LaunchesUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val launches: List<Launch> = emptyList(),
    val filteredLaunches: List<Launch> = emptyList(),
    val error: String? = null,
    val selectedLaunchId: String? = null
) {
    val hasLaunches: Boolean
        get() = filteredLaunches.isNotEmpty()
}