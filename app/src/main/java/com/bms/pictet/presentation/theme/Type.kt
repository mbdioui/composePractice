package com.bms.pictet.presentation.theme

// Material 3 Typography class - defines text styles for the app
import androidx.compose.material3.Typography
// TextStyle: Defines text appearance (font, size, line height, etc.)
// Note: This is from ui.text package, NOT material3 package
import androidx.compose.ui.text.TextStyle
// Text unit class for font sizes (sp = scalable pixels)
import androidx.compose.ui.unit.sp

// Typography: Defines text styles used throughout the app
// Material 3 provides 15 text styles organized by size/usage:
// - displayLarge/medium/small: Largest text, headlines
// - headlineLarge/medium/small: Section headers
// - titleLarge/medium/small: Card titles, app bar titles
// - bodyLarge/medium/small: Main content text
// - labelLarge/medium/small: Buttons, captions, small text
//
// Each style has default values from Material3, we override selectively
val Typography = Typography(
    // Display styles: Used for the largest text on screen
    // displayLarge: Hero text, onboarding headlines
    displayLarge = TextStyle(
        fontSize = 57.sp,          // Large size for maximum impact
        lineHeight = 64.sp,        // Space between lines (1.12x)
        letterSpacing = (-0.25).sp  // Tight tracking for headlines
    ),

    // displayMedium: Large display text, slightly smaller
    displayMedium = TextStyle(
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp        // Normal tracking
    ),

    // displaySmall: Still large but more readable
    displaySmall = TextStyle(
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // Headline styles: Section headers, page titles
    // headlineLarge: Main page titles
    headlineLarge = TextStyle(
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),

    // headlineMedium: Section headers
    headlineMedium = TextStyle(
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),

    // headlineSmall: Card headers, smaller sections
    headlineSmall = TextStyle(
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // Title styles: Component titles, app bars, lists
    // titleLarge: App bar titles, large list items
    titleLarge = TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),

    // titleMedium: Medium emphasis titles
    titleMedium = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp    // Slightly wider for medium text
    ),

    // titleSmall: Subtitles, overlines
    titleSmall = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // Body styles: Main content text, paragraphs
    // bodyLarge: Primary body text, most readable
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp     // More spacing for readability
    ),

    // bodyMedium: Secondary body text
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),

    // bodySmall: Captions, metadata
    bodySmall = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Label styles: Buttons, chips, badges
    // labelLarge: Large buttons, important actions
    labelLarge = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // labelMedium: Standard buttons
    labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),

    // labelSmall: Badges, timestamps
    labelSmall = TextStyle(
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
