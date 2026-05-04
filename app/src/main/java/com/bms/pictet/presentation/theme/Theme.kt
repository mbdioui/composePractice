package com.bms.pictet.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

@Composable
fun SpaceXplorerTheme(
    darkTheme: Boolean = true, // Space theme is dark
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = SpaceBlue,
        secondary = SpaceGray,
        background = SpaceBlack,
        surface = SpaceGray,
        error = SpaceRed
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}