package com.example.rentease.di

import android.content.Context
import com.example.rentease.auth.AuthManager
import com.example.rentease.data.api.ApiClient
import com.example.rentease.data.repository.AuthRepository

/**
 * A simple dependency injection provider for repositories
 */
object RepositoryProvider {
    fun provideAuthRepository(context: Context): AuthRepository {
        return AuthRepository(
            api = ApiClient.api,
            authManager = AuthManager.getInstance(context)
        )
    }
}
