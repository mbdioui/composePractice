package com.bms.pictet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bms.pictet.presentation.navigation.AppNavGraph
import com.bms.pictet.presentation.theme.SpaceXplorerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpaceXplorerTheme {
                AppNavGraph()
            }
        }
    }
}