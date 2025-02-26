package com.example.rentease.data.local

import androidx.room.*
import com.example.rentease.data.model.UserRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface UserRequestDao {
    @Query("SELECT * FROM user_requests")
    fun getAllRequests(): Flow<List<UserRequest>>

    @Query("SELECT * FROM user_requests WHERE userId = :userId")
    fun getRequestsByUser(userId: String): Flow<List<UserRequest>>

    @Query("SELECT * FROM user_requests WHERE userId = :userId")
    suspend fun getUserRequests(userId: String): List<UserRequest>

    @Query("SELECT * FROM user_requests WHERE propertyId = :propertyId")
    fun getRequestsByProperty(propertyId: Int): Flow<List<UserRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: UserRequest)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(request: UserRequest): Long

    @Update
    suspend fun update(request: UserRequest)

    @Delete
    suspend fun delete(request: UserRequest)

    @Query("DELETE FROM user_requests WHERE id = :id")
    suspend fun deleteById(id: Int)
}
