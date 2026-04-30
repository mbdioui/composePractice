package com.bms.pictet.presentation.theme

// Import Color class from Compose UI graphics package
// Color: Immutable class representing a color with alpha channel
// Format: Color(red, green, blue, alpha) where each value is 0.0-1.0
// Or: Color(0xFFRRGGBB) hex format without alpha
// Or: Color(0xAARRGGBB) hex format with alpha (AA)
import androidx.compose.ui.graphics.Color

// ============================================================================
// LIGHT THEME COLORS
// ============================================================================
// These colors are designed for use on light backgrounds (white/very light gray)
// Contrast ratios meet Material Design accessibility standards

// Primary: Main brand color - used for buttons, active states, key actions
// Blue shade for professional finance/banking feel
val md_theme_light_primary = Color(0xFF005FAE)              // Primary brand blue

// onPrimary: Text/icons displayed ON primary color (must have contrast)
// White ensures high contrast on blue background
val md_theme_light_onPrimary = Color(0xFFFFFFFF)            // White text on blue

// primaryContainer: Container background using primary color at lower emphasis
// Lighter blue for cards, chips, tonal buttons
val md_theme_light_primaryContainer = Color(0xFFD4E3FF)     // Light blue container

// onPrimaryContainer: Text/icons on primaryContainer
val md_theme_light_onPrimaryContainer = Color(0xFF001C3B) // Dark blue text

// Secondary: Accent color - less prominent than primary, for differentiation
// Teal/cyan for secondary actions, toggles, navigation
val md_theme_light_secondary = Color(0xFF006397)            // Teal accent
val md_theme_light_onSecondary = Color(0xFFFFFFFF)         // White on secondary
val md_theme_light_secondaryContainer = Color(0xFFCCE5FF)  // Light teal container
val md_theme_light_onSecondaryContainer = Color(0xFF001D31) // Dark text on container

// Tertiary: Additional accent for contrasting elements
// Purple/violet for unique accents that need to stand out
val md_theme_light_tertiary = Color(0xFF6B5778)            // Muted purple
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFF2DAFF)   // Light purple container
val md_theme_light_onTertiaryContainer = Color(0xFF251431)

// Error: Color for error states, validation failures, destructive actions
// Standard Material red for errors
val md_theme_light_error = Color(0xFFBA1A1A)               // Standard error red
val md_theme_light_errorContainer = Color(0xFFFFDAD6)       // Light red background
val md_theme_light_onError = Color(0xFFFFFFFF)            // White on error
val md_theme_light_onErrorContainer = Color(0xFF410002)   // Dark text on error bg

// Background: Main app background color
// Very light gray, easier on eyes than pure white
val md_theme_light_background = Color(0xFFFDFCFF)          // Off-white background
val md_theme_light_onBackground = Color(0xFF1A1C1E)       // Dark text on background

// Surface: Cards, sheets, menus background - slightly elevated from background
// Same as background in this theme (flat design), could be different
val md_theme_light_surface = Color(0xFFFDFCFF)            // Surface (cards, etc.)
val md_theme_light_onSurface = Color(0xFF1A1C1E)          // Primary text color
val md_theme_light_surfaceVariant = Color(0xFFE0E2EC)     // Alternative surface
val md_theme_light_onSurfaceVariant = Color(0xFF43474E)  // Secondary text color

// Outline: Border colors for outlined components (buttons, text fields)
// Medium gray for subtle borders
val md_theme_light_outline = Color(0xFF74777F)            // Border/outline color
val md_theme_light_outlineVariant = Color(0xFFC4C6D0)     // Subtle dividers

// Inverse: Colors for elements on colored backgrounds that contrast with main surface
// Used for snackbars, dialogs that appear over content
val md_theme_light_inverseSurface = Color(0xFF2F3033)       // Dark surface (inverse)
val md_theme_light_inverseOnSurface = Color(0xFFF1F0F4)     // Light text on inverse
val md_theme_light_inversePrimary = Color(0xFFA1C9FF)       // Light primary (inverse)

