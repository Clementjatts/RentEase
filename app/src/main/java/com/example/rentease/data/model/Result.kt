package com.example.rentease.data.model

// Generic sealed class that holds either a successful value or an error message
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val errorMessage: String?) : Result<Nothing>()
    
    val isSuccess: Boolean
        get() = this is Success
}
