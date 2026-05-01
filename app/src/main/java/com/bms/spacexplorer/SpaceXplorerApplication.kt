package com.bms.spacexplorer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class with Hilt DI setup.
 *
 * @HiltAndroidApp triggers Hilt's code generation:
 * - Application-level component
 * - Singleton bindings
 * - Automatic injection into Android framework classes
 */
@HiltAndroidApp
class SpaceXplorerApplication : Application()