// scrim: Semi-transparent overlay behind dialogs/bottom sheets
// Black with 32% opacity for dimming background
val md_theme_light_scrim = Color(0x52000000)              // 32% black overlay

// surfaceTint: Color overlay applied to surfaces at different elevations
// Creates subtle depth effect on elevated cards/sheets
val md_theme_light_surfaceTint = Color(0xFF005FAE)        // Primary tint for surfaces

// ============================================================================
// DARK THEME COLORS
// ============================================================================
// These colors are designed for use on dark backgrounds (dark gray/black)
// Same semantic meanings as light theme, but values adapted for dark mode

// Primary: Same blue, but at higher brightness for visibility
val md_theme_dark_primary = Color(0xFFA1C9FF)              // Lighter blue for dark bg
val md_theme_dark_onPrimary = Color(0xFF00315C)             // Dark text on light blue
val md_theme_dark_primaryContainer = Color(0xFF004785)      // Darker blue container
val md_theme_dark_onPrimaryContainer = Color(0xFFD4E3FF)    // Light text on container

// Secondary: Lighter teal for dark backgrounds
val md_theme_dark_secondary = Color(0xFF99CCFF)            // Light teal
val md_theme_dark_onSecondary = Color(0xFF003351)          // Dark text on secondary
val md_theme_dark_secondaryContainer = Color(0xFF004B73)   // Dark teal container
val md_theme_dark_onSecondaryContainer = Color(0xFFCCE5FF) // Light text

// Tertiary: Lighter purple for dark mode
val md_theme_dark_tertiary = Color(0xFFD6BBE4)            // Light purple
val md_theme_dark_onTertiary = Color(0xFF3B2948)           // Dark text
val md_theme_dark_tertiaryContainer = Color(0xFF52405F)    // Dark purple container
val md_theme_dark_onTertiaryContainer = Color(0xFFF2DAFF)  // Light text

// Error: Lighter red for dark backgrounds (better visibility)
val md_theme_dark_error = Color(0xFFFFB4AB)                // Light red
val md_theme_dark_errorContainer = Color(0xFF93000A)       // Dark red container
val md_theme_dark_onError = Color(0xFF690005)              // Dark text on error
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)     // Light text on error

// Background: Dark background color
val md_theme_dark_background = Color(0xFF1A1C1E)           // Dark gray (not pure black)
val md_theme_dark_onBackground = Color(0xFFE3E2E6)         // Light text on dark bg

// Surface: Slightly lighter than background for elevation
// Material elevation = lighter color in dark mode (inverted from light theme)
val md_theme_dark_surface = Color(0xFF1A1C1E)             // Surface (same as bg here)
val md_theme_dark_onSurface = Color(0xFFE3E2E6)          // Primary text color
val md_theme_dark_surfaceVariant = Color(0xFF43474E)      // Elevated surface
val md_theme_dark_onSurfaceVariant = Color(0xFFC4C6D0)    // Secondary text color

// Outline: Lighter gray for dark theme borders
val md_theme_dark_outline = Color(0xFF8E9099)            // Border color (dark)
val md_theme_dark_outlineVariant = Color(0xFF43474E)       // Subtle dividers

// Inverse: Opposite of main theme (used for elevated surfaces in dark)
val md_theme_dark_inverseSurface = Color(0xFFE3E2E6)       // Light surface (inverse)
val md_theme_dark_inverseOnSurface = Color(0xFF1A1C1E)   // Dark text on inverse
val md_theme_dark_inversePrimary = Color(0xFF005FAE)       // Primary color (inverse)

// scrim: Dark overlay for modals in dark mode (more opaque than light)
val md_theme_dark_scrim = Color(0xFF000000)              // Black overlay

// surfaceTint: Color tint for elevated surfaces in dark theme
val md_theme_dark_surfaceTint = Color(0xFFA1C9FF)        // Light primary tint

// ============================================================================
// SEED COLORS (for reference - used by Material Theme Builder)
// ============================================================================
// These are the base colors from which the full palette is generated
// Used when creating dynamic themes or using Material Theme Builder
val seed = Color(0xFF005FAE)  // Primary seed color
