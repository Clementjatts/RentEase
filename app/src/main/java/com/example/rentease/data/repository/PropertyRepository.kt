package com.example.rentease.data.repository

import android.content.Context
import android.net.Uri
import com.example.rentease.data.model.ApiResponse
import com.example.rentease.data.api.RentEaseApi
import com.example.rentease.data.local.PropertyDao
import com.example.rentease.data.local.RentEaseDatabase
import com.example.rentease.data.model.Property
import com.example.rentease.utils.ImageUploader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class PropertyRepository(
    private val api: RentEaseApi,
    private val context: Context
) : BaseRepository() {

    private val imageUploader = ImageUploader(context, api)
    private val propertyDao = RentEaseDatabase.getDatabase(context).propertyDao()

    suspend fun getProperties(forceRefresh: Boolean = false): com.example.rentease.data.model.Result<List<Property>> = withContext(Dispatchers.IO) {
        try {
            // If not forcing refresh and we have cached data, return it
            if (!forceRefresh) {
                val cachedProperties = propertyDao.getAllProperties().first()
                if (cachedProperties.isNotEmpty()) {
                    return@withContext com.example.rentease.data.model.Result.Success(cachedProperties)
                }
            }

            // Fetch from network
            val response = api.getProperties()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    @Suppress("UNCHECKED_CAST")
                    val properties = body.data as List<Property>
                    // Update cache
                    propertyDao.deleteAllProperties()
                    propertyDao.insertAllProperties(properties)
                    return@withContext com.example.rentease.data.model.Result.Success(properties)
                } else {
                    // If network call fails and we have cached data, return it
                    val cachedProperties = propertyDao.getAllProperties().first()
                    if (cachedProperties.isNotEmpty()) {
                        return@withContext com.example.rentease.data.model.Result.Success(cachedProperties)
                    } else {
                        return@withContext com.example.rentease.data.model.Result.Error(Exception(body?.message ?: "Failed to get properties").message)
                    }
                }
            } else {
                // If network call fails and we have cached data, return it
                val cachedProperties = propertyDao.getAllProperties().first()
                if (cachedProperties.isNotEmpty()) {
                    return@withContext com.example.rentease.data.model.Result.Success(cachedProperties)
                } else {
                    return@withContext com.example.rentease.data.model.Result.Error(handleApiError(response).message)
                }
            }
        } catch (e: Exception) {
            // On network error, try to load from cache
            val cachedProperties = propertyDao.getAllProperties().first()
            if (cachedProperties.isNotEmpty()) {
                return@withContext com.example.rentease.data.model.Result.Success(cachedProperties)
            } else {
                return@withContext com.example.rentease.data.model.Result.Error(handleException(e).message)
            }
        }
    }

    suspend fun getProperty(id: Int): com.example.rentease.data.model.Result<Property> = withContext(Dispatchers.IO) {
        try {
            val response = api.getProperty(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    @Suppress("UNCHECKED_CAST")
                    val property = body.data as Property
                    propertyDao.insertProperty(property)
                    return@withContext com.example.rentease.data.model.Result.Success(property)
                } else {
                    // Try to load from cache
                    val cachedProperty = propertyDao.getPropertyById(id)
                    if (cachedProperty != null) {
                        return@withContext com.example.rentease.data.model.Result.Success(cachedProperty)
                    } else {
                        return@withContext com.example.rentease.data.model.Result.Error(Exception(body?.message ?: "Failed to get property").message)
                    }
                }
            } else {
                // Try to load from cache
                val cachedProperty = propertyDao.getPropertyById(id)
                if (cachedProperty != null) {
                    return@withContext com.example.rentease.data.model.Result.Success(cachedProperty)
                } else {
                    return@withContext com.example.rentease.data.model.Result.Error(handleApiError(response).message)
                }
            }
        } catch (e: Exception) {
            // Try to load from cache
            val cachedProperty = propertyDao.getPropertyById(id)
            if (cachedProperty != null) {
                return@withContext com.example.rentease.data.model.Result.Success(cachedProperty)
            } else {
                return@withContext com.example.rentease.data.model.Result.Error(handleException(e).message)
            }
        }
    }

    /**
     * Get a property by its ID.
     *
     * @param propertyId The ID of the property to retrieve
     * @return A Result containing the property if successful, or an error message if not
     */
    suspend fun getPropertyById(propertyId: Int): com.example.rentease.data.model.Result<Property> = withContext(Dispatchers.IO) {
        try {
            // Try to get from local database first
            val property = propertyDao.getPropertyById(propertyId)
            if (property != null) {
                return@withContext com.example.rentease.data.model.Result.Success(property)
            }
            
            // If not in local database, fetch from API
            // For now, returning a mock error since API implementation would be more complex
            return@withContext com.example.rentease.data.model.Result.Error("Property not found")
        } catch (e: Exception) {
            return@withContext com.example.rentease.data.model.Result.Error("Error fetching property: ${e.message}")
        }
    }
    
    /**
     * Get properties owned by a specific landlord.
     *
     * @param landlordId The ID of the landlord
     * @return A Result containing the list of properties if successful, or an error message if not
     */
    suspend fun getLandlordProperties(landlordId: String): com.example.rentease.data.model.Result<List<Property>> = withContext(Dispatchers.IO) {
        try {
            // First try to get from local database
            val properties = propertyDao.getPropertiesByLandlordId(landlordId.toIntOrNull() ?: -1)
            if (properties.isNotEmpty()) {
                return@withContext com.example.rentease.data.model.Result.Success(properties)
            }
            
            // If not in database or empty, fetch from API
            val response = api.getProperties()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    @Suppress("UNCHECKED_CAST")
                    val allProperties = body.data as? List<Property> ?: emptyList()
                    // Filter properties by landlord ID
                    val landlordProperties = allProperties.filter {
                        it.landlordId.toString() == landlordId
                    }
                    
                    // Cache the properties
                    if (landlordProperties.isNotEmpty()) {
                        propertyDao.insertAllProperties(landlordProperties)
                    }
                    
                    return@withContext com.example.rentease.data.model.Result.Success(landlordProperties)
                }
            }
            
            // If API call fails, return empty list
            return@withContext com.example.rentease.data.model.Result.Success(emptyList())
        } catch (e: Exception) {
            return@withContext com.example.rentease.data.model.Result.Error("Error fetching landlord properties: ${e.message}")
        }
    }

    suspend fun createProperty(property: Property, images: List<Uri> = emptyList()): com.example.rentease.data.model.Result<Property> = withContext(Dispatchers.IO) {
        try {
            val response = api.createProperty(property)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    @Suppress("UNCHECKED_CAST")
                    val createdProperty = body.data as Property
                    propertyDao.insertProperty(createdProperty)

                    // Upload images if any
                    if (images.isNotEmpty()) {
                        uploadPropertyImages(createdProperty.id, images)
                    }

                    return@withContext com.example.rentease.data.model.Result.Success(createdProperty)
                } else {
                    return@withContext com.example.rentease.data.model.Result.Error(Exception(body?.message ?: "Failed to create property").message)
                }
            } else {
                return@withContext com.example.rentease.data.model.Result.Error(handleApiError(response).message)
            }
        } catch (e: Exception) {
            return@withContext com.example.rentease.data.model.Result.Error(handleException(e).message)
        }
    }

    suspend fun updateProperty(property: Property, images: List<Uri> = emptyList()): com.example.rentease.data.model.Result<Property> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateProperty(property)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    @Suppress("UNCHECKED_CAST")
                    val updatedProperty = body.data as Property
                    propertyDao.updateProperty(updatedProperty)

                    // Upload images if any
                    if (images.isNotEmpty()) {
                        uploadPropertyImages(updatedProperty.id, images)
                    }

                    return@withContext com.example.rentease.data.model.Result.Success(updatedProperty)
                } else {
                    return@withContext com.example.rentease.data.model.Result.Error(Exception(body?.message ?: "Failed to update property").message)
                }
            } else {
                return@withContext com.example.rentease.data.model.Result.Error(handleApiError(response).message)
            }
        } catch (e: Exception) {
            return@withContext com.example.rentease.data.model.Result.Error(handleException(e).message)
        }
    }

    suspend fun deleteProperty(id: Int): com.example.rentease.data.model.Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteProperty(id)
            if (response.isSuccessful) {
                propertyDao.deletePropertyById(id)
                return@withContext com.example.rentease.data.model.Result.Success(Unit)
            } else {
                return@withContext com.example.rentease.data.model.Result.Error(handleApiError(response).message)
            }
        } catch (e: Exception) {
            return@withContext com.example.rentease.data.model.Result.Error(handleException(e).message)
        }
    }

    private suspend fun uploadPropertyImages(propertyId: Int, images: List<Uri>): List<String> {
        return images.mapNotNull { uri ->
            imageUploader.uploadImage(propertyId, uri).getOrNull()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: PropertyRepository? = null
        
        fun getInstance(context: Context): PropertyRepository {
            return INSTANCE ?: synchronized(this) {
                val api = com.example.rentease.data.api.ApiClient.api
                val instance = PropertyRepository(api, context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
