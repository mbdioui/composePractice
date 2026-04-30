package com.bms.pictet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.bms.pictet.presentation.navigation.NavGraph
import com.bms.pictet.presentation.theme.PictetTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity: Entry point of the Android application.
 *
 * Navigation:
 * - Sets up Navigation Component with NavHost
 * - Uses NavGraph to define navigation destinations
 * - NavController manages back stack and navigation state
 *
 * @AndroidEntryPoint: Required by Hilt for dependency injection
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * onCreate: Called when activity is first created.
     * Sets up the navigation graph with Compose Navigation.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            PictetTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Navigation setup
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}

/**
 * Preview for Android Studio design view.
 * Shows the navigation graph starting point.
 */
@Preview(showBackground = true)
@Composable
fun MainPreview() {
    PictetTheme {
        val navController = rememberNavController()
        NavGraph(navController = navController)
    }
}
