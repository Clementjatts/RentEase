package com.example.rentease.data.repository

import com.example.rentease.data.api.RentEaseApi
import com.example.rentease.data.model.CreateRequestData
import com.example.rentease.data.model.Request
import com.example.rentease.data.model.Result

// Repository for handling property inquiry requests and notifications
class RequestRepository(private val api: RentEaseApi) {

    // Submits a new contact request for a property
    suspend fun submitRequest(
        propertyId: Int,
        landlordId: Int,
        requesterName: String,
        requesterEmail: String,
        requesterPhone: String?,
        message: String
    ): Result<Boolean> {
        return try {
            val requestData = CreateRequestData(
                propertyId = propertyId,
                landlordId = landlordId,
                requesterName = requesterName,
                requesterEmail = requesterEmail,
                requesterPhone = requesterPhone,
                message = message
            )

            val response = api.createRequest(requestData)
            if (response.isSuccessful) {
                Result.Success(true)
            } else {
                Result.Error("Failed to submit request")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    // Retrieves all contact requests for a specific landlord
    suspend fun getRequestsForLandlord(landlordId: Int): Result<List<Request>> {
        return try {
            val response = api.getRequestsForLandlord(landlordId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    // The backend returns the requests in body.data as a list
                    val requestsList = body.data as? List<*>
                    if (requestsList != null) {
                        val requests = mutableListOf<Request>()
                        for (item in requestsList) {
                            try {
                                if (item is Map<*, *>) {
                                    val request = convertMapToRequest(item)
                                    requests.add(request)
                                }
                            } catch (e: Exception) {
                                // Skip malformed items
                            }
                        }
                        Result.Success(requests)
                    } else {
                        Result.Success(emptyList())
                    }
                } else {
                    Result.Error(body?.message ?: "Failed to fetch requests")
                }
            } else {
                Result.Error("Failed to fetch requests")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    // Marks a specific request as read by the landlord
    suspend fun markAsRead(requestId: Int): Result<Boolean> {
        return try {
            val response = api.markRequestAsRead(requestId)
            if (response.isSuccessful) {
                Result.Success(true)
            } else {
                Result.Error("Failed to mark as read")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    // Gets the count of unread requests for a landlord
    suspend fun getUnreadCount(landlordId: Int): Result<Int> {
        return try {
            val response = api.getUnreadCount(landlordId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    val data = body.data as? Map<*, *>
                    val count = data?.get("count") as? Number
                    Result.Success(count?.toInt() ?: 0)
                } else {
                    Result.Error(body?.message ?: "Failed to fetch unread count")
                }
            } else {
                Result.Error("Failed to fetch unread count")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    // Converts a Map from API response to a Request object
    private fun convertMapToRequest(map: Map<*, *>): Request {
        return Request(
            id = (map["id"] as? Number)?.toInt() ?: 0,
            propertyId = (map["property_id"] as? Number)?.toInt() ?: 0,
            landlordId = (map["landlord_id"] as? Number)?.toInt() ?: 0,
            requesterName = map["requester_name"] as? String ?: "",
            requesterEmail = map["requester_email"] as? String ?: "",
            requesterPhone = map["requester_phone"] as? String,
            message = map["message"] as? String ?: "",
            isRead = (map["is_read"] as? Number)?.toInt() == 1,
            createdAt = map["created_at"] as? String ?: "",
            propertyTitle = map["property_title"] as? String ?: "",
            propertyAddress = map["property_address"] as? String ?: ""
        )
    }
}
