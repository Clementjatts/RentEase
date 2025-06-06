package com.example.rentease.auth

import android.content.Context

class AuthManager private constructor(context: Context) {

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

    /**
     * Get the user ID as a string
     */
    fun getUserId(): String {
        return userId?.toString() ?: ""
    }

    /**
     * Login user with session-based authentication
     */
    fun login(username: String, userType: UserType, authToken: String, userId: Int? = null) {
        this.username = username
        this.userType = userType
        this.authToken = authToken
        this.userId = userId
        this.isLoggedIn = true
    }

    /**
     * Logout - clears all session state
     */
    fun logout() {
        isLoggedIn = false
        userType = null
        username = null
        authToken = null
        userId = null
    }

    companion object {
        private const val PREFS_NAME = "auth_prefs"

        @Volatile
        private var instance: AuthManager? = null

        fun getInstance(context: Context): AuthManager {
            return instance ?: synchronized(this) {
                instance ?: AuthManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

/**
 * Defines the types of users in the system
 */
enum class UserType {
    ADMIN,
    LANDLORD;

    companion object {
        fun fromString(value: String): UserType {
            return when (value.uppercase()) {
                "ADMIN" -> ADMIN
                "LANDLORD" -> LANDLORD
                else -> LANDLORD
            }
        }
    }
}
