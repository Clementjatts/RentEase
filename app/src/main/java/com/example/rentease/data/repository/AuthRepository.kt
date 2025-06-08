package com.example.rentease.data.repository

import com.example.rentease.auth.AuthManager
import com.example.rentease.auth.UserType
import com.example.rentease.data.api.RentEaseApi
import com.example.rentease.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val api: RentEaseApi,
    private val authManager: AuthManager
) : BaseRepository() {

    private val userRepository by lazy { UserRepository(api, authManager) }

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
                val userType = UserType.valueOf(loginResponse.data.user.userType)

                authManager.login(
                    username = loginResponse.data.user.username,
                    userType = userType,
                    authToken = loginResponse.data.token,
                    userId = loginResponse.data.user.id
                )
                // Cache user data
                userRepository.saveUser()
                Result.Success(loginResponse.data.user)
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                android.util.Log.e("AuthRepository", "Login failed: ${response.code()} - $errorBody")
                Result.Error(handleApiError(response).message)
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthRepository", "Login exception", e)
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
                userType = userType.name,
                fullName = fullName,
                phone = phone
            )

            val response = api.register(request)
            if (response.isSuccessful) {
                val loginResponse = response.body()!!
                authManager.login(
                    username = loginResponse.data.user.username,
                    userType = userType,
                    authToken = loginResponse.data.token,
                    userId = loginResponse.data.user.id
                )
                // Cache user data
                userRepository.saveUser()
                Result.Success(loginResponse.data.user)
            } else {
                Result.Error(handleApiError(response).message)
            }
        } catch (e: Exception) {
            Result.Error(handleException(e).message)
        }
    }

    /**
     * Register a new user without affecting the current admin session.
     */
    suspend fun registerUserAsAdmin(
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
                userType = userType.name,
                fullName = fullName,
                phone = phone
            )

            val response = api.register(request)
            if (response.isSuccessful) {
                val loginResponse = response.body()!!
                Result.Success(loginResponse.data.user)
            } else {
                Result.Error(handleApiError(response).message)
            }
        } catch (e: Exception) {
            Result.Error(handleException(e).message)
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
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    Result.Success(Unit)
                } else {
                    Result.Error(apiResponse?.message ?: "Failed to change password")
                }
            } else {
                Result.Error(handleApiError(response).message)
            }
        } catch (e: Exception) {
            Result.Error(handleException(e).message)
        }
    }

}
