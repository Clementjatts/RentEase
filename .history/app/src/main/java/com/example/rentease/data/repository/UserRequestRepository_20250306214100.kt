package com.example.rentease.data.repository

import android.content.Context
import com.example.rentease.data.api.RentEaseApi
import com.example.rentease.data.model.ApiResponse
import com.example.rentease.data.model.UserRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRequestRepository(
    private val api: RentEaseApi,
    private val context: Context
) : BaseRepository() {

    // No local database access

    suspend fun createRequest(request: UserRequest): Result<UserRequest> = withContext(Dispatchers.IO) {
        try {
            // Send to backend
            val response = api.createRequest(request)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.data != null) {
                    // If server returns updated data
                    @Suppress("UNCHECKED_CAST")
                    val serverRequest = apiResponse.data as UserRequest
                    Result.success(serverRequest)
                } else {
                    Result.success(request)
                }
            } else {
                Result.failure(handleApiError(response))
            }
        } catch (e: Exception) {
            Result.failure(handleException(e))
        }
    }

    suspend fun getUserRequests(userId: String): Result<List<UserRequest>> = withContext(Dispatchers.IO) {
        try {
            // Get from API
            val response = api.getUserRequests(userId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null) {
                    // Parse data and return
                    val requests = apiResponse.data as List<*>
                    if (requests.isNotEmpty()) {
                        // Process the list (simplified for now)
                        val processedRequests = requests.mapNotNull { it as? Map<*, *> }
                            .map { mapToUserRequest(it) }
                        Result.success(processedRequests)
                    } else {
                        Result.success(emptyList())
                    }
                } else {
                    Result.success(emptyList())
                }
            } else {
                Result.failure(handleApiError(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapToUserRequest(map: Map<*, *>): UserRequest {
        // Map the API response to a UserRequest object
        // This implementation depends on your API response format
        return UserRequest(
            id = (map["id"] as? Number)?.toInt() ?: 0,
            userId = map["userId"] as? String ?: "",
            propertyId = (map["propertyId"] as? Number)?.toInt() ?: 0,
            message = map["message"] as? String ?: "",
            createdAt = map["createdAt"] as? String ?: ""
        )
    }
}
