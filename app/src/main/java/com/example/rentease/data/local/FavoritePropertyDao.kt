package com.example.rentease.data.local

import androidx.room.*
import com.example.rentease.data.model.FavoriteProperty
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritePropertyDao {
    @Query("SELECT * FROM favorite_properties WHERE userId = :userId")
    fun getFavoritesByUser(userId: String): Flow<List<FavoriteProperty>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_properties WHERE userId = :userId AND propertyId = :propertyId)")
    suspend fun isFavorite(userId: String, propertyId: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteProperty): Long

    @Delete
    suspend fun delete(favorite: FavoriteProperty)

    @Query("DELETE FROM favorite_properties WHERE userId = :userId AND propertyId = :propertyId")
    suspend fun deleteFavorite(userId: String, propertyId: Int)
}
