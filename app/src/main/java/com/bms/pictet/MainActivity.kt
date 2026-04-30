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
// Material Design 3 Surface container
import androidx.compose.material3.Surface
// Marks a function as a Composable (UI component function)
import androidx.compose.runtime.Composable
// Modifier type for chaining layout/behavior modifications
import androidx.compose.ui.Modifier
// Preview annotation to show component in Android Studio preview panel
import androidx.compose.ui.tooling.preview.Preview
// HiltAndroidEntryPoint: Marks Activity for dependency injection
// Required for Hilt to inject dependencies into Android components
import dagger.hilt.android.AndroidEntryPoint
// App theme import
import com.bms.pictet.presentation.screens.PostsScreen
import com.bms.pictet.presentation.theme.PictetTheme

/**
 * MainActivity: Entry point of the Android application.
 *
 * @AndroidEntryPoint: Required by Hilt for dependency injection
 * Hilt will generate necessary components for this Activity.
 * Without this annotation, ViewModels cannot be injected.
 *
 * Activity responsibilities:
 * - Sets up edge-to-edge display
 * - Sets Compose content
 * - Applies theme wrapper
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * onCreate: Called when activity is first created.
     * @param savedInstanceState Bundle containing previous state if activity was recreated
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display: content extends behind system bars
        // Modern Android design pattern for immersive UI
        enableEdgeToEdge()

        // setContent: Sets the Compose UI content for this activity
        setContent {
            // PictetTheme: Custom theme wrapper that applies app's colors/typography
            PictetTheme {
                // Surface: Container that applies theme background
                Surface(modifier = Modifier.fillMaxSize()) {
                    // PostsScreen: Main screen with real API integration
                    // Demonstrates Retrofit + MVVM + Flow + Hilt
                    PostsScreen()
                }
            }
        }
    }
}

/**
 * Preview for Android Studio design view.
 * Shows how the app looks without running it on device.
 */
@Preview(showBackground = true)
@Composable
fun MainPreview() {
    PictetTheme {
        PostsScreen()
    }
}
