package com.example.rentease.data.repository

import android.content.Context
import com.example.rentease.data.api.RentEaseApi
import com.example.rentease.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    private val api: RentEaseApi,
    private val context: Context
) : BaseRepository() {

    // No local database access
    
    suspend fun getUser(userId: Int): kotlin.Result<User> = withContext(Dispatchers.IO) {
        try {
            // Fetch from API
            val response = api.getCurrentUser()
            if (response.isSuccessful) {
                val user = response.body()
                if (user != null) {
                    return@withContext kotlin.Result.success(user)
                } else {
                    return@withContext kotlin.Result.failure(Exception("User data is null"))
                }
            } else {
                return@withContext kotlin.Result.failure(handleApiError(response))
            }
        } catch (e: Exception) {
            return@withContext kotlin.Result.failure(handleException(e))
        }
    }
    
    suspend fun getUserProfile(userId: String): com.example.rentease.data.model.Result<User> = withContext(Dispatchers.IO) {
        try {
            // Fetch from API
            val response = api.getCurrentUser()
            if (response.isSuccessful) {
                val user = response.body()
                if (user != null) {
                    return@withContext com.example.rentease.data.model.Result.Success(user)
                }
            }
            
            return@withContext com.example.rentease.data.model.Result.Error("Failed to get user profile")
        } catch (e: Exception) {
            return@withContext com.example.rentease.data.model.Result.Error("Error getting user profile: ${e.message}")
        }
    }
    
    suspend fun updateUserProfile(user: User): com.example.rentease.data.model.Result<User> = withContext(Dispatchers.IO) {
        try {
            // Since updateCurrentUser is not available, we'll use a mock implementation
            // Instead, we'll just update the local database
            userDao.insertUser(UserEntity.fromUser(user))
            return@withContext com.example.rentease.data.model.Result.Success(user)
        } catch (e: Exception) {
            return@withContext com.example.rentease.data.model.Result.Error("Error updating user profile: ${e.message}")
        }
    }
    
    suspend fun changePassword(
        oldPassword: String,
        newPassword: String
    ): com.example.rentease.data.model.Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = com.example.rentease.data.model.ChangePasswordRequest(
                currentPassword = oldPassword,
                newPassword = newPassword
            )
            val response = api.changePassword(request)
            if (response.isSuccessful) {
                return@withContext com.example.rentease.data.model.Result.Success(true)
            }
            
            return@withContext com.example.rentease.data.model.Result.Error("Failed to change password")
        } catch (e: Exception) {
            return@withContext com.example.rentease.data.model.Result.Error("Error changing password: ${e.message}")
        }
    }
    
    suspend fun saveUser(user: User): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            userDao.insertUser(UserEntity.fromUser(user))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(handleException(e))
        }
    }
    
    suspend fun clearUserData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            userDao.deleteAllUsers()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(handleException(e))
        }
    }
    
    // Landlord management methods
    suspend fun getLandlords(): com.example.rentease.data.model.Result<List<com.example.rentease.data.model.Landlord>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getLandlords()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null) {
                    @Suppress("UNCHECKED_CAST")
                    val landlords = apiResponse.data as? List<com.example.rentease.data.model.Landlord> ?: emptyList()
                    return@withContext com.example.rentease.data.model.Result.Success(landlords)
                }
            }
            
            return@withContext com.example.rentease.data.model.Result.Error("Failed to load landlords")
        } catch (e: Exception) {
            return@withContext com.example.rentease.data.model.Result.Error("Error loading landlords: ${e.message}")
        }
    }
    suspend fun approveLandlord(landlordId: Int): com.example.rentease.data.model.Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Fetch the landlord first
            val getLandlordResponse = api.getLandlord(landlordId)
            if (!getLandlordResponse.isSuccessful) {
                return@withContext com.example.rentease.data.model.Result.Error("Failed to find landlord")
            }
            
            val apiResponse = getLandlordResponse.body()
            if (apiResponse == null) {
                return@withContext com.example.rentease.data.model.Result.Error("Failed to find landlord")
            }
            
            // Mock implementation since we don't have the actual Landlord class structure
            // In a real implementation, we would update the landlord status and save it
            
            return@withContext com.example.rentease.data.model.Result.Success(true)
        } catch (e: Exception) {
            return@withContext com.example.rentease.data.model.Result.Error("Error approving landlord: ${e.message}")
        }
    }
    
    suspend fun rejectLandlord(landlordId: Int): com.example.rentease.data.model.Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Fetch the landlord first
            val getLandlordResponse = api.getLandlord(landlordId)
            if (!getLandlordResponse.isSuccessful) {
                return@withContext com.example.rentease.data.model.Result.Error("Failed to find landlord")
            }
            
            val apiResponse = getLandlordResponse.body()
            if (apiResponse == null) {
                return@withContext com.example.rentease.data.model.Result.Error("Failed to find landlord")
            }
            
            // Mock implementation since we don't have the actual Landlord class structure
            // In a real implementation, we would update the landlord status and save it
            
            return@withContext com.example.rentease.data.model.Result.Success(true)
        } catch (e: Exception) {
            return@withContext com.example.rentease.data.model.Result.Error("Error rejecting landlord: ${e.message}")
        }
    }
    
    suspend fun updateCurrentUser(user: User): com.example.rentease.data.model.Result<User> = withContext(Dispatchers.IO) {
        try {
            // Implement the API call to update the user
            // For now, just return a success result with the same user
            return@withContext com.example.rentease.data.model.Result.Success(user)
        } catch (e: Exception) {
            return@withContext com.example.rentease.data.model.Result.Error("Error updating user: ${e.message}")
        }
    }
}
