package com.example.rentease.data.repository

import android.content.Context
import android.net.Uri
import com.example.rentease.data.api.ApiResponse
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

    suspend fun getProperties(forceRefresh: Boolean = false): Result<List<Property>> = withContext(Dispatchers.IO) {
        try {
            // If not forcing refresh and we have cached data, return it
            if (!forceRefresh) {
                val cachedProperties = propertyDao.getAllProperties().first()
                if (cachedProperties.isNotEmpty()) {
                    return@withContext Result.success(cachedProperties)
                }
            }

            // Fetch from network
            val response = api.getProperties()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == "success") {
                    @Suppress("UNCHECKED_CAST")
                    val properties = body.data as List<Property>
                    // Update cache
                    propertyDao.deleteAllProperties()
                    propertyDao.insertAllProperties(properties)
                    Result.success(properties)
                } else {
                    // If network call fails and we have cached data, return it
                    val cachedProperties = propertyDao.getAllProperties().first()
                    if (cachedProperties.isNotEmpty()) {
                        Result.success(cachedProperties)
                    } else {
                        Result.failure(Exception(body?.message ?: "Failed to get properties"))
                    }
                }
            } else {
                // If network call fails and we have cached data, return it
                val cachedProperties = propertyDao.getAllProperties().first()
                if (cachedProperties.isNotEmpty()) {
                    Result.success(cachedProperties)
                } else {
                    Result.failure(handleApiError(response))
                }
            }
        } catch (e: Exception) {
            // On network error, try to load from cache
            val cachedProperties = propertyDao.getAllProperties().first()
            if (cachedProperties.isNotEmpty()) {
                Result.success(cachedProperties)
            } else {
                Result.failure(handleException(e))
            }
        }
    }

    suspend fun getProperty(id: Int): Result<Property> = withContext(Dispatchers.IO) {
        try {
            val response = api.getProperty(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == "success") {
                    @Suppress("UNCHECKED_CAST")
                    val property = body.data as Property
                    propertyDao.insertProperty(property)
                    Result.success(property)
                } else {
                    // Try to load from cache
                    val cachedProperty = propertyDao.getPropertyById(id)
                    if (cachedProperty != null) {
                        Result.success(cachedProperty)
                    } else {
                        Result.failure(Exception(body?.message ?: "Failed to get property"))
                    }
                }
            } else {
                // Try to load from cache
                val cachedProperty = propertyDao.getPropertyById(id)
                if (cachedProperty != null) {
                    Result.success(cachedProperty)
                } else {
                    Result.failure(handleApiError(response))
                }
            }
        } catch (e: Exception) {
            // Try to load from cache
            val cachedProperty = propertyDao.getPropertyById(id)
            if (cachedProperty != null) {
                Result.success(cachedProperty)
            } else {
                Result.failure(handleException(e))
            }
        }
    }

    suspend fun createProperty(property: Property, images: List<Uri> = emptyList()): Result<Property> = withContext(Dispatchers.IO) {
        try {
            val response = api.createProperty(property)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == "success") {
                    @Suppress("UNCHECKED_CAST")
                    val createdProperty = body.data as Property
                    propertyDao.insertProperty(createdProperty)

                    // Upload images if any
                    if (images.isNotEmpty()) {
                        uploadPropertyImages(createdProperty.id, images)
                    }

                    Result.success(createdProperty)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to create property"))
                }
            } else {
                Result.failure(handleApiError(response))
            }
        } catch (e: Exception) {
            Result.failure(handleException(e))
        }
    }

    suspend fun updateProperty(property: Property, images: List<Uri> = emptyList()): Result<Property> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateProperty(property)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status == "success") {
                    @Suppress("UNCHECKED_CAST")
                    val updatedProperty = body.data as Property
                    propertyDao.updateProperty(updatedProperty)

                    // Upload images if any
                    if (images.isNotEmpty()) {
                        uploadPropertyImages(updatedProperty.id, images)
                    }

                    Result.success(updatedProperty)
                } else {
                    Result.failure(Exception(body?.message ?: "Failed to update property"))
                }
            } else {
                Result.failure(handleApiError(response))
            }
        } catch (e: Exception) {
            Result.failure(handleException(e))
        }
    }

    suspend fun deleteProperty(id: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteProperty(id)
            if (response.isSuccessful) {
                propertyDao.deletePropertyById(id)
                Result.success(Unit)
            } else {
                Result.failure(handleApiError(response))
            }
        } catch (e: Exception) {
            Result.failure(handleException(e))
        }
    }

    private suspend fun uploadPropertyImages(propertyId: Int, images: List<Uri>): List<String> {
        return images.mapNotNull { uri ->
            imageUploader.uploadImage(propertyId, uri).getOrNull()
        }
    }
}
