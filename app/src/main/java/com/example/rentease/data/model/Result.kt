package com.example.rentease.data.model

/**
 * A generic class that holds a value or an error message.
 * @param <T> Type of the value
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val errorMessage: String?) : Result<Nothing>()
    
    val isSuccess: Boolean
        get() = this is Success
}
