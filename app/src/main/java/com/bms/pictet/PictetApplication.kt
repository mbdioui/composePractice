package com.bms.pictet

// HiltAndroidApp: Marks this Application class as the entry point for Hilt
// Hilt generates the necessary components based on this annotation
// Must be placed on the custom Application class
import dagger.hilt.android.HiltAndroidApp
// Application: Base class for maintaining global application state
import android.app.Application

/**
 * PictetApplication: Custom Application class for the Pictet interview app.
 *
 * @HiltAndroidApp does several things:
 * 1. Triggers Hilt's code generation to create the application-level component
 * 2. Creates a base Application class that serves as the dependency container
 * 3. Makes the application context available for dependency injection
 *
 * This is REQUIRED for Hilt to work - without it, no dependencies can be injected.
 *
 * The generated component will be named: DaggerPictetApplication_HiltComponents
 * This component lives for the entire lifecycle of the application.
 */
@HiltAndroidApp
class PictetApplication : Application() {
    // Override methods here if needed for app-level initialization
    // Common use cases:
    // - Initialize logging libraries (Timber)
    // - Setup crash reporting
    // - Initialize analytics
    // - Configure image loading libraries (Coil)

    override fun onCreate() {
        super.onCreate()
        // Application-wide initialization goes here
        // This runs before any Activity, Service, or Receiver is created
    }
}
