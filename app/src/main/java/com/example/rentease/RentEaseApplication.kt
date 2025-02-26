package com.example.rentease

import android.app.Application
import android.util.Log

class RentEaseApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Initialize any required components here
            Log.d(TAG, "Application initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing application", e)
        }
    }
    
    companion object {
        private const val TAG = "RentEaseApp"
    }
}
