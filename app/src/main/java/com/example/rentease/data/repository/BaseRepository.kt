package com.example.rentease.data.repository

import retrofit2.Response

abstract class BaseRepository {

    protected fun <T> handleApiError(response: Response<T>): Exception {
        return Exception("Request failed: ${response.message()}")
    }

    protected fun handleException(e: Exception): Exception {
        return Exception("An error occurred: ${e.message}")
    }
}
