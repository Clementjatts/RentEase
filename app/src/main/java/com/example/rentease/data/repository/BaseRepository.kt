package com.example.rentease.data.repository

import retrofit2.Response

// Base repository class providing common error handling functionality
abstract class BaseRepository {

    // Handles API response errors and returns a formatted exception
    protected fun <T> handleApiError(response: Response<T>): Exception {
        return Exception("Request failed: ${response.message()}")
    }

    // Handles general exceptions and returns a formatted exception
    protected fun handleException(e: Exception): Exception {
        return Exception("An error occurred: ${e.message}")
    }
}
