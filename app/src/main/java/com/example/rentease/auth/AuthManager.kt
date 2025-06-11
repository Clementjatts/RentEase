package com.example.rentease.auth

import android.content.Context

// Manages user authentication state using session-based authentication
class AuthManager private constructor() {

    // Simple session-based authentication state (in-memory only)
    var isLoggedIn: Boolean = false
        private set

    var userType: UserType? = null
        private set

    var username: String? = null
        private set

    var authToken: String? = null
        private set

    private var userId: Int? = null

    // Returns the current user ID as a string
    fun getUserId(): String {
        return userId?.toString() ?: ""
    }

    // Logs in a user with session-based authentication
    fun login(username: String, userType: UserType, authToken: String, userId: Int? = null) {
        this.username = username
        this.userType = userType
        this.authToken = authToken
        this.userId = userId
        this.isLoggedIn = true
    }

    // Logs out the user and clears all session state
    fun logout() {
        isLoggedIn = false
        userType = null
        username = null
        authToken = null
        userId = null
    }

    companion object {
        @Volatile
        private var instance: AuthManager? = null

        // Returns the singleton instance of AuthManager
        @Suppress("UNUSED_PARAMETER")
        fun getInstance(context: Context): AuthManager {
            return instance ?: synchronized(this) {
                instance ?: AuthManager().also { instance = it }
            }
        }
    }
}

// Defines the types of users in the system
enum class UserType {
    ADMIN,
    LANDLORD;

    companion object {
        // Converts a string value to UserType enum
        fun fromString(value: String): UserType {
            return when (value.uppercase()) {
                "ADMIN" -> ADMIN
                "LANDLORD" -> LANDLORD
                else -> LANDLORD
            }
        }
    }
}
