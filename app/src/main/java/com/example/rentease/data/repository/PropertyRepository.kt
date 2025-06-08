package com.example.rentease.data.repository

import android.content.Context
import android.net.Uri
import com.example.rentease.data.api.RentEaseApi
import com.example.rentease.data.model.Property
import com.example.rentease.utils.ImageUploader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PropertyRepository(
    private val api: RentEaseApi,
    context: Context
) : BaseRepository() {

    private val imageUploader = ImageUploader(context, api)

    suspend fun getProperties(): com.example.rentease.data.model.Result<List<Property>> = withContext(Dispatchers.IO) {
        try {
            // Fetch from network
            val response = api.getProperties()

            // First check if the response was successful
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    try {
                        // The API returns data.properties, not just a list directly
                        val dataMap = body.data as? Map<*, *>
                        if (dataMap == null) {
                            android.util.Log.e("PropertyRepository", "dataMap is null, body.data is: ${body.data}")
                            return@withContext com.example.rentease.data.model.Result.Error("Failed to parse properties: dataMap is null")
                        }

                        val propertiesList = dataMap["properties"] as? List<*>
                        if (propertiesList == null) {
                            android.util.Log.e("PropertyRepository", "propertiesList is null, dataMap keys: ${dataMap.keys}")
                            return@withContext com.example.rentease.data.model.Result.Error("Failed to parse properties: propertiesList is null")
                        }

                        // Use a safer approach to convert the list
                        val properties = mutableListOf<Property>()
                        for (item in propertiesList) {
                            try {
                                if (item is Map<*, *>) {
                                    // Convert the map to a Property object
                                    val property = convertMapToProperty(item)
                                    properties.add(property)
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("PropertyRepository", "Error converting item to Property: ${e.message}")
                                // Continue with the next item instead of failing completely
                            }
                        }

                        return@withContext com.example.rentease.data.model.Result.Success(properties)
                    } catch (e: Exception) {
                        android.util.Log.e("PropertyRepository", "Error parsing properties: ${e.message}", e)
                        return@withContext com.example.rentease.data.model.Result.Error("Failed to parse properties: ${e.message}")
                    }
                } else {
                    return@withContext com.example.rentease.data.model.Result.Error("Failed to get properties: Response body is null")
                }
            } else {
                return@withContext com.example.rentease.data.model.Result.Error(handleApiError(response).message)
            }
        } catch (e: Exception) {
            return@withContext com.example.rentease.data.model.Result.Error(handleException(e).message)
        }
    }

    // Helper method to convert a Map to a Property object
    private fun convertMapToProperty(map: Map<*, *>): Property {
        // Try to parse the date or use current date as fallback
        val dateAdded = try {
            val dateStr = map["created_at"] as? String
            if (dateStr != null) {
                // Simple parsing - in a real app you'd use a proper date formatter
                java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).parse(dateStr)
            } else {
                java.util.Date()
            }
        } catch (e: Exception) {
            android.util.Log.e("PropertyRepository", "Error parsing date: ${e.message}")
            java.util.Date()
        }

        // Get the address from the map
        val rawAddress = map["address"] as? String

        // If address is missing, create a fallback address from the title
        val address = if (rawAddress.isNullOrBlank()) {
            val title = map["title"] as? String ?: ""
            val city = when {
                title.contains("Downtown", ignoreCase = true) -> "Downtown"
                title.contains("Park", ignoreCase = true) -> "Near Park"
                title.contains("Penthouse", ignoreCase = true) -> "Luxury District"
                title.contains("Suburban", ignoreCase = true) -> "Suburban Area"
                else -> "City Center"
            }
            "$city, ${map["bedroom_count"]} bed, ${map["bathroom_count"]} bath"
        } else {
            rawAddress
        }

        return Property(
            id = (map["id"] as? Number)?.toInt() ?: 0,
            title = map["title"] as? String ?: "",
            description = map["description"] as? String,
            landlordId = (map["landlord_id"] as? Number)?.toInt() ?: 0,
            price = (map["price"] as? Number)?.toDouble() ?: 0.0,
            address = address,
            dateAdded = dateAdded,
            type = map["furniture_type"] as? String,
            landlordName = map["landlord_name"] as? String,
            landlordContact = map["landlord_contact"] as? String,
            bedroomCount = (map["bedroom_count"] as? Number)?.toInt() ?: 0,
            bathroomCount = (map["bathroom_count"] as? Number)?.toInt() ?: 0,
            furnitureType = map["furniture_type"] as? String ?: "unfurnished",
            imageUrl = transformImageUrl(map["image_url"] as? String)
        )
    }



    /**
     * Get a property by its ID.
     *
     * @param propertyId The ID of the property to retrieve
     * @return A Result containing the property if successful, or an error message if not
     */
    suspend fun getPropertyById(propertyId: Int): com.example.rentease.data.model.Result<Property> = withContext(Dispatchers.IO) {
        try {
            // Fetch from API
            val response = api.getProperty(propertyId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    try {
                        if (body.data is Map<*, *>) {
                            val property = convertMapToProperty(body.data)
                            return@withContext com.example.rentease.data.model.Result.Success(property)
                        } else {
                            android.util.Log.e("PropertyRepository", "Property data is not a Map: ${body.data}")
                            return@withContext com.example.rentease.data.model.Result.Error("Failed to parse property: data is not a Map")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("PropertyRepository", "Error parsing property by ID: ${e.message}", e)
                        return@withContext com.example.rentease.data.model.Result.Error("Failed to parse property: ${e.message}")
                    }
                } else {
                    return@withContext com.example.rentease.data.model.Result.Error("Property not found")
                }
            } else {
                return@withContext com.example.rentease.data.model.Result.Error(handleApiError(response).message)
            }
        } catch (e: Exception) {
            android.util.Log.e("PropertyRepository", "Exception in getPropertyById: ${e.message}", e)
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
            // Fetch from API
            val response = api.getProperties()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    try {
                        // The API returns data.properties, not just a list directly
                        val dataMap = body.data as? Map<*, *>
                        if (dataMap == null) {
                            android.util.Log.e("PropertyRepository", "dataMap is null for landlord properties")
                            return@withContext com.example.rentease.data.model.Result.Error("Failed to parse landlord properties: dataMap is null")
                        }

                        val propertiesList = dataMap["properties"] as? List<*>
                        if (propertiesList == null) {
                            android.util.Log.e("PropertyRepository", "propertiesList is null for landlord properties")
                            return@withContext com.example.rentease.data.model.Result.Error("Failed to parse landlord properties: propertiesList is null")
                        }

                        // Use a safer approach to convert the list
                        val allProperties = mutableListOf<Property>()
                        for (item in propertiesList) {
                            try {
                                if (item is Map<*, *>) {
                                    // Convert the map to a Property object
                                    val property = convertMapToProperty(item)
                                    allProperties.add(property)
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("PropertyRepository", "Error converting item to Property for landlord: ${e.message}")
                                // Continue with the next item instead of failing completely
                            }
                        }

                        // Filter properties by landlord ID
                        val landlordProperties = allProperties.filter {
                            it.landlordId.toString() == landlordId
                        }

                        return@withContext com.example.rentease.data.model.Result.Success(landlordProperties)
                    } catch (e: Exception) {
                        android.util.Log.e("PropertyRepository", "Error parsing landlord properties: ${e.message}", e)
                        return@withContext com.example.rentease.data.model.Result.Error("Failed to parse landlord properties: ${e.message}")
                    }
                }
            }

            // If API call fails, return empty list
            return@withContext com.example.rentease.data.model.Result.Success(emptyList())
        } catch (e: Exception) {
            android.util.Log.e("PropertyRepository", "Exception in getLandlordProperties: ${e.message}", e)
            return@withContext com.example.rentease.data.model.Result.Error("Error fetching landlord properties: ${e.message}")
        }
    }

    suspend fun createProperty(property: Property, imageUri: Uri? = null): com.example.rentease.data.model.Result<Property> = withContext(Dispatchers.IO) {
        try {
            val response = api.createProperty(property)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    try {
                        if (body.data is Map<*, *>) {
                            val createdProperty = convertMapToProperty(body.data)

                            // Upload single image if provided
                            imageUri?.let { uri ->
                                try {
                                    val result = imageUploader.uploadImage(createdProperty.id, uri)
                                    if (result.isSuccess) {
                                        result.getOrNull()?.let { uploadedUrl ->
                                            createdProperty.imageUrl = uploadedUrl
                                        }
                                    } else {
                                        val exception = result.exceptionOrNull()
                                        android.util.Log.e("PropertyRepository", "Image upload failed: ${exception?.message}", exception)
                                        // Continue without image - don't fail the property creation
                                        // But log the specific error for debugging
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("PropertyRepository", "Error uploading image: ${e.message}", e)
                                    // Continue without image - don't fail the property creation
                                }
                            }

                            return@withContext com.example.rentease.data.model.Result.Success(createdProperty)
                        } else {
                            android.util.Log.e("PropertyRepository", "Created property data is not a Map: ${body.data}")
                            return@withContext com.example.rentease.data.model.Result.Error("Failed to parse created property: data is not a Map")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("PropertyRepository", "Error parsing created property: ${e.message}", e)
                        return@withContext com.example.rentease.data.model.Result.Error("Failed to parse created property: ${e.message}")
                    }
                } else {
                    return@withContext com.example.rentease.data.model.Result.Error("Failed to create property")
                }
            } else {
                return@withContext com.example.rentease.data.model.Result.Error(handleApiError(response).message)
            }
        } catch (e: Exception) {
            android.util.Log.e("PropertyRepository", "Exception in createProperty: ${e.message}", e)
            return@withContext com.example.rentease.data.model.Result.Error(handleException(e).message)
        }
    }

    suspend fun updateProperty(property: Property, imageUri: Uri? = null): com.example.rentease.data.model.Result<Property> = withContext(Dispatchers.IO) {
        try {
            // Use Property directly - @Transient annotation excludes dateAdded from serialization
            val response = api.updateProperty(property.id, property)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    try {
                        if (body.data is Map<*, *>) {
                            val updatedProperty = convertMapToProperty(body.data)

                            // Upload single image if provided (replaces existing image)
                            imageUri?.let { uri ->
                                try {
                                    val result = imageUploader.uploadImage(updatedProperty.id, uri)
                                    if (result.isSuccess) {
                                        result.getOrNull()?.let { uploadedUrl ->
                                            updatedProperty.imageUrl = uploadedUrl
                                        }
                                    } else {
                                        val exception = result.exceptionOrNull()
                                        android.util.Log.e("PropertyRepository", "Image upload failed: ${exception?.message}", exception)
                                        // Continue without updating image - don't fail the property update
                                        // But log the specific error for debugging
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("PropertyRepository", "Error uploading updated image: ${e.message}", e)
                                    // Continue without updating image - don't fail the property update
                                }
                            }

                            return@withContext com.example.rentease.data.model.Result.Success(updatedProperty)
                        } else {
                            android.util.Log.e("PropertyRepository", "Updated property data is not a Map: ${body.data}")
                            return@withContext com.example.rentease.data.model.Result.Error("Failed to parse updated property: data is not a Map")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("PropertyRepository", "Error parsing updated property: ${e.message}", e)
                        return@withContext com.example.rentease.data.model.Result.Error("Failed to parse updated property: ${e.message}")
                    }
                } else {
                    return@withContext com.example.rentease.data.model.Result.Error("Failed to update property")
                }
            } else {
                return@withContext com.example.rentease.data.model.Result.Error(handleApiError(response).message)
            }
        } catch (e: Exception) {
            android.util.Log.e("PropertyRepository", "Exception in updateProperty: ${e.message}", e)
            return@withContext com.example.rentease.data.model.Result.Error(handleException(e).message)
        }
    }

    suspend fun deleteProperty(id: Int): com.example.rentease.data.model.Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteProperty(id)
            if (response.isSuccessful) {
                return@withContext com.example.rentease.data.model.Result.Success(Unit)
            } else {
                return@withContext com.example.rentease.data.model.Result.Error(handleApiError(response).message)
            }
        } catch (e: Exception) {
            return@withContext com.example.rentease.data.model.Result.Error(handleException(e).message)
        }
    }



    /**
     * Delete a property image
     *
     * @param imageId The ID of the image to delete
     * @return Result indicating success or failure
     */
    suspend fun deletePropertyImage(imageId: Int): com.example.rentease.data.model.Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val result = imageUploader.deleteImage(imageId)
            return@withContext if (result.isSuccess) {
                com.example.rentease.data.model.Result.Success(true)
            } else {
                val exception = result.exceptionOrNull()
                android.util.Log.e("PropertyRepository", "Failed to delete image: ${exception?.message}")
                com.example.rentease.data.model.Result.Error("Failed to delete image: ${exception?.message}")
            }
        } catch (e: Exception) {
            android.util.Log.e("PropertyRepository", "Exception in deletePropertyImage: ${e.message}", e)
            return@withContext com.example.rentease.data.model.Result.Error("Error deleting property image: ${e.message}")
        }
    }

    /**
     * Transform image URLs for Android emulator compatibility
     * With ADB reverse proxy, localhost:8000 URLs should work directly
     */
    private fun transformImageUrl(url: String?): String? {
        if (url == null) return null
        // With ADB reverse proxy, no transformation needed
        return url
    }

    companion object {
        fun getInstance(context: Context): PropertyRepository {
            val api = com.example.rentease.data.api.ApiClient.getApi(context)
            return PropertyRepository(api, context.applicationContext)
        }
    }
}
