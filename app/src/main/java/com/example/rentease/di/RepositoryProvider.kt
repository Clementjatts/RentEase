package com.example.rentease.di

import android.content.Context
import com.example.rentease.auth.AuthManager
import com.example.rentease.data.api.ApiClient
import com.example.rentease.data.repository.AuthRepository
import com.example.rentease.data.repository.PropertyRepository
import com.example.rentease.data.repository.RequestRepository
import com.example.rentease.data.repository.UserRepository

// Simple dependency injection provider for repositories
object RepositoryProvider {
    // Creates and returns an AuthRepository instance
    fun provideAuthRepository(context: Context): AuthRepository {
        return AuthRepository(
            api = ApiClient.getApi(context),
            authManager = AuthManager.getInstance(context)
        )
    }

    // Creates and returns a PropertyRepository instance
    fun providePropertyRepository(context: Context): PropertyRepository {
        return PropertyRepository(
            api = ApiClient.getApi(context),
            context = context
        )
    }

    // Creates and returns a UserRepository instance
    fun provideUserRepository(context: Context): UserRepository {
        return UserRepository(
            api = ApiClient.getApi(context),
            authManager = AuthManager.getInstance(context)
        )
    }

    // Creates and returns a RequestRepository instance
    fun provideRequestRepository(context: Context): RequestRepository {
        return RequestRepository(
            api = ApiClient.getApi(context)
        )
    }

}
