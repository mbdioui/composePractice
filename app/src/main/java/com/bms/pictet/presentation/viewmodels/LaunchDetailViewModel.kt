package com.bms.pictet.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bms.pictet.domain.model.Launch
import com.bms.pictet.domain.model.Result
import com.bms.pictet.domain.model.Rocket
import com.bms.pictet.domain.usecase.GetLaunchDetailsUseCase
import com.bms.pictet.domain.usecase.GetRocketDetailsUseCase
import com.bms.pictet.presentation.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LaunchDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getLaunchDetailsUseCase: GetLaunchDetailsUseCase,
    private val getRocketDetailsUseCase: GetRocketDetailsUseCase
) : ViewModel() {

    private val launchId: String = savedStateHandle.get<String>(NavRoutes.LAUNCH_ID_ARG).orEmpty()

    private val _uiState = MutableStateFlow<LaunchDetailUiState>(LaunchDetailUiState.Loading)
    val uiState: StateFlow<LaunchDetailUiState> = _uiState.asStateFlow()

    init {
        loadLaunchDetails()
    }

    fun onRetry() {
        loadLaunchDetails()
    }

    private fun loadLaunchDetails() {
        if (launchId.isBlank()) {
            _uiState.update { LaunchDetailUiState.Error("Missing launch id.") }
            return
        }

        _uiState.update { LaunchDetailUiState.Loading }
        viewModelScope.launch {
            when (val launchResult = getLaunchDetailsUseCase(launchId)) {
                is Result.Success -> {
                    loadRocket(launchResult.data)
                }

                is Result.Error -> {
                    _uiState.update { LaunchDetailUiState.Error(launchResult.message) }
                }

                is Result.Loading -> {
                    _uiState.update { LaunchDetailUiState.Loading }
                }
            }
        }
    }

    private suspend fun loadRocket(launch: Launch) {
        when (val rocketResult = getRocketDetailsUseCase(launch.rocketId)) {
            is Result.Success -> {
                _uiState.update {
                    LaunchDetailUiState.Content(
                        launch = launch,
                        rocket = rocketResult.data
                    )
                }
            }

            is Result.Error -> {
                _uiState.update { LaunchDetailUiState.Error(rocketResult.message) }
            }

            is Result.Loading -> {
                _uiState.update { LaunchDetailUiState.Loading }
            }
        }
    }
}

sealed interface LaunchDetailUiState {
    data object Loading : LaunchDetailUiState
    data class Error(val message: String) : LaunchDetailUiState
    data class Content(
        val launch: Launch,
        val rocket: Rocket
    ) : LaunchDetailUiState
}
