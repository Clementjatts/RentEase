package com.example.rentease.data.repository

import com.example.rentease.data.api.RentEaseApi
import com.example.rentease.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Repository for handling user profile operations and landlord management
class UserRepository(
    private val api: RentEaseApi,
    private val authManager: com.example.rentease.auth.AuthManager
) : BaseRepository() {

    // Retrieves user profile data for current user or specific landlord
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
                            // Create a user object from the user data (consistent field names)
                            return@withContext com.example.rentease.data.model.Result.Success(
                                User(
                                    id = landlordId,
                                    username = dataMap["username"] as? String ?: "",
                                    email = dataMap["email"] as? String ?: "",
                                    userType = "LANDLORD",
                                    fullName = dataMap["full_name"] as? String ?: "",
                                    phone = dataMap["phone"] as? String ?: "",
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
                        // If we got a guest user but we have authentication data, it means the backend authentication failed
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

    // Updates user profile information for current user or specific landlord
    suspend fun updateUserProfile(user: User, landlordId: Int? = null): com.example.rentease.data.model.Result<User> = withContext(Dispatchers.IO) {
        try {
            // If landlordId is provided, we're updating a landlord profile
            if (landlordId != null && landlordId > 0) {
                // When admin updates landlord profile, we need the landlord's username, not admin's
                // Get the current landlord data first to get the correct username
                val landlordResponse = api.getLandlord(landlordId)
                if (!landlordResponse.isSuccessful) {
                    return@withContext com.example.rentease.data.model.Result.Error("Failed to get landlord data: ${landlordResponse.message()}")
                }

                val landlordApiResponse = landlordResponse.body()
                if (landlordApiResponse == null || !landlordApiResponse.success) {
                    return@withContext com.example.rentease.data.model.Result.Error("Failed to get landlord data")
                }

                val landlordData = landlordApiResponse.data as? Map<*, *>
                val landlordUsername = landlordData?.get("username") as? String

                if (landlordUsername.isNullOrEmpty()) {
                    return@withContext com.example.rentease.data.model.Result.Error("Could not get landlord username")
                }

                // Update landlord data via the user API endpoint with consistent field names
                // Use the landlord's username, not the admin's username
                val updateData = mapOf(
                    "username" to landlordUsername,
                    "full_name" to (user.fullName ?: ""),
                    "phone" to (user.phone ?: ""),
                    "email" to (user.email ?: "")
                )

                val response = api.updateLandlord(landlordId, updateData)
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success) {
                        // Return the updated user object
                        return@withContext com.example.rentease.data.model.Result.Success(user)
                    } else {
                        val errorMsg = apiResponse?.message ?: "Unknown error"
                        return@withContext com.example.rentease.data.model.Result.Error("Failed to update landlord profile: $errorMsg")
                    }
                } else {
                    return@withContext com.example.rentease.data.model.Result.Error("Failed to update landlord profile: ${response.message()}")
                }
            } else {
                // Update current user profile via user API endpoint
                val username = if (user.username.isNotEmpty()) {
                    user.username
                } else {
                    authManager.username ?: ""
                }

                // Use Map format to match API expectations and include required fields
                val updateData = mapOf(
                    "username" to username,
                    "full_name" to (user.fullName ?: ""),
                    "phone" to (user.phone ?: ""),
                    "email" to (user.email ?: "")
                )
                val response = api.updateUser(user.id, updateData)
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

    // Saves user data to local storage (placeholder implementation)
    suspend fun saveUser(): com.example.rentease.data.model.Result<Unit> = withContext(Dispatchers.IO) {
        try {
            com.example.rentease.data.model.Result.Success(Unit)
        } catch (e: Exception) {
            com.example.rentease.data.model.Result.Error(handleException(e).message)
        }
    }

    // Gets the landlord ID for the current user when they want to edit their profile
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
            }

            // If we couldn't find a matching landlord, return an error
            return@withContext com.example.rentease.data.model.Result.Error("Could not find landlord profile for current user")
        } catch (e: Exception) {
            return@withContext com.example.rentease.data.model.Result.Error("Error getting landlord ID: ${e.message}")
        }
    }

    // Retrieves all landlord users for admin management
    suspend fun getLandlords(): com.example.rentease.data.model.Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getLandlords()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null && apiResponse.success) {
                    // The data structure is nested: {"data":{"landlords":[...]}}
                    val dataMap = apiResponse.data as? Map<*, *>
                    val landlordsList = dataMap?.get("landlords") as? List<*>

                    if (landlordsList != null) {
                        val users = landlordsList.mapNotNull { userMap ->
                            try {
                                if (userMap is Map<*, *>) {
                                    User(
                                        id = (userMap["id"] as? Double)?.toInt() ?: 0,
                                        username = (userMap["username"] as? String) ?: "",
                                        email = (userMap["email"] as? String) ?: "",
                                        userType = "LANDLORD", // All users from this endpoint are landlords
                                        fullName = (userMap["full_name"] as? String) ?: "", // Consistent field name
                                        phone = (userMap["phone"] as? String) ?: "", // Consistent field name
                                        createdAt = (userMap["created_at"] as? String) ?: ""
                                    )
                                } else null
                            } catch (e: Exception) {
                                null
                            }
                        }
                        return@withContext com.example.rentease.data.model.Result.Success(users)
                    }
                }
            }

            return@withContext com.example.rentease.data.model.Result.Error("Failed to load landlords")
        } catch (e: Exception) {
            return@withContext com.example.rentease.data.model.Result.Error("Error loading landlords: ${e.message}")
        }
    }

    // Deletes a landlord account by ID
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
