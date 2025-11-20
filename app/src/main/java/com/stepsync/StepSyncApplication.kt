package com.stepsync

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for StepSync
 * Initializes Hilt dependency injection
 */
@HiltAndroidApp
class StepSyncApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
