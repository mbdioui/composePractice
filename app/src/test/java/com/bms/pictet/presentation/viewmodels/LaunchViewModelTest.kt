package com.bms.pictet.presentation.viewmodels

import com.bms.pictet.domain.model.Launch
import com.bms.pictet.domain.model.Result
import com.bms.pictet.domain.usecase.FilterLaunchesUseCase
import com.bms.pictet.domain.usecase.GetLaunchesUseCase
import com.bms.pictet.domain.usecase.LaunchFilterStatus
import com.bms.pictet.domain.usecase.RefreshLaunchUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LaunchViewModelTest {

    private val dispatcher: TestDispatcher = StandardTestDispatcher()

    private val getLaunchesUseCase: GetLaunchesUseCase = mockk()
    private val refreshLaunchUseCase: RefreshLaunchUseCase = mockk()
    private val filterLaunchesUseCase = FilterLaunchesUseCase()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun whenInitialized_shouldLoadLaunches() = runTest {
        val launches = listOf(sampleLaunch(name = "FalconSat"))
        every { getLaunchesUseCase.invoke() } returns flowOf(Result.Success(launches))

        val viewModel = LaunchViewModel(
            getLaunchesUseCase = getLaunchesUseCase,
            refreshLaunchUseCase = refreshLaunchUseCase,
            filterLaunchesUseCase = filterLaunchesUseCase
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.launches.size)
        assertEquals(1, state.filteredLaunches.size)
        assertTrue(!state.isLoading)
    }

    @Test
    fun whenSearchQueryChanges_shouldFilterLaunches() = runTest {
        val launches = listOf(
            sampleLaunch(id = "1", name = "FalconSat"),
            sampleLaunch(id = "2", name = "Starlink")
        )
        every { getLaunchesUseCase.invoke() } returns flowOf(Result.Success(launches))

        val viewModel = LaunchViewModel(
            getLaunchesUseCase = getLaunchesUseCase,
            refreshLaunchUseCase = refreshLaunchUseCase,
            filterLaunchesUseCase = filterLaunchesUseCase
        )
        advanceUntilIdle()

        viewModel.onSearchQueryChange("falcon")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.filteredLaunches.size)
        assertEquals("FalconSat", state.filteredLaunches.first().name)
    }

    @Test
    fun whenRefreshFails_shouldShowError() = runTest {
        every { getLaunchesUseCase.invoke() } returns flowOf(Result.Success(emptyList()))
        coEvery { refreshLaunchUseCase.invoke() } returns Result.Error("No internet")

        val viewModel = LaunchViewModel(
            getLaunchesUseCase = getLaunchesUseCase,
            refreshLaunchUseCase = refreshLaunchUseCase,
            filterLaunchesUseCase = filterLaunchesUseCase
        )
        advanceUntilIdle()

        viewModel.onRefresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("No internet", state.error)
        assertTrue(!state.isRefreshing)
    }

    private fun sampleLaunch(
        id: String = "launch-1",
        name: String = "Launch",
        status: LaunchFilterStatus = LaunchFilterStatus.ALL
    ): Launch = Launch(
        id = id,
        flightNumber = 1,
        name = name,
        dateUtc = "2006-03-24T22:30:00.000Z",
        upcoming = status == LaunchFilterStatus.UPCOMING,
        success = when (status) {
            LaunchFilterStatus.SUCCESS -> true
            LaunchFilterStatus.FAILED -> false
            else -> null
        },
        details = null,
        links = null,
        rocketId = "rocket-1",
        launchpadId = "launchpad-1"
    )
}
