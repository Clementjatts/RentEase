package com.example.rentease.data.local

import androidx.room.*
import com.example.rentease.data.model.Landlord
import kotlinx.coroutines.flow.Flow

@Dao
interface LandlordDao {
    @Query("SELECT * FROM landlords")
    fun getAllLandlords(): Flow<List<Landlord>>

    @Query("SELECT * FROM landlords WHERE id = :id")
    suspend fun getLandlordById(id: Int): Landlord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLandlord(landlord: Landlord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLandlords(landlords: List<Landlord>)

    @Update
    suspend fun updateLandlord(landlord: Landlord)

    @Delete
    suspend fun deleteLandlord(landlord: Landlord)

    @Query("DELETE FROM landlords")
    suspend fun deleteAllLandlords()
}
