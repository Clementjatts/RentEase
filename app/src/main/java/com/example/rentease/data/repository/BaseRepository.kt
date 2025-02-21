package com.example.rentease.data.repository

import com.example.rentease.data.api.ApiResponse
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
            return Result.failure(Exception("API call failed with code: ${response.code()}"))
        } catch (e: IOException) {
            return Result.failure(Exception("Network error occurred", e))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
