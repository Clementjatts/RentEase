package com.example.rentease.data.repository

import com.example.rentease.data.api.RentEaseApi
import com.example.rentease.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    private val api: RentEaseApi,
    private val authManager: com.example.rentease.auth.AuthManager
) : BaseRepository() {


    suspend fun getUserProfile(landlordId: Int? = null): com.example.rentease.data.model.Result<User> = withContext(Dispatchers.IO) {
        try {
            // If landlordId is provided, we're editing a landlord profile
            // This works for both admin editing a landlord and a landlord editing their own profile
            if (landlordId != null && landlordId > 0) {
                // Get the landlord details
                val landlordResponse = api.getLandlord(landlordId)
                if (landlordResponse.isSuccessful) {
                    val apiResponse = landlordResponse.body()
                    if (apiResponse != null && apiResponse.success) {
                        val dataMap = apiResponse.data as? Map<*, *>
                        if (dataMap != null) {
                            // Create a user object from the landlord data
                            return@withContext com.example.rentease.data.model.Result.Success(
                                User(
                                    id = landlordId,
                                    username = dataMap["name"] as? String ?: "",
                                    email = dataMap["email"] as? String ?: "",
                                    userType = "LANDLORD",
                                    fullName = dataMap["name"] as? String ?: "",
                                    phone = dataMap["contact"] as? String ?: "",
                                    createdAt = dataMap["created_at"] as? String ?: ""
                                )
                            )
                        }
                    }
                }
            }

            // Get the user ID and type from AuthManager
            val userId = authManager.getUserId().toIntOrNull() ?: 0
            val userType = authManager.userType

            if (userId <= 0) {
                return@withContext com.example.rentease.data.model.Result.Error("User not logged in")
            }

            // Try to fetch from API
            val response = api.getCurrentUser()

            if (response.isSuccessful) {
                val user = response.body()

                if (user != null) {
                    // Check if we got a guest user (id = 0)
                    if (user.id <= 0 || user.userType == "GUEST") {
                        // If we got a guest user but we have authentication data,
                        // it means the backend authentication failed
                        // Let's try to get the landlord data directly
                        if (userType == com.example.rentease.auth.UserType.LANDLORD) {
                            val landlordResult = getLandlordIdForCurrentUser()
                            if (landlordResult is com.example.rentease.data.model.Result.Success && landlordResult.data != null) {
                                return@withContext getUserProfile(landlordResult.data)
                            }
                        }

                        // Fallback: Create a user object with the data from AuthManager
                        val createdUser = User(
                            id = userId,
                            username = authManager.username ?: "",
                            email = "",
                            userType = userType.toString(),
                            fullName = "",
                            phone = "",
                            createdAt = null
                        )
                        return@withContext com.example.rentease.data.model.Result.Success(createdUser)
                    }

                    // We got a valid user, return it
                    return@withContext com.example.rentease.data.model.Result.Success(user)
                }
            }

            // If API call failed, create a user object with the data from AuthManager
            val fallbackUser = User(
                id = userId,
                username = authManager.username ?: "",
                email = "",
                userType = userType.toString(),
                fullName = "",
                phone = "",
                createdAt = null
            )
            return@withContext com.example.rentease.data.model.Result.Success(fallbackUser)
        } catch (e: Exception) {
            return@withContext com.example.rentease.data.model.Result.Error("Error getting user profile: ${e.message}")
        }
    }

    suspend fun updateUserProfile(user: User, landlordId: Int? = null): com.example.rentease.data.model.Result<User> = withContext(Dispatchers.IO) {
        try {
            // If landlordId is provided, we're updating a landlord profile
            if (landlordId != null && landlordId > 0) {
                // Update landlord data via the landlord API endpoint
                val updateData = mapOf(
                    "name" to (user.fullName ?: ""),
                    "contact" to (user.phone ?: ""),
                    "email" to (user.email ?: "")
                )

                val response = api.updateLandlord(landlordId, updateData)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success) {
                        // Return the updated user object
                        return@withContext com.example.rentease.data.model.Result.Success(user)
                    } else {
                        return@withContext com.example.rentease.data.model.Result.Error("Failed to update landlord profile")
                    }
                } else {
                    return@withContext com.example.rentease.data.model.Result.Error("Failed to update landlord profile: ${response.message()}")
                }
            } else {
                // Update current user profile via user API endpoint
                val response = api.updateUser(user.id, user)
                if (response.isSuccessful) {
                    return@withContext com.example.rentease.data.model.Result.Success(user)
                } else {
                    return@withContext com.example.rentease.data.model.Result.Error("Failed to update user profile: ${response.message()}")
                }
            }
        } catch (e: Exception) {
            return@withContext com.example.rentease.data.model.Result.Error("Error updating user profile: ${e.message}")
        }
    }

    suspend fun saveUser(): com.example.rentease.data.model.Result<Unit> = withContext(Dispatchers.IO) {
        try {
            com.example.rentease.data.model.Result.Success(Unit)
        } catch (e: Exception) {
            com.example.rentease.data.model.Result.Error(handleException(e).message)
        }
    }

    /**
     * Get the landlord ID for the current user
     * This is used when a landlord wants to edit their own profile
     */
    private suspend fun getLandlordIdForCurrentUser(): com.example.rentease.data.model.Result<Int?> = withContext(Dispatchers.IO) {
        try {
            // Get the user ID and type from AuthManager
            val userId = authManager.getUserId().toIntOrNull() ?: 0
            val userType = authManager.userType

            // Only proceed if the user is a landlord
            if (userType != com.example.rentease.auth.UserType.LANDLORD || userId <= 0) {
                return@withContext com.example.rentease.data.model.Result.Error("User is not a landlord")
            }

            // Use the new endpoint to get landlord by user ID
            val response = api.getLandlordByUserId()

            if (response.isSuccessful) {
                val apiResponse = response.body()

                if (apiResponse != null && apiResponse.success) {
                    val landlordData = apiResponse.data as? Map<*, *>

                    if (landlordData != null) {
                        val landlordId = (landlordData["id"] as? Double)?.toInt()

                        if (landlordId != null && landlordId > 0) {
                            return@withContext com.example.rentease.data.model.Result.Success(landlordId)
                        }
                    }
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("UserRepository", "API call failed - Code: ${response.code()}, Error: $errorBody")
            }

            // If we couldn't find a matching landlord, return an error
            android.util.Log.e("UserRepository", "Could not find landlord profile for current user")
            return@withContext com.example.rentease.data.model.Result.Error("Could not find landlord profile for current user")
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "Exception in getLandlordIdForCurrentUser", e)
            return@withContext com.example.rentease.data.model.Result.Error("Error getting landlord ID: ${e.message}")
        }
    }

    // Landlord management methods
    suspend fun getLandlords(): com.example.rentease.data.model.Result<List<com.example.rentease.data.model.Landlord>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getLandlords()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success) {
                    // The data structure is nested: {"data":{"landlords":[...]}}
                    val dataMap = apiResponse.data as? Map<*, *>
                    val landlordsList = dataMap?.get("landlords") as? List<*>

                    if (landlordsList != null) {
                        val landlords = landlordsList.mapNotNull { landlordMap ->
                            try {
                                if (landlordMap is Map<*, *>) {
                                    com.example.rentease.data.model.Landlord(
                                        id = (landlordMap["id"] as? Double)?.toInt() ?: 0,
                                        user_id = (landlordMap["user_id"] as? Double)?.toInt() ?: 0,
                                        name = (landlordMap["name"] as? String) ?: "",
                                        contact = (landlordMap["contact"] as? String) ?: "",
                                        email = (landlordMap["email"] as? String) ?: "",
                                        created_at = (landlordMap["created_at"] as? String) ?: ""
                                    )
                                } else null
                            } catch (e: Exception) {
                                null
                            }
                        }
                        return@withContext com.example.rentease.data.model.Result.Success(landlords)
                    }
                }
            }

            return@withContext com.example.rentease.data.model.Result.Error("Failed to load landlords")
        } catch (e: Exception) {
            return@withContext com.example.rentease.data.model.Result.Error("Error loading landlords: ${e.message}")
        }
    }

    suspend fun deleteLandlord(landlordId: Int): com.example.rentease.data.model.Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteLandlord(landlordId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success) {
                    return@withContext com.example.rentease.data.model.Result.Success(true)
                } else {
                    return@withContext com.example.rentease.data.model.Result.Error(apiResponse?.message ?: "Failed to delete landlord")
                }
            } else {
                return@withContext com.example.rentease.data.model.Result.Error("Failed to delete landlord: ${response.message()}")
            }
        } catch (e: Exception) {
            return@withContext com.example.rentease.data.model.Result.Error("Error deleting landlord: ${e.message}")
        }
    }
}
