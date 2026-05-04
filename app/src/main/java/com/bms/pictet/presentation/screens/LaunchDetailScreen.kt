package com.bms.pictet.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.bms.pictet.R
import com.bms.pictet.domain.model.Launch
import com.bms.pictet.domain.model.Rocket
import com.bms.pictet.presentation.theme.SpaceSurfaceHighest
import com.bms.pictet.presentation.viewmodels.LaunchDetailUiState
import com.bms.pictet.presentation.viewmodels.LaunchDetailViewModel

@Composable
fun LaunchDetailScreen(viewModel: LaunchDetailViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is LaunchDetailUiState.Loading -> LoadingContent()
        is LaunchDetailUiState.Error -> ErrorContent(
            message = state.message,
            onRetry = viewModel::onRetry
        )

        is LaunchDetailUiState.Content -> {
            LaunchDetailContent(
                launch = state.launch,
                rocket = state.rocket
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun LaunchDetailContent(
    launch: Launch,
    rocket: Rocket
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = launch.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = launch.links?.patchLarge ?: launch.links?.patchSmall,
                contentDescription = "${launch.name} patch",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = painterResource(R.drawable.ic_patch_placeholder),
                error = painterResource(R.drawable.ic_patch_error)
            )

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = SpaceSurfaceHighest
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Launch details",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("Flight #${launch.flightNumber}")
                    Text("Date: ${launch.formattedDate}")
                    Text("Status: ${launch.isSuccessDisplay}")
                    Text("Description: ${launch.details ?: "Not available"}")
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = SpaceSurfaceHighest
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Rocket",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("Name: ${rocket.name}")
                    Text("Type: ${rocket.type}")
                    Text("Active: ${if (rocket.active) "Yes" else "No"}")
                    Text("Description: ${rocket.description ?: "Not available"}")
                }
            }
        }
    }
}
