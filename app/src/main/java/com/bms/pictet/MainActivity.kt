package com.bms.pictet

// Android OS bundle for saving/restoring instance state
import android.os.Bundle
// Base class for all activities using Compose
import androidx.activity.ComponentActivity
// Extension function to set Compose content in an Activity
import androidx.activity.compose.setContent
// Extension to enable edge-to-edge display (draws behind system bars)
import androidx.activity.enableEdgeToEdge
// Modifier for UI element configuration (size, padding, etc.)
import androidx.compose.foundation.layout.fillMaxSize
// Modifier for adding padding to UI elements
import androidx.compose.foundation.layout.padding
// Material Design 3 theme components
import androidx.compose.material3.MaterialTheme
// Scaffold provides basic material design layout structure (app bar, content, FAB)
import androidx.compose.material3.Scaffold
// Surface is a container that draws background color and handles elevation
import androidx.compose.material3.Surface
// Text composable for displaying text
import androidx.compose.material3.Text
// Marks a function as a Composable (UI component function)
import androidx.compose.runtime.Composable
// Modifier type for chaining layout/behavior modifications
import androidx.compose.ui.Modifier
// Preview annotation to show component in Android Studio preview panel
import androidx.compose.ui.tooling.preview.Preview
// App theme import
import com.bms.pictet.presentation.theme.PictetTheme

// MainActivity: Entry point of the Android application
// Extends ComponentActivity to support Compose content
class MainActivity : ComponentActivity() {

    // onCreate: Called when activity is first created
    // savedInstanceState: Bundle containing previous state if activity was recreated
    override fun onCreate(savedInstanceState: Bundle?) {
        // Call parent implementation first (required)
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display: content extends behind status and navigation bars
        enableEdgeToEdge()

        // setContent: Sets the Compose UI content for this activity
        setContent {
            // PictetTheme: Custom theme wrapper that applies app's colors/typography
            PictetTheme {
                // Scaffold: Provides standard material layout structure
                // modifier.fillMaxSize(): Makes scaffold fill entire screen
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    // innerPadding: Provided by Scaffold, represents safe area insets
                    // (accounts for system bars, keyboard, etc.)
                    Greeting(
                        name = "Pictet Interview",
                        // Apply scaffold's padding to avoid content being hidden
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// @Composable: Marks this function as a UI component
// Composable functions can only be called from other composable functions
@Composable
// Greeting: Simple composable that displays a greeting message
// name: Text to display after "Hello"
// modifier: Optional layout modifications (defaults to no modifier)
fun Greeting(name: String, modifier: Modifier = Modifier) {
    // Surface: Container that handles:
    // - Background color (from theme)
    // - Elevation (shadows)
    // - Shape (corners)
    Surface(
        modifier = modifier,           // Apply passed layout modifications
        color = MaterialTheme.colorScheme.background  // Use theme background color
    ) {
        // Text: Displays text on screen
        Text(
            text = "Hello $name!",     // String to display with name interpolation
            style = MaterialTheme.typography.headlineMedium,  // Use theme headline style
            color = MaterialTheme.colorScheme.primary         // Use theme primary color
        )
    }
}

// @Preview: Shows this composable in Android Studio's design preview
// showBackground = true: Adds background color to preview (easier to see component)
@Preview(showBackground = true)
// Preview composable: must match the actual composable signature
@Composable
fun GreetingPreview() {
    // Wrap in theme to see actual colors
    PictetTheme {
        // Call Greeting with sample data for preview
        Greeting("Android")
    }
}
