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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bms.pictet.domain.model.PortfolioItem
import com.bms.pictet.presentation.viewmodels.PortfolioUiState
import com.bms.pictet.presentation.viewmodels.PortfolioViewModel

/**
 * PortfolioScreen: Main screen displaying portfolio information.
 *
 * Architecture Pattern:
 * - State hoisting: ViewModel owns state, Screen is "dumb"
 * - Unidirectional data flow: Events flow up, State flows down
 * - Preview-friendly: Separate UI from ViewModel for previews
 *
 * @param viewModel Injected by Hilt via hiltViewModel()
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    viewModel: PortfolioViewModel = hiltViewModel()
) {
    // Collect UI state from ViewModel
    // collectAsStateWithLifecycle: Optimized for Compose lifecycle
    // - Automatically stops collecting when not visible
    // - Resumes when visible again
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Portfolio") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        // Main content container with padding from Scaffold
        PortfolioContent(
            uiState = uiState,
            onRefresh = { viewModel.onRefresh() },
            onRetry = { viewModel.onRetry() },
            modifier = Modifier.padding(paddingValues)
        )
    }
}

/**
 * PortfolioContent: Stateless composable displaying portfolio UI.
 *
 * Why separate from PortfolioScreen?
 * - Testable without ViewModel (pass mock state)
 * - Preview-friendly (can show different states)
 * - Reusable (could be used in different contexts)
 *
 * @param uiState Current UI state to display
 * @param onRefresh Callback when user requests refresh
 * @param onRetry Callback when user retries after error
 * @param modifier Modifier for layout adjustments
 */
@Composable
private fun PortfolioContent(
    uiState: PortfolioUiState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Box allows layering (loading indicator over content)
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            // Show loading indicator
            uiState.isLoading -> LoadingIndicator()

            // Show error with retry
            uiState.error != null -> ErrorContent(
                error = uiState.error,
                onRetry = onRetry
            )

            // Show portfolio data
            // Check summary != null instead of hasData to enable smart cast
            // Smart cast allows using summary without !! operator
            uiState.summary != null -> PortfolioDataContent(
                summary = uiState.summary,
                modifier = Modifier.fillMaxSize()
            )

            // Initial empty state
            else -> EmptyContent()
        }
    }
}

/**
 * Loading indicator shown during data fetch.
 */
@Composable
private fun LoadingIndicator() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading portfolio...",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Error content shown when data fetch fails.
 *
 * @param error Error message to display
 * @param onRetry Callback when user taps retry
 */
@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⚠️",  // Warning emoji
            style = MaterialTheme.typography.displayMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Error loading portfolio",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

/**
 * Empty content shown when no data available.
 */
@Composable
private fun EmptyContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No portfolio data",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Portfolio data content showing summary and items.
 *
 * @param summary Portfolio summary data
 * @param modifier Modifier for layout
 */
@Composable
private fun PortfolioDataContent(
    summary: com.bms.pictet.domain.model.PortfolioSummary,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Portfolio Summary Card
        item {
            SummaryCard(summary = summary)
        }

        // Section header for top performers
        item {
            Text(
                text = "Top Holdings",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // List of portfolio items
        items(
            items = summary.topPerformers,
            key = { it.id }  // Stable key for LazyColumn performance
        ) { item ->
            PortfolioItemCard(item = item)
        }
    }
}

/**
 * Summary card showing portfolio overview.
 *
 * @param summary Portfolio summary statistics
 */
@Composable
private fun SummaryCard(summary: com.bms.pictet.domain.model.PortfolioSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Total Value",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = "$%,.2f".format(summary.totalValue),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${summary.totalPositions} positions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Individual portfolio item card.
 *
 * @param item Portfolio item data
 */
@Composable
private fun PortfolioItemCard(item: PortfolioItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = item.symbol,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$%,.2f".format(item.totalValue),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${item.quantity.toInt()} shares",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
