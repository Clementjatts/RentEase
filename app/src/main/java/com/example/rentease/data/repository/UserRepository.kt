package com.example.rentease.data.repository

import android.content.Context
import com.example.rentease.data.api.RentEaseApi
import com.example.rentease.data.local.RentEaseDatabase
import com.example.rentease.data.model.User
import com.example.rentease.data.model.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    private val api: RentEaseApi,
    private val context: Context
) : BaseRepository() {

    private val userDao = RentEaseDatabase.getDatabase(context).userDao()
    
    suspend fun getUser(userId: Int): Result<User> = withContext(Dispatchers.IO) {
        try {
            // First try to get from local database
            val cachedUser = userDao.getUserById(userId)
            if (cachedUser != null) {
                return@withContext Result.success(UserEntity.toUser(cachedUser))
            }
            
            // If not in database, fetch from API
            val response = api.getCurrentUser()
            if (response.isSuccessful) {
                val user = response.body()
                if (user != null) {
                    // Cache the user in the database
                    userDao.insertUser(UserEntity.fromUser(user))
                    Result.success(user)
                } else {
                    Result.failure(Exception("User data is null"))
                }
            } else {
                Result.failure(handleApiError(response))
            }
        } catch (e: Exception) {
            Result.failure(handleException(e))
        }
    }
    
    suspend fun saveUser(user: User): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            userDao.insertUser(UserEntity.fromUser(user))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(handleException(e))
        }
    }
    
    suspend fun clearUserData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            userDao.deleteAllUsers()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(handleException(e))
        }
    }
}
