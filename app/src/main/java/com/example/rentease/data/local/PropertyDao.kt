package com.example.rentease.data.local

import androidx.room.*
import com.example.rentease.data.model.Property
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyDao {
    @Query("SELECT * FROM properties")
    fun getAllProperties(): Flow<List<Property>>

    @Query("SELECT * FROM properties WHERE id = :id")
    fun getPropertyByIdFlow(id: Int): Flow<Property?>
    
    @Query("SELECT * FROM properties WHERE id = :id")
    suspend fun getPropertyById(id: Int): Property?

    @Query("SELECT * FROM properties WHERE landlordId = :landlordId")
    fun getPropertiesByLandlord(landlordId: Int): Flow<List<Property>>
    
    @Query("SELECT * FROM properties WHERE landlordId = :landlordId")
    suspend fun getPropertiesByLandlordId(landlordId: Int): List<Property>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: Property)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProperties(properties: List<Property>)

    @Update
    suspend fun updateProperty(property: Property)

    @Delete
    suspend fun deleteProperty(property: Property)
    
    @Query("DELETE FROM properties WHERE id = :id")
    suspend fun deletePropertyById(id: Int)

    @Query("DELETE FROM properties")
    suspend fun deleteAllProperties()
}
