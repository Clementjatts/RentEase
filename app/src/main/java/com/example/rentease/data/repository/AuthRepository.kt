package com.example.rentease.data.repository

import android.content.Context
import com.example.rentease.auth.AuthManager
import com.example.rentease.auth.UserType
import com.example.rentease.data.api.RentEaseApi
import com.example.rentease.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val api: RentEaseApi,
    private val authManager: AuthManager,
    private val context: Context
) : BaseRepository() {
    
    private val userRepository by lazy { UserRepository(api, context) }

    suspend fun login(
        username: String,
        password: String
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            val request = LoginRequest(
                username = username,
                password = password
            )

            val response = api.login(request)
            if (response.isSuccessful) {
                val loginResponse = response.body()!!
                // Get user type from response
                val userType = UserType.valueOf(loginResponse.user.userType)
                
                authManager.login(
                    username = loginResponse.user.username,
                    userType = userType,
                    authToken = loginResponse.token,
                    userId = loginResponse.user.id
                )
                // Cache user data
                userRepository.saveUser(loginResponse.user)
                Result.Success(loginResponse.user)
            } else {
                Result.Error(handleApiError(response).message)
            }
        } catch (e: Exception) {
            Result.Error(handleException(e).message)
        }
    }

    suspend fun register(
        username: String,
        password: String,
        email: String,
        fullName: String,
        phone: String,
        userType: UserType
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            val request = RegisterRequest(
                username = username,
                password = password,
                email = email,
                // Ensure user_type is "ADMIN" or "LANDLORD" as expected by backend
                userType = userType.name,
                fullName = fullName,
                phone = phone
            )

            val response = api.register(request)
            if (response.isSuccessful) {
                val loginResponse = response.body()!!
                authManager.login(
                    username = loginResponse.user.username,
                    userType = userType,
                    authToken = loginResponse.token,
                    userId = loginResponse.user.id
                )
                // Cache user data
                userRepository.saveUser(loginResponse.user)
                Result.Success(loginResponse.user)
            } else {
                Result.Error(handleApiError(response).message)
            }
        } catch (e: Exception) {
            Result.Error(handleException(e).message)
        }
    }

    suspend fun getCurrentUser(): com.example.rentease.data.model.Result<User> = withContext(Dispatchers.IO) {
            if (!authManager.isLoggedIn) {
                return@withContext com.example.rentease.data.model.Result.Error(Exception("User not logged in").message)
            }
            
            val result = userRepository.getUser(authManager.userId ?: -1)
            if (result.isSuccess) {
                return@withContext com.example.rentease.data.model.Result.Success(result.getOrNull()!!)
            } else {
                return@withContext com.example.rentease.data.model.Result.Error(result.exceptionOrNull()?.message)
            }
        }

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = ChangePasswordRequest(
                currentPassword = currentPassword,
                newPassword = newPassword
            )

            val response = api.changePassword(request)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error(handleApiError(response).message)
            }
        } catch (e: Exception) {
            Result.Error(handleException(e).message)
        }
    }

    fun logout() {
        authManager.logout()
        // Clear cached user data
        kotlinx.coroutines.runBlocking {
            userRepository.clearUserData()
        }
    }
}
