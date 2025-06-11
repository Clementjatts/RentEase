package com.example.rentease

import android.app.Application
import android.util.Log
import com.example.rentease.auth.AuthManager

// Application class that handles global app initialization and configuration
class RentEaseApplication : Application() {

    companion object {
        private const val TAG = "RentEaseApp"
    }

    // Initializes the application with global exception handling and AuthManager setup
    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception in thread ${thread.name}", throwable)
        }

        try {
            // Initialize AuthManager
            AuthManager.getInstance(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AuthManager", e)
        }
    }
}
