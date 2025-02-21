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

    suspend fun login(
        username: String,
        password: String,
        userType: UserType
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            val request = LoginRequest(
                username = username,
                password = password,
                userType = userType.name
            )

            val response = api.login(request)
            if (response.isSuccessful) {
                val loginResponse = response.body()!!
                authManager.login(
                    username = loginResponse.user.username,
                    userType = userType,
                    authToken = loginResponse.token
                )
                Result.success(loginResponse.user)
            } else {
                Result.failure(handleApiError(response))
            }
        } catch (e: Exception) {
            Result.failure(handleException(e))
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
                    username = loginResponse.user.username,
                    userType = userType,
                    authToken = loginResponse.token
                )
                Result.success(loginResponse.user)
            } else {
                Result.failure(handleApiError(response))
            }
        } catch (e: Exception) {
            Result.failure(handleException(e))
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
                Result.success(Unit)
            } else {
                Result.failure(handleApiError(response))
            }
        } catch (e: Exception) {
            Result.failure(handleException(e))
        }
    }

    suspend fun getCurrentUser(): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = api.getCurrentUser()
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(handleApiError(response))
            }
        } catch (e: Exception) {
            Result.failure(handleException(e))
        }
    }

    fun logout() {
        authManager.logout()
    }
}
