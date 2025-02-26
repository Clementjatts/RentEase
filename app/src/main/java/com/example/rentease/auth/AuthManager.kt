package com.example.rentease.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AuthManager private constructor(context: Context) {
    private lateinit var masterKey: MasterKey
    private lateinit var prefs: SharedPreferences

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

    fun login(username: String, userType: UserType, authToken: String) {
        this.username = username
        this.userType = userType
        this.authToken = authToken
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

        @Volatile
        private var instance: AuthManager? = null

        fun getInstance(context: Context): AuthManager {
            return instance ?: synchronized(this) {
                instance ?: AuthManager(context.applicationContext).also { instance = it }
            }
        }
    }
}

enum class UserType {
    ADMIN,
    LANDLORD
}
