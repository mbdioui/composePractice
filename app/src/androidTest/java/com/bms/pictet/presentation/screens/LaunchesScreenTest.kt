package com.bms.pictet.presentation.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import com.bms.pictet.domain.model.Launch
import com.bms.pictet.domain.usecase.LaunchFilterStatus
import com.bms.pictet.presentation.theme.SpaceXplorerTheme
import com.bms.pictet.presentation.viewmodels.LaunchesUiState
import org.junit.Rule
import org.junit.Test

class LaunchesScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun launchesList_displaysLaunchNames() {
        val launch = sampleLaunch(name = "FalconSat")
        val state = LaunchesUiState(
            launches = listOf(launch),
            filteredLaunches = listOf(launch),
            isLoading = false
        )

        composeRule.setContent {
            SpaceXplorerTheme {
                LaunchContent(
                    uiState = state,
                    onRefresh = {},
                    onRetry = {},
                    onSearchQueryChange = {},
                    onFilterStatusChange = {},
                    onLaunchClick = {}
                )
            }
        }

        composeRule.onNodeWithText("FalconSat").assertIsDisplayed()
    }

    @Test
    fun loadingState_showsIndicator() {
        val state = LaunchesUiState(
            isLoading = true,
            launches = emptyList(),
            filteredLaunches = emptyList()
        )

        composeRule.setContent {
            SpaceXplorerTheme {
                LaunchContent(
                    uiState = state,
                    onRefresh = {},
                    onRetry = {},
                    onSearchQueryChange = {},
                    onFilterStatusChange = {},
                    onLaunchClick = {}
                )
            }
        }

        composeRule
            .onNode(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.ProgressBarRangeInfo,
                    ProgressBarRangeInfo.Indeterminate
                )
            )
            .assertIsDisplayed()
    }

    @Test
    fun errorState_showsRetryButton() {
        val state = LaunchesUiState(
            isLoading = false,
            launches = emptyList(),
            filteredLaunches = emptyList(),
            error = "Network error",
            filterStatus = LaunchFilterStatus.ALL
        )

        composeRule.setContent {
            SpaceXplorerTheme {
                LaunchContent(
                    uiState = state,
                    onRefresh = {},
                    onRetry = {},
                    onSearchQueryChange = {},
                    onFilterStatusChange = {},
                    onLaunchClick = {}
                )
            }
        }

        composeRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    private fun sampleLaunch(name: String): Launch = Launch(
        id = "launch-1",
        flightNumber = 1,
        name = name,
        dateUtc = "2006-03-24T22:30:00.000Z",
        upcoming = false,
        success = true,
        details = "Test launch",
        links = null,
        rocketId = "rocket-1",
        launchpadId = "launchpad-1"
    )
}
