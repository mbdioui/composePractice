package com.bms.pictet.presentation.theme

// Checks if device is in dark mode (battery saver or system setting)
import android.app.Activity
// Android OS Build version info
import android.os.Build
// Composable function marker
import androidx.compose.foundation.isSystemInDarkTheme
// Material 3 ColorScheme class - defines app color palette
import androidx.compose.material3.MaterialTheme
// Material 3 dark color scheme preset
import androidx.compose.material3.darkColorScheme
// Material 3 dynamic dark color scheme (Android 12+ feature)
import androidx.compose.material3.dynamicDarkColorScheme
// Material 3 dynamic light color scheme (Android 12+ feature)
import androidx.compose.material3.dynamicLightColorScheme
// Material 3 light color scheme preset
import androidx.compose.material3.lightColorScheme
// Marks function as composable
import androidx.compose.runtime.Composable
// For accessing current context
import androidx.compose.ui.platform.LocalContext

// Light theme color scheme - maps semantic colors to actual color values
// primary: Main brand color for buttons, selected states
// secondary: Accent color for less prominent components
// tertiary: Additional accent for contrasting elements
// background: Screen background color
// surface: Card, sheet, and menu background colors
// error: Error states and validation messages
private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)

// Dark theme color scheme - inverted colors for dark mode
// "on" colors are designed to be readable on their "base" color
// Containers are slightly different shade of the base color
private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
)

// PictetTheme: Root theme composable that wraps entire app content
// Applies colors, typography and shapes consistently across all screens
//
// Parameters:
// darkTheme: Boolean - use dark colors if true, light if false
//            defaults to system setting via isSystemInDarkTheme()
// dynamicColor: Boolean - use Material You dynamic colors (Android 12+)
//               generates palette from device wallpaper colors
// content: Composable lambda - actual UI content to theme
@Composable
fun PictetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // Determine which color scheme to use
    val colorScheme = when {
        // Dynamic color: generates theme from wallpaper (Android 12+ only)
        // Build.VERSION.SDK_INT >= 31 checks for Android 12 (API 31)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            // Get current activity context to access dynamic colors
            val context = LocalContext.current
            // Choose dynamic scheme based on dark/light preference
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        // Static dark theme: use predefined dark colors
        darkTheme -> DarkColorScheme
        // Static light theme: use predefined light colors
        else -> LightColorScheme
    }

    // MaterialTheme: Compose component that provides theme values to all children
    // via CompositionLocal - children can access colors, typography, shapes
    MaterialTheme(
        colorScheme = colorScheme,      // Color palette for this theme
        typography = Typography,         // Text styles (headline, body, etc.)
        content = content                // Child composables to apply theme to
    )
}
