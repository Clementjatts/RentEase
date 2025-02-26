package com.example.rentease

import android.app.Application
import android.util.Log
import androidx.room.Room
import com.example.rentease.auth.AuthManager
import com.example.rentease.data.local.RentEaseDatabase

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
            
            // Initialize database
            try {
                // Initialize database without assigning to an unused variable
                RentEaseDatabase.getDatabase(this)
                Log.d(TAG, "Database initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing database", e)
            }
            
            Log.d(TAG, "Application initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing application", e)
        }
    }
    
    companion object {
        private const val TAG = "RentEaseApp"
    }
}
