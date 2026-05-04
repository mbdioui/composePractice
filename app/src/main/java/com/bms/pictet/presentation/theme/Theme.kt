package com.bms.pictet.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

@Composable
fun SpaceXplorerTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = CosmicBlue,
        onPrimary = SpaceBackground,
        secondary = NebulaPurple,
        onSecondary = SpaceBackground,
        background = SpaceBackground,
        onBackground = SpaceOnBackground,
        surface = SpaceSurface,
        onSurface = SpaceOnSurface,
        surfaceVariant = SpaceSurfaceVariant,
        onSurfaceVariant = SpaceOnSurfaceVariant,
        error = SpaceError,
        onError = SpaceBackground,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}