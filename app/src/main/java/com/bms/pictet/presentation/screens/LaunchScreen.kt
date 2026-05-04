package com.bms.pictet.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.bms.pictet.domain.model.Launch
import com.bms.pictet.domain.usecase.LaunchFilterStatus
import com.bms.pictet.presentation.viewmodels.LaunchViewModel
import com.bms.pictet.presentation.viewmodels.LaunchesUiState

@Composable
fun LaunchScreen(
    viewModel: LaunchViewModel = hiltViewModel(), onLaunchClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchContent(
        uiState = uiState,
        onRefresh = viewModel::onRefresh,
        onRetry = viewModel::onRetry,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onFilterStatusChange = viewModel::onFilterStatusChange,
        onLaunchClick = viewModel::onLaunchClick
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaunchContent(
    uiState: LaunchesUiState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onFilterStatusChange: (LaunchFilterStatus) -> Unit,
    onLaunchClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SpaceExplorer") }, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    onSearchQueryChange(it)
                },
                placeholder = { Text("Search Launches ...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )

            FilterChips(
                selectedStatus = uiState.filterStatus, onFilterStatusChange = onFilterStatusChange
            )

            when {
                uiState.isLoading && !uiState.hasLaunches -> {
                    LoadingContent()
                }

                uiState.error != null && !uiState.hasLaunches -> {
                    ErrorContent(
                        message = uiState.error,
                        onRetry = onRetry
                    )
                }

                else -> {
                    LaunchesList(
                        launches = uiState.filteredLaunches,
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = onRefresh,
                        onLaunchClick = onLaunchClick
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaunchesList(
    launches: List<Launch>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onLaunchClick: (String) -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh
    ) {
        LazyColumn(
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = launches,
                key = { it.id }
            ) { launch ->
                LaunchCard(
                    launch = launch,
                    onClick = { onLaunchClick(launch.id) }
                )
            }
        }
    }
}

@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun LaunchCard(
    launch: Launch,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mission Patch
            AsyncImage(
                model = launch.links?.patchSmall,
                contentDescription = "${launch.name} patch",
                modifier = Modifier.size(64.dp),
                placeholder = rememberVectorPainter(Icons.Default.Image),
                error = rememberVectorPainter(Icons.Default.Image)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Launch Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = launch.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = launch.formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = launch.isSuccessDisplay,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            // Arrow
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}

@Composable
fun FilterChips(
    selectedStatus: LaunchFilterStatus, onFilterStatusChange: (LaunchFilterStatus) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LaunchFilterStatus.entries.forEach { status ->
            FilterChip(
                selected = selectedStatus == status,
                onClick = { onFilterStatusChange(status) },
                label = { Text(status.name) })
        }
    }
}

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}