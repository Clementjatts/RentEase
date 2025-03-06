package com.example.rentease

import android.app.Application
import android.util.Log
import com.example.rentease.auth.AuthManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class RentEaseApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception in thread ${thread.name}", throwable)
        }
        
        try {
            Log.d(TAG, "Application initialization starting...")
            
            // Initialize AuthManager
            try {
                val authManager = AuthManager.getInstance(this)
                Log.d(TAG, "AuthManager initialized successfully: isLoggedIn=${authManager.isLoggedIn}")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing AuthManager", e)
            }
                        
            Log.d(TAG, "Application initialization completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing application", e)
        }
    }
    
    companion object {
        private const val TAG = "RentEaseApp"
    }
}
