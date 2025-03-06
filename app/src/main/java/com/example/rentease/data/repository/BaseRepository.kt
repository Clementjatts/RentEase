package com.example.rentease.data.repository

import com.example.rentease.data.model.ApiResponse
import retrofit2.Response
import java.io.IOException

abstract class BaseRepository {
    protected suspend fun <T> safeApiCall(apiCall: suspend () -> Response<ApiResponse>): Result<T> {
        try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "success") {
                    @Suppress("UNCHECKED_CAST")
                    return Result.success(body.data as T)
                }
                return Result.failure(Exception(body?.message ?: "Unknown error occurred"))
            }
            return Result.failure(handleApiError(response))
        } catch (e: IOException) {
            return Result.failure(Exception("Network error occurred", e))
        } catch (e: Exception) {
            return Result.failure(handleException(e))
        }
    }

    protected fun <T> handleApiError(response: Response<T>): Exception {
        val errorBody = response.errorBody()?.string()
        return when (response.code()) {
            400 -> Exception("Bad request: $errorBody")
            401 -> Exception("Unauthorized: $errorBody")
            403 -> Exception("Forbidden: $errorBody")
            404 -> Exception("Not found: $errorBody")
            500 -> Exception("Server error: $errorBody")
            else -> Exception("API error ${response.code()}: $errorBody")
        }
    }

    protected fun handleException(e: Exception): Exception {
        return when (e) {
            is IOException -> Exception("Network error occurred", e)
            else -> Exception("An error occurred: ${e.message}", e)
        }
    }
}
