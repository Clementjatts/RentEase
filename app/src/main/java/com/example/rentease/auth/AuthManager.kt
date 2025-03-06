package com.example.rentease.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AuthManager private constructor(context: Context) {
    private var masterKey: MasterKey
    private var prefs: SharedPreferences

    init {
        try {
            masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            prefs = try {
                EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e: Exception) {
                // Fallback to regular SharedPreferences if encryption fails
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            }
        } catch (e: Exception) {
            // If master key creation fails, use a simpler approach
            throw RuntimeException("Failed to initialize AuthManager: ${e.message}", e)
        }
    }

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        private set(value) = prefs.edit { putBoolean(KEY_IS_LOGGED_IN, value) }

    var userType: UserType?
        get() = prefs.getString(KEY_USER_TYPE, null)?.let { UserType.valueOf(it) }
        private set(value) = prefs.edit { putString(KEY_USER_TYPE, value?.name) }

    var username: String?
        get() = prefs.getString(KEY_USERNAME, null)
        private set(value) = prefs.edit { putString(KEY_USERNAME, value) }

    var authToken: String?
        get() = prefs.getString(KEY_AUTH_TOKEN, null)
        private set(value) = prefs.edit { putString(KEY_AUTH_TOKEN, value) }
        
    var userId: Int?
        get() = prefs.getInt(KEY_USER_ID, -1).takeIf { it != -1 }
        private set(value) = prefs.edit { 
            if (value != null) {
                putInt(KEY_USER_ID, value)
            } else {
                remove(KEY_USER_ID)
            }
        }
        
    /**
     * Get the user ID as a string
     * @return The user ID as a string, or an empty string if not available
     */
    fun getUserId(): String {
        return userId?.toString() ?: ""
    }

    fun login(username: String, userType: UserType, authToken: String, userId: Int? = null) {
        this.username = username
        this.userType = userType
        this.authToken = authToken
        this.userId = userId
        this.isLoggedIn = true
    }

    fun logout() {
        prefs.edit {
            clear()
            apply()
        }
    }

    companion object {
        private const val PREFS_NAME = "auth_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_TYPE = "user_type"
        private const val KEY_USERNAME = "username"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"

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
 * Note: Only ADMIN and LANDLORD are supported as registered users.
 * Regular users (previously TENANT) do not need to log in to view properties.
 */
enum class UserType {
    ADMIN,
    LANDLORD;
    
    companion object {
        fun fromString(value: String): UserType {
            return when (value.uppercase()) {
                "ADMIN" -> ADMIN
                "LANDLORD" -> LANDLORD
                else -> LANDLORD // Default to LANDLORD for safety
            }
        }
    }
}
