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

    private val userRequestDao = RentEaseDatabase.getDatabase(context).userRequestDao()

    suspend fun createRequest(request: UserRequest): Result<UserRequest> = withContext(Dispatchers.IO) {
        try {
            // Save to local database first
            val localId = userRequestDao.insert(request)
            val localRequest = request.copy(id = localId.toInt())
            
            // Then send to backend
            val response = api.createRequest(request)
            if (response.isSuccessful) {
                // Update local record with server ID if needed
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.data != null) {
                    // If server returns updated data, update local database
                    userRequestDao.update(localRequest)
                }
                Result.success(localRequest)
            } else {
                // Keep the local record but return error from API
                Result.failure(handleApiError(response))
            }
        } catch (e: Exception) {
            // If network call fails, we still have the local record
            Result.failure(handleException(e))
        }
    }

    suspend fun getUserRequests(userId: String): Result<List<UserRequest>> = withContext(Dispatchers.IO) {
        try {
            // Try to get from API first
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
                        // Fallback to local database
                        Result.success(userRequestDao.getUserRequests(userId))
                    }
                } else {
                    // Fallback to local database
                    Result.success(userRequestDao.getUserRequests(userId))
                }
            } else {
                // Fallback to local database
                Result.success(userRequestDao.getUserRequests(userId))
            }
        } catch (e: Exception) {
            // Fallback to local database on network error
            try {
                Result.success(userRequestDao.getUserRequests(userId))
            } catch (dbError: Exception) {
                Result.failure(dbError)
            }
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
