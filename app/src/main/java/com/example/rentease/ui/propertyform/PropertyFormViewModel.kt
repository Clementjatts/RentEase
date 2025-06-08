package com.example.rentease.ui.propertyform

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.rentease.auth.AuthManager
import com.example.rentease.data.model.Property
import com.example.rentease.data.repository.PropertyRepository
import com.example.rentease.utils.PropertyImageItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * PropertyFormViewModel handles the business logic for the property form screen.
 */
class PropertyFormViewModel(
    application: Application,
    private val propertyId: Int
) : AndroidViewModel(application) {

    private val propertyRepository = PropertyRepository.getInstance(application)
    private val authManager = AuthManager.getInstance(application)

    private val _uiState = MutableStateFlow<PropertyFormUiState>(PropertyFormUiState.Initial)
    val uiState: StateFlow<PropertyFormUiState> = _uiState

    private val _image = MutableStateFlow<PropertyImageItem?>(null)
    val image: StateFlow<PropertyImageItem?> = _image

    /**
     * Load the property data from the repository.
     */
    fun loadPropertyDetails() {
        if (propertyId == -1) return // New property, don't load anything

        viewModelScope.launch {
            try {
                when (val result = propertyRepository.getPropertyById(propertyId)) {
                    is com.example.rentease.data.model.Result.Success -> {
                        val property = result.data
                        _uiState.value = PropertyFormUiState.PropertyData(
                            title = property.title,
                            description = property.description ?: "",
                            address = property.address,
                            price = property.price.toString(),
                            bedroomCount = property.bedroomCount.toString(),
                            bathroomCount = property.bathroomCount.toString(),
                            furnitureType = property.furnitureType
                        )

                        // Load existing image from the property's imageUrl
                        val imageUrl = property.imageUrl
                        if (!imageUrl.isNullOrEmpty()) {
                            val imageItem = PropertyImageItem(
                                uri = imageUrl.toUri(),
                                isExisting = true
                            )
                            _image.value = imageItem
                        }
                    }
                    is com.example.rentease.data.model.Result.Error -> {
                        _uiState.value = PropertyFormUiState.Error(result.errorMessage ?: "Failed to load property")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = PropertyFormUiState.Error(e.message ?: "An error occurred")
            }
        }
    }



    /**
     * Save the property to the repository.
     */
    fun saveProperty(
        title: String,
        description: String,
        address: String,
        price: String,
        bedroomCount: String,
        bathroomCount: String,
        furnitureType: String
    ) {
        if (title.isBlank() || description.isBlank() || address.isBlank() || price.isBlank()) {
            _uiState.value = PropertyFormUiState.Error("All fields are required")
            return
        }

        _uiState.value = PropertyFormUiState.Loading

        viewModelScope.launch {
            try {
                // Parse bedroom and bathroom counts (default to 0 if invalid)
                val bedroomCountInt = bedroomCount.toIntOrNull() ?: 0
                val bathroomCountInt = bathroomCount.toIntOrNull() ?: 0

                val result: com.example.rentease.data.model.Result<Property>

                // Get the single image URI if it exists and is new (not existing)
                val imageUri = _image.value?.let { imageItem ->
                    if (!imageItem.isExisting) {
                        imageItem.uri
                    } else {
                        null
                    }
                }

                if (propertyId == -1) {
                    // Create new property - need to get the correct landlord ID
                    val userType = authManager.userType
                    val landlordId = if (userType == com.example.rentease.auth.UserType.ADMIN) {
                        // For admin, we need to determine which landlord to assign
                        // For now, use a default or let the backend handle it
                        1 // Default to first landlord - this should be improved
                    } else {
                        // For landlord users, use userId directly as landlordId (since they're the same in the backend)
                        val userId = authManager.getUserId().toIntOrNull()
                        if (userId != null && userId > 0) {
                            userId
                        } else {
                            _uiState.value = PropertyFormUiState.Error("Invalid user ID")
                            return@launch
                        }
                    }

                    val newProperty = Property(
                        id = 0,
                        title = title,
                        description = description,
                        address = address,
                        price = price.toDouble(),
                        landlordId = landlordId,
                        bedroomCount = bedroomCountInt,
                        bathroomCount = bathroomCountInt,
                        furnitureType = furnitureType
                    )
                    result = propertyRepository.createProperty(newProperty, imageUri)
                } else {
                    // Update existing property - preserve the original landlord ID
                    // First get the existing property to preserve its landlord ID
                    when (val existingPropertyResult = propertyRepository.getPropertyById(propertyId)) {
                        is com.example.rentease.data.model.Result.Success -> {
                            val existingProperty = existingPropertyResult.data
                            val updatedProperty = Property(
                                id = propertyId,
                                title = title,
                                description = description,
                                address = address,
                                price = price.toDouble(),
                                landlordId = existingProperty.landlordId, // Preserve original landlord ID
                                bedroomCount = bedroomCountInt,
                                bathroomCount = bathroomCountInt,
                                furnitureType = furnitureType
                            )
                            result = propertyRepository.updateProperty(updatedProperty, imageUri)
                        }
                        is com.example.rentease.data.model.Result.Error -> {
                            _uiState.value = PropertyFormUiState.Error("Failed to load existing property: ${existingPropertyResult.errorMessage}")
                            return@launch
                        }
                    }
                }

                when (result) {
                    is com.example.rentease.data.model.Result.Success -> {
                        _uiState.value = PropertyFormUiState.Success
                    }
                    is com.example.rentease.data.model.Result.Error -> {
                        _uiState.value = PropertyFormUiState.Error(result.errorMessage ?: "Failed to save property")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = PropertyFormUiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    /**
     * Set the single image for the property (replaces any existing image).
     */
    fun addImage(uri: Uri) {
        _image.value = PropertyImageItem(uri = uri, isExisting = false)
    }

    /**
     * Remove the single image from the property.
     */
    fun removeImage() {
        _image.value = null
    }

    /**
     * Factory for creating PropertyFormViewModel instances.
     */
    class Factory(
        private val application: Application,
        private val propertyId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PropertyFormViewModel::class.java)) {
                return PropertyFormViewModel(application, propertyId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * Represents the UI state for the property form screen.
 */
sealed class PropertyFormUiState {
    data object Initial : PropertyFormUiState()
    data object Loading : PropertyFormUiState()
    data object Success : PropertyFormUiState()
    data class Error(val message: String) : PropertyFormUiState()
    data class PropertyData(
        val title: String,
        val description: String,
        val address: String,
        val price: String,
        val bedroomCount: String = "0",
        val bathroomCount: String = "0",
        val furnitureType: String = "unfurnished"
    ) : PropertyFormUiState()
}
