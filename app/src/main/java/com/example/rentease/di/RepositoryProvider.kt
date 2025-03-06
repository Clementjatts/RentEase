package com.example.rentease.di

import android.content.Context
import com.example.rentease.auth.AuthManager
import com.example.rentease.data.api.ApiClient
import com.example.rentease.data.repository.AuthRepository
import com.example.rentease.data.repository.PropertyRepository
import com.example.rentease.data.repository.UserRepository

/**
 * A simple dependency injection provider for repositories
 */
object RepositoryProvider {
    fun provideAuthRepository(context: Context): AuthRepository {
        return AuthRepository(
            api = ApiClient.api,
            authManager = AuthManager.getInstance(context),
            context = context
        )
    }
    
    fun providePropertyRepository(context: Context): PropertyRepository {
        return PropertyRepository(
            api = ApiClient.api,
            context = context
        )
    }
    
    fun provideUserRepository(context: Context): UserRepository {
        return UserRepository(
            api = ApiClient.api,
            context = context
        )
    }

}
